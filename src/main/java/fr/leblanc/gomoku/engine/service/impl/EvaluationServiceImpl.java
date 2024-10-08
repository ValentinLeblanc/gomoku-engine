package fr.leblanc.gomoku.engine.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.CheckWinResult;
import fr.leblanc.gomoku.engine.model.CompoThreatType;
import fr.leblanc.gomoku.engine.model.EvaluationContext;
import fr.leblanc.gomoku.engine.model.EvaluationResult;
import fr.leblanc.gomoku.engine.model.GameData;
import fr.leblanc.gomoku.engine.model.Threat;
import fr.leblanc.gomoku.engine.model.ThreatContext;
import fr.leblanc.gomoku.engine.model.ThreatType;
import fr.leblanc.gomoku.engine.service.CacheService;
import fr.leblanc.gomoku.engine.service.CheckWinService;
import fr.leblanc.gomoku.engine.service.EvaluationService;
import fr.leblanc.gomoku.engine.service.ThreatService;
import fr.leblanc.gomoku.engine.util.Pair;

@Service
public class EvaluationServiceImpl implements EvaluationService {

	private static final Logger logger = LoggerFactory.getLogger(EvaluationServiceImpl.class);
	
	private ThreatService threatService;
	
	private CheckWinService checkWinService;
	
	private CacheService cacheService;
	
	public EvaluationServiceImpl(ThreatService threatService, CheckWinService checkWinService,
			CacheService cacheService) {
		super();
		this.threatService = threatService;
		this.checkWinService = checkWinService;
		this.cacheService = cacheService;
	}

	@Override
	public EvaluationResult computeEvaluation(Long gameId, EvaluationContext context) throws InterruptedException {
		
		StopWatch stopWatch = null;
		
		if (logger.isDebugEnabled() && !context.isInternal()) {
			stopWatch = new StopWatch("computeEvaluation");
			stopWatch.start();
		}
		
		int playingColor = GameData.extractPlayingColor(context.getGameData());
		context.setPlayingColor(playingColor);
		
		EvaluationResult evaluationResult = new EvaluationResult();
		CheckWinResult checkWinResult = checkWinService.checkWin(context.getGameData());
		
		if (checkWinResult.isWin()) {
			if (checkWinResult.getColor() == context.getPlayingColor()) {
				evaluationResult.setEvaluation(WIN_EVALUATION);
			} else {
				evaluationResult.setEvaluation(-WIN_EVALUATION);
			}
			return evaluationResult;
		}
		
		EvaluationResult evaluation =  evaluateThreats(gameId, context, 0);
		
		if (logger.isDebugEnabled() && stopWatch != null && !context.isInternal()) {
			stopWatch.stop();
			logger.debug("computeEvaluation elapsed time: {} ms", stopWatch.lastTaskInfo().getTimeMillis());
		}
		
		return evaluation;
	}
	
