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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.CheckWinResult;
import fr.leblanc.gomoku.engine.model.CompoThreatType;
import fr.leblanc.gomoku.engine.model.DoubleThreat;
import fr.leblanc.gomoku.engine.model.EvaluationContext;
import fr.leblanc.gomoku.engine.model.EvaluationResult;
import fr.leblanc.gomoku.engine.model.GameData;
import fr.leblanc.gomoku.engine.model.Threat;
import fr.leblanc.gomoku.engine.model.ThreatContext;
import fr.leblanc.gomoku.engine.model.ThreatType;
import fr.leblanc.gomoku.engine.service.CacheService;
import fr.leblanc.gomoku.engine.service.CheckWinService;
import fr.leblanc.gomoku.engine.service.EvaluationService;
import fr.leblanc.gomoku.engine.service.StrikeService;
import fr.leblanc.gomoku.engine.service.ThreatContextService;
import fr.leblanc.gomoku.engine.util.Pair;

@Service
public class EvaluationServiceImpl implements EvaluationService {

	private static final Logger logger = LoggerFactory.getLogger(EvaluationServiceImpl.class);
	
	@Autowired
	private ThreatContextService threatContextService;
	
	@Autowired
	private CheckWinService checkWinService;
	
	@Autowired
	private CacheService cacheService;
	
	@Autowired
	private StrikeService strikeService;
	
	@Override
	public EvaluationResult computeEvaluation(Long gameId, EvaluationContext context) throws InterruptedException {
		
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
		
		if (context.isUseStrikeService() && strikeService.hasStrike(context.getGameData(), playingColor, gameId, true)) {
			if (!context.isInternal() && logger.isDebugEnabled()) {
				logger.debug("evaluation from strike: {}", THREAT_5_POTENTIAL);
			}
			EvaluationResult evaluation = new EvaluationResult();
			evaluation.setEvaluation(THREAT_5_POTENTIAL);
			return evaluation;
		}
		
		if (context.isInternal() && gameId != null && cacheService.isCacheEnabled() && cacheService.getEvaluationCache(gameId).get(playingColor).containsKey(context.getGameData())) {
			return cacheService.getEvaluationCache(gameId).get(playingColor).get(context.getGameData());
		}
		
		EvaluationResult evaluation =  evaluateThreats(context);
		
		if (gameId != null && cacheService.isCacheEnabled()) {
			cacheService.getEvaluationCache(gameId).get(playingColor).put(new GameData(context.getGameData()), evaluation);
		}
		
		return evaluation;
	}
	
