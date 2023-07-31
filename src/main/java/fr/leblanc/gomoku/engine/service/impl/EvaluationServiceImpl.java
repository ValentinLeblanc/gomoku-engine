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
import fr.leblanc.gomoku.engine.model.GameData;
import fr.leblanc.gomoku.engine.model.DoubleThreat;
import fr.leblanc.gomoku.engine.model.EngineConstants;
import fr.leblanc.gomoku.engine.model.EvaluationContext;
import fr.leblanc.gomoku.engine.model.EvaluationResult;
import fr.leblanc.gomoku.engine.model.Threat;
import fr.leblanc.gomoku.engine.model.ThreatContext;
import fr.leblanc.gomoku.engine.model.ThreatType;
import fr.leblanc.gomoku.engine.service.CacheService;
import fr.leblanc.gomoku.engine.service.CheckWinService;
import fr.leblanc.gomoku.engine.service.EvaluationService;
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
	
	@Override
	public EvaluationResult computeEvaluation(GameData dataWrapper) {
		return computeEvaluation(dataWrapper, false);
	}
	
	@Override
	public EvaluationResult computeEvaluation(GameData dataWrapper, boolean externalCall) {
		
		int playingColor = GameData.extractPlayingColor(dataWrapper);
		
		if (!externalCall && cacheService.isCacheEnabled() && cacheService.getEvaluationCache().get(playingColor).containsKey(dataWrapper)) {
			return cacheService.getEvaluationCache().get(playingColor).get(dataWrapper);
		}
		
		EvaluationResult evaluation =  evaluateThreats(new EvaluationContext(dataWrapper, playingColor, -1, 0, externalCall));
		
		if (!externalCall && cacheService.isCacheEnabled()) {
			cacheService.getEvaluationCache().get(playingColor).put(new GameData(dataWrapper), evaluation);
		}
		
		return evaluation;
	}

	private EvaluationResult evaluateThreats(EvaluationContext context) {
		
		EvaluationResult evaluationResult = new EvaluationResult();
		
		CheckWinResult checkWinResult = checkWinService.checkWin(context.getDataWrapper());
		
		if (checkWinResult.isWin()) {
			if (checkWinResult.getColor() == context.getPlayingColor()) {
				evaluationResult.setEvaluation(EngineConstants.WIN_EVALUATION);
			} else {
				evaluationResult.setEvaluation(-EngineConstants.WIN_EVALUATION);
			}
			return evaluationResult;
		}
		
		ThreatContext playingThreatContext = threatContextService.computeThreatContext(context.getDataWrapper(), context.getPlayingColor());
		ThreatContext opponentThreatContext = threatContextService.computeThreatContext(context.getDataWrapper(), -context.getPlayingColor());

		int evaluation = 0;
		
		Map<CompoThreatType, List<Pair<Threat, Threat>>> compositeThreatMap = new HashMap<>();
		
		Map<CompoThreatType, List<Pair<Threat, Threat>>> validatedThreatMap = new HashMap<>();
		
		Map<CompoThreatType, List<Pair<Threat, Threat>>> pendingThreatMap = new HashMap<>();
		
		initializeMaps(playingThreatContext, opponentThreatContext, compositeThreatMap, validatedThreatMap, pendingThreatMap);
		
		for (CompoThreatType compoThreatType : EngineConstants.COMPO_THREAT_TYPES) {
			
			double threatTypeEvaluation = 0;
			
			for (Pair<Threat, Threat> threatPair : compositeThreatMap.get(compoThreatType)) {
				int threatEvaluation = 0;
				if (compoThreatType.isPlaying()) {
					threatEvaluation = evaluatePlayingThreatPair(playingThreatContext, opponentThreatContext, compositeThreatMap, validatedThreatMap, pendingThreatMap, threatPair, compoThreatType);
				} else {
					threatEvaluation = evaluatePendingThreatPair(playingThreatContext, compositeThreatMap, validatedThreatMap, threatPair, compoThreatType);
				}
				evaluation += threatEvaluation;
				threatTypeEvaluation += threatEvaluation;
				if (logger.isDebugEnabled() && threatEvaluation != 0 && context.isLogEnabled()) {
					logger.debug(threatEvaluation + " AT " + retrieveThreatCell(threatPair) + " FROM "  + compoThreatType);
				}
			}
			
			evaluationResult.getEvaluationMap().put(compoThreatType, threatTypeEvaluation);
		}
		
		evaluationResult.setEvaluation(evaluation);
		
		return evaluationResult;
	}

	private void initializeMaps(ThreatContext playingThreatContext, ThreatContext opponentThreatContext,
			Map<CompoThreatType, List<Pair<Threat, Threat>>> compositeThreatMap,
			Map<CompoThreatType, List<Pair<Threat, Threat>>> validatedThreatMap,
			Map<CompoThreatType, List<Pair<Threat, Threat>>> pendingThreatMap) {
		for (CompoThreatType tryContext : EngineConstants.COMPO_THREAT_TYPES) {
			
			if (tryContext.isPlaying()) {
				compositeThreatMap.put(tryContext, threatContextService.findCompositeThreats(playingThreatContext, tryContext));
			} else {
				compositeThreatMap.put(tryContext, threatContextService.findCompositeThreats(opponentThreatContext, tryContext));
			}
			
			validatedThreatMap.put(tryContext, new ArrayList<>());
			pendingThreatMap.put(tryContext, new ArrayList<>());
		}
	}

	private int evaluatePlayingThreatPair(ThreatContext playingThreatContext, ThreatContext opponentThreatContext,
			Map<CompoThreatType, List<Pair<Threat, Threat>>> compositeThreatMap,
			Map<CompoThreatType, List<Pair<Threat, Threat>>> validatedThreatMap,
			Map<CompoThreatType, List<Pair<Threat, Threat>>> pendingThreatMap,
			Pair<Threat, Threat> threatPair,
			CompoThreatType compoThreatType) {
		
		Cell threatCell = retrieveThreatCell(threatPair);
		
		// check for a pending attack for the given compoThreatType, find the blocking moves, compare them with threat cell, count how many => pendingAttackCount
		int pendingAttackCount = pendingAttackCount(compositeThreatMap, compoThreatType, threatCell);
		
		if (pendingAttackCount <= 1) {
			
			// check for the blocking moves of first threat, check for opponent threatTypes corresponding, compare with second threat type/moves => isBlocked
			boolean isPairBlocked = isThreatPairBlocked(playingThreatContext, opponentThreatContext, threatPair, threatCell);
			
			if (pendingAttackCount == 0 && !isPairBlocked) {
				validatedThreatMap.computeIfAbsent(compoThreatType, k -> new ArrayList<>()).add(threatPair);
				return compoThreatType.getPotential();
			} else if (!isPairBlocked || pendingAttackCount == 0) {
				
				if (hasSimilarThreat(pendingThreatMap, compoThreatType, threatPair, true)) {
					return compoThreatType.getPotential() / 2;
				}
				
				pendingThreatMap.computeIfAbsent(compoThreatType, k -> new ArrayList<>()).add(threatPair);
			}
		}
		
		return 0;
	}

	private int evaluatePendingThreatPair(ThreatContext playingThreatContext,
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

	private int pendingAttackCount(Map<CompoThreatType, List<Pair<Threat, Threat>>> compositeThreatMap, CompoThreatType compoThreatType, Cell threatPosition) {
		
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
		
		Set<Cell> blockingCells = new HashSet<>(threatPair.getFirst().getBlockingCells(threatCell));
		
		if (threatPair.getSecond() != null && threatPair.getFirst().getThreatType().equals(threatPair.getSecond().getThreatType())) {
			blockingCells.addAll(threatPair.getSecond().getBlockingCells(threatCell));
		}
		
		for (Cell firstThreatBlockingCell : blockingCells) {
			
			if (opponentThreatContext.getCellToThreatMap().containsKey(firstThreatBlockingCell)) {
				
				for (Entry<ThreatType, List<Threat>> entry : opponentThreatContext.getCellToThreatMap().get(firstThreatBlockingCell).entrySet()) {
					
					ThreatType opponentThreatType = entry.getKey();
					
					if (threatPair.getFirst().getThreatType().getBlockingThreatTypes().contains(opponentThreatType) && !opponentThreatType.getBetterOrEqualThreatTypes().contains(threatPair.getSecond().getThreatType())) {
						
						boolean isBlocked = true;
						
						for (Threat opponentKillingThreat : entry.getValue()) {
							Set<Cell> reBlockingCells = opponentKillingThreat.getBlockingCells(firstThreatBlockingCell);
							
							for (Cell blockingCell : reBlockingCells) {
								
								if (playingThreatContext.getCellToThreatMap().containsKey(blockingCell)) {
									
									for (Entry<ThreatType, List<Threat>> entry2 : playingThreatContext.getCellToThreatMap().get(blockingCell).entrySet()) {
										
										if (threatPair.getSecond().getThreatType().getBetterOrEqualThreatTypes().contains(entry2.getKey())) {
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
		
		return false;
	}

	private boolean hasSimilarThreat(Map<CompoThreatType, List<Pair<Threat, Threat>>> pendingThreatMap, CompoThreatType compoThreatType, Pair<Threat, Threat> threatPair, boolean isPlaying) {
		
		Set<Cell> firstBlockingCells = getBlockingCells(threatPair, isPlaying);
		
		for (CompoThreatType betterThreatType : compoThreatType.getSimilarOrBetterCompoThreatTypes(true, true)) {
			for (Pair<Threat, Threat> previousPair : pendingThreatMap.get(betterThreatType)) {
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