	private EvaluationResult evaluateThreats(Long gameId, EvaluationContext context, int depth) {
		
		if (gameId != null && cacheService.isCacheEnabled() && cacheService.getEvaluationCache(gameId).get(context.getPlayingColor()).containsKey(context.getGameData())) {
			EvaluationResult cachedResult = cacheService.getEvaluationCache(gameId).get(context.getPlayingColor()).get(context.getGameData());
			
			if (logger.isDebugEnabled() && !context.isInternal()) {
				
				for (Entry<CompoThreatType, Double> entry : cachedResult.getEvaluationMap().entrySet()) {
					double threatEvaluation = entry.getValue();
					if (threatEvaluation != 0d) {
						logger.debug("{} FROM {}", threatEvaluation, entry.getKey());
					}
				}
			}
			return cachedResult;
		}
		
		EvaluationResult evaluationResult = new EvaluationResult();
		
		ThreatContext playingThreatContext = threatService.getOrUpdateThreatContext(context.getGameData(), context.getPlayingColor());
		ThreatContext opponentThreatContext = threatService.getOrUpdateThreatContext(context.getGameData(), -context.getPlayingColor());

		if (!playingThreatContext.getThreatsOfType(ThreatType.THREAT_5).isEmpty()) {
			evaluationResult.setEvaluation(THREAT_5_POTENTIAL);
		}
		
		double evaluation = 0;
		
		Map<CompoThreatType, List<Pair<Threat, Threat>>> validatedThreatMap = new HashMap<>();
		Map<CompoThreatType, List<Pair<Threat, Threat>>> opponentThreatMap = new HashMap<>();
		Map<CompoThreatType, List<Pair<Threat, Threat>>> compositeThreatMap = getCompositeThreats(playingThreatContext, opponentThreatContext);

		for (CompoThreatType compoThreatType : CompoThreatType.COMPO_THREAT_TYPES) {
			
			double threatTypeEvaluation = 0;
			
			for (Pair<Threat, Threat> threatPair : compositeThreatMap.get(compoThreatType)) {
				int threatEvaluation = 0;
				if (compoThreatType.isPlaying()) {
					threatEvaluation = evaluatePlayingThreatPair(playingThreatContext, opponentThreatContext, compositeThreatMap, validatedThreatMap, opponentThreatMap, threatPair, compoThreatType);
				} else {
					threatEvaluation = evaluateOpponentThreatPair(playingThreatContext, compositeThreatMap, validatedThreatMap, threatPair, compoThreatType);
				}
				evaluation += threatEvaluation;
				threatTypeEvaluation += threatEvaluation;
				if (logger.isDebugEnabled() && threatEvaluation != 0 && !context.isInternal()) {
					logger.debug("{} AT {} FROM {}", threatEvaluation, threatPair.getFirst().getTargetCell(), compoThreatType);
				}
				Double previousCellEvaluation = evaluationResult.getCellEvaluationMap().computeIfAbsent(threatPair.getFirst().getTargetCell(), k -> 0d);
				evaluationResult.getCellEvaluationMap().put(threatPair.getFirst().getTargetCell(), previousCellEvaluation + threatEvaluation);
			}
			
			evaluationResult.getEvaluationMap().put(compoThreatType, threatTypeEvaluation);
		}
		
		double deepEvaluation = Double.NEGATIVE_INFINITY;
		
		if (deepEvaluation > evaluation) {
			evaluation = deepEvaluation;
		}
		
		evaluationResult.setEvaluation(evaluation);
		
		if (gameId != null && cacheService.isCacheEnabled()) {
			cacheService.getEvaluationCache(gameId).get(context.getPlayingColor()).put(new GameData(context.getGameData()), evaluationResult);
		}
		
		return evaluationResult;
	}

	private Map<CompoThreatType, List<Pair<Threat, Threat>>> getCompositeThreats(ThreatContext playingThreatContext, ThreatContext opponentThreatContext) {
		Map<CompoThreatType, List<Pair<Threat, Threat>>> compositeThreatMap = new HashMap<>();
		for (CompoThreatType tryContext : CompoThreatType.COMPO_THREAT_TYPES) {
			if (tryContext.isPlaying()) {
				compositeThreatMap.put(tryContext, threatService.findCompositeThreats(playingThreatContext, tryContext));
			} else {
				compositeThreatMap.put(tryContext, threatService.findCompositeThreats(opponentThreatContext, tryContext));
			}
		}
		return compositeThreatMap;
	}

	private int evaluatePlayingThreatPair(ThreatContext playingThreatContext, ThreatContext opponentThreatContext,
			Map<CompoThreatType, List<Pair<Threat, Threat>>> compositeThreatMap,
			Map<CompoThreatType, List<Pair<Threat, Threat>>> validatedThreatMap,
			Map<CompoThreatType, List<Pair<Threat, Threat>>> opponentThreatMap,
			Pair<Threat, Threat> threatPair,
			CompoThreatType compoThreatType) {
		
		Cell threatCell = threatPair.getFirst().getTargetCell();
		
		// check for a pending attack for the given compoThreatType, find the blocking moves, compare them with threat cell, count how many => opponentAttackCount
		int opponentAttackCount = opponentAttackCount(compositeThreatMap, compoThreatType, threatCell);
		
		if (opponentAttackCount > 1) {
			return 0;
		}
			
		// check for the blocking moves of first threat, check for opponent threatTypes corresponding, compare with second threat type/moves => isBlocked
		boolean isPairBlocked = isThreatPairBlocked(playingThreatContext, opponentThreatContext, threatPair);
		
		if (opponentAttackCount == 1 && isPairBlocked) {
			return 0;
		}
		
		if (opponentAttackCount == 0 && !isPairBlocked) {
			validatedThreatMap.computeIfAbsent(compoThreatType, k -> new ArrayList<>()).add(threatPair);
			return compoThreatType.getPotential();
		} 
		
		if (hasSimilarThreat(opponentThreatMap, compoThreatType, threatPair, true)) {
			return compoThreatType.getPotential() / 2;
		}
		opponentThreatMap.computeIfAbsent(compoThreatType, k -> new ArrayList<>()).add(threatPair);
		
		return 0;
	}