	private EvaluationResult evaluateThreats(EvaluationContext context) {
		
		EvaluationResult evaluationResult = new EvaluationResult();
		
		ThreatContext playingThreatContext = threatContextService.computeThreatContext(context.getGameData(), context.getPlayingColor());
		ThreatContext opponentThreatContext = threatContextService.computeThreatContext(context.getGameData(), -context.getPlayingColor());

		int evaluation = 0;
		
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
					logger.debug("{} AT {} FROM {}", threatEvaluation, retrieveThreatCell(threatPair), compoThreatType);
				}
			}
			
			evaluationResult.getEvaluationMap().put(compoThreatType, threatTypeEvaluation);
		}
		
		evaluationResult.setEvaluation(evaluation);
		
		return evaluationResult;
	}

	private Map<CompoThreatType, List<Pair<Threat, Threat>>> getCompositeThreats(ThreatContext playingThreatContext, ThreatContext opponentThreatContext) {
		Map<CompoThreatType, List<Pair<Threat, Threat>>> compositeThreatMap = new HashMap<>();
		for (CompoThreatType tryContext : CompoThreatType.COMPO_THREAT_TYPES) {
			if (tryContext.isPlaying()) {
				compositeThreatMap.put(tryContext, threatContextService.findCompositeThreats(playingThreatContext, tryContext));
			} else {
				compositeThreatMap.put(tryContext, threatContextService.findCompositeThreats(opponentThreatContext, tryContext));
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
		
		Cell threatCell = retrieveThreatCell(threatPair);
		
		// check for a pending attack for the given compoThreatType, find the blocking moves, compare them with threat cell, count how many => pendingAttackCount
		int pendingAttackCount = opponentAttackCount(compositeThreatMap, compoThreatType, threatCell);
		
		if (pendingAttackCount > 1) {
			return 0;
		}
			
		// check for the blocking moves of first threat, check for opponent threatTypes corresponding, compare with second threat type/moves => isBlocked
		boolean isPairBlocked = isThreatPairBlocked(playingThreatContext, opponentThreatContext, threatPair, threatCell);
		
		if (pendingAttackCount == 1 && isPairBlocked) {
			return 0;
		}
		
		if (pendingAttackCount == 0 && !isPairBlocked) {
			validatedThreatMap.computeIfAbsent(compoThreatType, k -> new ArrayList<>()).add(threatPair);
			return compoThreatType.getPotential();
		} 
		
		if (hasSimilarThreat(opponentThreatMap, compoThreatType, threatPair, true)) {
			return compoThreatType.getPotential() / 2;
		}
		opponentThreatMap.computeIfAbsent(compoThreatType, k -> new ArrayList<>()).add(threatPair);
		
		return 0;
	}

	private int evaluateOpponentThreatPair(ThreatContext playingThreatContext,
			Map<CompoThreatType, List<Pair<Threat, Threat>>> compositeThreatMap,
			Map<CompoThreatType, List<Pair<Threat, Threat>>> validatedThreatMap, Pair<Threat, Threat> threatPair,
			CompoThreatType compoThreatType) {
	
		boolean hasDirectAttack = hasDirectAttack(compositeThreatMap, compoThreatType);
	
		if (!hasDirectAttack) {
			boolean isFirstThreatKilled = areThreatsKilled(playingThreatContext, threatPair);
			
			if (!isFirstThreatKilled) {
				if (hasSimilarThreat(validatedThreatMap, compoThreatType, threatPair, false)) {
					return -compoThreatType.getPotential() / 2;
				}
				validatedThreatMap.computeIfAbsent(compoThreatType, k -> new ArrayList<>()).add(threatPair);
			}
		}
		
		return 0;
	}

	private boolean areThreatsKilled(ThreatContext playingThreatContext, Pair<Threat, Threat> threatPair) {
		for (Cell killingCell : threatPair.getFirst().getKillingCells()) {
			
			for (ThreatType killingType : threatPair.getFirst().getThreatType().getKillingThreatTypes()) {
				if (playingThreatContext.getCellToThreatMap().containsKey(killingCell) && playingThreatContext.getCellToThreatMap().get(killingCell).containsKey(killingType) && !playingThreatContext.getCellToThreatMap().get(killingCell).get(killingType).isEmpty()) {
					return true;
				}
			}
		}
		
		if (threatPair.getSecond() != null) {
			for (Cell killingCell : threatPair.getSecond().getKillingCells()) {
				
				for (ThreatType killingType : threatPair.getSecond().getThreatType().getKillingThreatTypes()) {
					if (playingThreatContext.getCellToThreatMap().containsKey(killingCell) && playingThreatContext.getCellToThreatMap().get(killingCell).containsKey(killingType) && !playingThreatContext.getCellToThreatMap().get(killingCell).get(killingType).isEmpty()) {
						return true;
					}
				}
			}
		}
		
		return false;
	}

	private boolean hasDirectAttack(Map<CompoThreatType, List<Pair<Threat, Threat>>> compositeThreatMap, CompoThreatType compoThreatType) {
		
		for (CompoThreatType directAttackingType : compoThreatType.getSimilarOrBetterCompoThreatTypes(false, true)) {
			
			boolean isAlreadyCountered = false;
			
			for (CompoThreatType pendingType : directAttackingType.getSimilarOrBetterCompoThreatTypes(false, false)) {
				if (!compositeThreatMap.containsKey(pendingType) || !compositeThreatMap.get(pendingType).isEmpty()) {
					isAlreadyCountered = true;
					break;
				}
			}
			
			if (!isAlreadyCountered) {
				if (compositeThreatMap.containsKey(directAttackingType) && !compositeThreatMap.get(directAttackingType).isEmpty()) {
					return true;
				}
			}
		}
		
		return false;
	}

	private int opponentAttackCount(Map<CompoThreatType, List<Pair<Threat, Threat>>> compositeThreatMap, CompoThreatType compoThreatType, Cell threatPosition) {
		
		List<Pair<Threat, Threat>> notBlockedPendingPairs = new ArrayList<>();
		
		for (CompoThreatType pendingType : compoThreatType.getSimilarOrBetterCompoThreatTypes(false, false)) {
			for (Pair<Threat, Threat> pendingPair : compositeThreatMap.get(pendingType)) {
				Cell killingCell = retrieveThreatCell(pendingPair);
				if (!killingCell.equals(threatPosition)) {
					notBlockedPendingPairs.add(pendingPair);
				}
			}
		}
		
		List<Set<Cell>> setsOfBlocking = new ArrayList<>();
		
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
			}
			
		}
		
		return setsOfBlocking.size();
	}

	private boolean isThreatPairBlocked(ThreatContext playingThreatContext, ThreatContext opponentThreatContext, Pair<Threat, Threat> threatPair, Cell threatCell) {
		
		Set<Cell> blockingCells = extractBlockingCells(threatPair, threatCell);
		
		for (Cell blockingCell : blockingCells) {
			if (opponentThreatContext.getCellToThreatMap().containsKey(blockingCell)) {
				for (Entry<ThreatType, List<Threat>> entry : opponentThreatContext.getCellToThreatMap().get(blockingCell).entrySet()) {
					ThreatType opponentThreatType = entry.getKey();
					List<Threat> oppponentThreats = entry.getValue();
					
					boolean isOpponentThreatTypeBetterThanFirst = threatPair.getFirst().getThreatType().getBlockingThreatTypes().contains(opponentThreatType);
					
					if (isOpponentThreatTypeBetterThanFirst) {
						boolean isOpponentThreatTypeBetterThanSecond = threatPair.getSecond() == null || !opponentThreatType.getBetterOrEqualThreatTypes().contains(threatPair.getSecond().getThreatType());
						if (isOpponentThreatTypeBetterThanSecond) {
							boolean isBlocked = true;
							for (Threat opponentKillingThreat : oppponentThreats) {
								Set<Cell> reBlockingCells = opponentKillingThreat.getBlockingCells(blockingCell);
								
								for (Cell reblockingCell : reBlockingCells) {
									
									if (playingThreatContext.getCellToThreatMap().containsKey(reblockingCell)) {
										
										for (Entry<ThreatType, List<Threat>> entry2 : playingThreatContext.getCellToThreatMap().get(reblockingCell).entrySet()) {
											
											if (threatPair.getSecond() == null || threatPair.getSecond().getThreatType().getBetterOrEqualThreatTypes().contains(entry2.getKey())) {
												isBlocked = false;
												break;
											}
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

	private Set<Cell> extractBlockingCells(Pair<Threat, Threat> threatPair, Cell threatCell) {
		Set<Cell> blockingCells = new HashSet<>(threatPair.getFirst().getBlockingCells(threatCell));
		if (threatPair.getSecond() != null && threatPair.getFirst().getThreatType().equals(threatPair.getSecond().getThreatType())) {
			blockingCells.addAll(threatPair.getSecond().getBlockingCells(threatCell));
		}
		return blockingCells;
	}

	private boolean hasSimilarThreat(Map<CompoThreatType, List<Pair<Threat, Threat>>> pendingThreatMap, CompoThreatType compoThreatType, Pair<Threat, Threat> threatPair, boolean isPlaying) {
		
		Set<Cell> firstBlockingCells = getBlockingCells(threatPair, isPlaying);
		
		for (CompoThreatType betterThreatType : compoThreatType.getSimilarOrBetterCompoThreatTypes(true, true)) {
			for (Pair<Threat, Threat> previousPair : pendingThreatMap.computeIfAbsent(betterThreatType, k -> new ArrayList<>())) {
				Set<Cell> previousBlockingCells = getBlockingCells(previousPair, isPlaying);
				if (previousBlockingCells.stream().filter(firstBlockingCells::contains).count() == 0) {
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
	
	private Set<Cell> getBlockingCells(Pair<Threat, Threat> threatPair, boolean isPlaying) {
		Set<Cell> blockingCells = new HashSet<>();
		
		if (isPlaying) {
			Cell secondThreatCell = retrieveThreatCell(threatPair);
			blockingCells.addAll(threatPair.getFirst().getBlockingCells(secondThreatCell));
			if (threatPair.getSecond() != null) {
				blockingCells.addAll(threatPair.getSecond().getBlockingCells(secondThreatCell));
			}
		} else {
			blockingCells.addAll(threatPair.getFirst().getKillingCells());
			if (threatPair.getSecond() != null) {
				blockingCells.addAll(threatPair.getSecond().getKillingCells());
			}
		}
		
		return blockingCells;
	}

	private Cell retrieveThreatCell(Pair<Threat, Threat> threatPair) {
		
		if (threatPair.getFirst().getThreatType().isDoubleType()) {
			return ((DoubleThreat) threatPair.getFirst()).getTargetCell();
		}
		
		if (threatPair.getSecond() == null) {
			return threatPair.getFirst().getEmptyCells().iterator().next();
		}
		
		if (threatPair.getSecond().getThreatType().isDoubleType()) {
			return ((DoubleThreat) threatPair.getSecond()).getTargetCell();
		}
		
		return threatPair.getFirst().getEmptyCells().stream().filter(c -> threatPair.getSecond().getEmptyCells().contains(c)).findFirst().orElseThrow();
		
	}

}