	private int opponentAttackCount(Map<CompoThreatType, List<Pair<Threat, Threat>>> compositeThreatMap, CompoThreatType compoThreatType, Cell threatPosition) {
		
		Set<Pair<Threat, Threat>> notBlockedPendingPairs = new HashSet<>();
		
		for (CompoThreatType pendingType : compoThreatType.getSimilarOrBetterCompoThreatTypes(false, false)) {
			for (Pair<Threat, Threat> pendingPair : compositeThreatMap.get(pendingType)) {
				Cell killingCell = pendingPair.getFirst().getTargetCell();
				if (!killingCell.equals(threatPosition)) {
					notBlockedPendingPairs.add(pendingPair);
				}
			}
		}
		
		Set<Set<Cell>> setsOfBlocking = new HashSet<>();
		
		for (Pair<Threat, Threat> notBlockedPendingPair1 : notBlockedPendingPairs) {
			Set<Cell> killingCells = getKillingCells(notBlockedPendingPair1);
			boolean isPresent = false;
			for (Set<Cell> set : setsOfBlocking) {
				if (set.stream().anyMatch(killingCells::contains)) {
					isPresent = true;
				}
			}
			if (!isPresent) {
				setsOfBlocking.add(new HashSet<>(killingCells));
				if (setsOfBlocking.size() > 1) {
					return setsOfBlocking.size();
				}
			}
		}
		
		return setsOfBlocking.size();
	}

	private boolean isThreatPairBlocked(ThreatContext playingThreatContext, ThreatContext opponentThreatContext, Pair<Threat, Threat> threatPair) {
		
		Set<Cell> blockingCells = extractBlockingCells(threatPair);
		
		for (Cell blockingCell : blockingCells) {
			for (Entry<ThreatType, List<Threat>> entry : opponentThreatContext.getThreatsOfTargetCell(blockingCell).entrySet()) {
				ThreatType opponentThreatType = entry.getKey();
				List<Threat> oppponentThreats = entry.getValue();
				
				if (!oppponentThreats.isEmpty()) {
					boolean isOpponentThreatTypeBetterThanFirst = threatPair.getFirst().getThreatType().getBlockingThreatTypes().contains(opponentThreatType);
					if (isOpponentThreatTypeBetterThanFirst) {
						boolean isOpponentThreatTypeBetterThanSecond = threatPair.getSecond() == null || !opponentThreatType.getBetterOrEqualThreatTypes().contains(threatPair.getSecond().getThreatType());
						if (isOpponentThreatTypeBetterThanSecond) {
							boolean isBlocked = true;
							for (Threat opponentKillingThreat : oppponentThreats) {
								Set<Cell> reBlockingCells = opponentKillingThreat.getBlockingCells();
								
								for (Cell reblockingCell : reBlockingCells) {
									for (Entry<ThreatType, List<Threat>> entry2 : playingThreatContext.getThreatsOfTargetCell(reblockingCell).entrySet()) {
										ThreatType newThreatType = entry2.getKey();
										List<Threat> newThreats = entry2.getValue();
										if (threatPair.getSecond() == null || !newThreats.isEmpty() && threatPair.getSecond().getThreatType().getBetterOrEqualThreatTypes().contains(newThreatType)) {
											isBlocked = false;
											break;
										}
									}
									if (!isBlocked) {
										break;
									}
								}
							}
							if (isBlocked) {
								return true;
							}
						}
					}
				}
			}
		}
		
		return false;
	}

	private int evaluateOpponentThreatPair(ThreatContext playingThreatContext,
			Map<CompoThreatType, List<Pair<Threat, Threat>>> compositeThreatMap,
			Map<CompoThreatType, List<Pair<Threat, Threat>>> validatedThreatMap, Pair<Threat, Threat> threatPair,
			CompoThreatType compoThreatType) {
	
		boolean hasDirectAttack = hasPlayingAttack(compositeThreatMap, compoThreatType);
	
		if (!hasDirectAttack) {
			boolean isFirstThreatKilled = isThreatPairKilled(playingThreatContext, threatPair);
			
			if (!isFirstThreatKilled) {
				if (hasSimilarThreat(validatedThreatMap, compoThreatType, threatPair, false)) {
					return -compoThreatType.getPotential() / 2;
				}
				validatedThreatMap.computeIfAbsent(compoThreatType, k -> new ArrayList<>()).add(threatPair);
			}
		}
		
		return 0;
	}

	private boolean hasPlayingAttack(Map<CompoThreatType, List<Pair<Threat, Threat>>> compositeThreatMap, CompoThreatType compoThreatType) {
		
		for (CompoThreatType directAttackingType : compoThreatType.getSimilarOrBetterCompoThreatTypes(false, true)) {
			
			boolean isAlreadyCountered = false;
			
			for (CompoThreatType pendingType : directAttackingType.getSimilarOrBetterCompoThreatTypes(false, false)) {
				if (!compositeThreatMap.containsKey(pendingType) || !compositeThreatMap.get(pendingType).isEmpty()) {
					isAlreadyCountered = true;
					break;
				}
			}
			
			if (!isAlreadyCountered && compositeThreatMap.containsKey(directAttackingType) && !compositeThreatMap.get(directAttackingType).isEmpty()) {
				return true;
			}
		}
		
		return false;
	}

	private boolean isThreatPairKilled(ThreatContext playingThreatContext, Pair<Threat, Threat> threatPair) {
		for (Cell killingCell : threatPair.getFirst().getKillingCells()) {
			
			for (ThreatType killingType : threatPair.getFirst().getThreatType().getKillingThreatTypes()) {
				if (!playingThreatContext.getThreatsOfTargetCell(killingCell).get(killingType).isEmpty()) {
					return true;
				}
			}
		}
		
		if (threatPair.getSecond() != null) {
			for (Cell killingCell : threatPair.getSecond().getKillingCells()) {
				
				for (ThreatType killingType : threatPair.getSecond().getThreatType().getKillingThreatTypes()) {
					if (!playingThreatContext.getThreatsOfTargetCell(killingCell).get(killingType).isEmpty()) {
						return true;
					}
				}
			}
		}
		
		return false;
	}

	private Set<Cell> extractBlockingCells(Pair<Threat, Threat> threatPair) {
		Set<Cell> blockingCells = new HashSet<>(threatPair.getFirst().getBlockingCells());
		if (threatPair.getSecond() != null && threatPair.getFirst().getThreatType().equals(threatPair.getSecond().getThreatType())) {
			blockingCells.addAll(threatPair.getSecond().getBlockingCells());
		}
		return blockingCells;
	}

	private boolean hasSimilarThreat(Map<CompoThreatType, List<Pair<Threat, Threat>>> pendingThreatMap, CompoThreatType compoThreatType, Pair<Threat, Threat> threatPair, boolean isPlaying) {
		
		Set<Cell> firstPairCounterCells = getPairCounterCells(threatPair, isPlaying);
		
		for (CompoThreatType betterThreatType : compoThreatType.getSimilarOrBetterCompoThreatTypes(true, true)) {
			for (Pair<Threat, Threat> previousPair : pendingThreatMap.computeIfAbsent(betterThreatType, k -> new ArrayList<>())) {
				Set<Cell> previousCounterCells = getPairCounterCells(previousPair, isPlaying);
				if (previousCounterCells.stream().filter(firstPairCounterCells::contains).count() == 0) {
					return true;
				}
			}
		}
		
		return false;
	}

	private Set<Cell> getKillingCells(Pair<Threat, Threat> threatPair) {
		Set<Cell> killingCells = new HashSet<>();
		killingCells.addAll(threatPair.getFirst().getKillingCells());
		if (threatPair.getSecond() != null) {
			killingCells.addAll(threatPair.getSecond().getKillingCells());
		}
		return killingCells;
	}
	
	private Set<Cell> getPairCounterCells(Pair<Threat, Threat> threatPair, boolean isPlaying) {
		Set<Cell> blockingCells = new HashSet<>();
		
		if (isPlaying) {
			blockingCells.addAll(threatPair.getFirst().getBlockingCells());
			if (threatPair.getSecond() != null) {
				blockingCells.addAll(threatPair.getSecond().getBlockingCells());
			}
		} else {
			blockingCells.addAll(threatPair.getFirst().getKillingCells());
			if (threatPair.getSecond() != null) {
				blockingCells.addAll(threatPair.getSecond().getKillingCells());
			}
		}
		
		return blockingCells;
	}

}
