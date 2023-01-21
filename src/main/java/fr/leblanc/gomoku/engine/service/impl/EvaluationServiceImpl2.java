package fr.leblanc.gomoku.engine.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.CompoThreatType;
import fr.leblanc.gomoku.engine.model.DataWrapper;
import fr.leblanc.gomoku.engine.model.DoubleThreat;
import fr.leblanc.gomoku.engine.model.EngineConstants;
import fr.leblanc.gomoku.engine.model.EvaluationContext;
import fr.leblanc.gomoku.engine.model.Threat;
import fr.leblanc.gomoku.engine.model.ThreatContext;
import fr.leblanc.gomoku.engine.model.ThreatType;
import fr.leblanc.gomoku.engine.service.CheckWinService;
import fr.leblanc.gomoku.engine.service.EvaluationService;
import fr.leblanc.gomoku.engine.service.ThreatContextService;
import fr.leblanc.gomoku.engine.util.Pair;
import fr.leblanc.gomoku.engine.util.cache.L2CacheSupport;
import lombok.extern.apachecommons.CommonsLog;

@Service
@CommonsLog
public class EvaluationServiceImpl2 implements EvaluationService {

	@Autowired
	private ThreatContextService threatContextService;
	
	@Autowired
	private CheckWinService checkWinService;
	
	@Override
	public double computeEvaluation(DataWrapper dataWrapper) {
		
		int playingColor = extractPlayingColor(dataWrapper);
		
		if (L2CacheSupport.isCacheEnabled() && L2CacheSupport.getEvaluationCache().get(playingColor).containsKey(dataWrapper)) {
			return L2CacheSupport.getEvaluationCache().get(playingColor).get(dataWrapper);
		}
		
		double evaluation = internalComputeEvaluation(new EvaluationContext(dataWrapper, playingColor, -1, 0));
		
		if (L2CacheSupport.isCacheEnabled()) {
			L2CacheSupport.getEvaluationCache().get(playingColor).put(new DataWrapper(dataWrapper), evaluation);
		}
		
		return evaluation;
	}

	private int extractPlayingColor(DataWrapper dataWrapper) {
		
		int[][] data = dataWrapper.getData();
		
		int moveCount = 0;
		
		for (int i = 0; i < data[0].length; i++) {
			for (int j = 0; j < data.length; j++) {
				if (data[j][i] != EngineConstants.NONE_COLOR) {
					moveCount++;
				}
			}
		}
		
		return moveCount % 2 == 0 ? EngineConstants.BLACK_COLOR : EngineConstants.WHITE_COLOR;
	}
	
	private double internalComputeEvaluation(EvaluationContext context) {
		
		if (checkWinService.checkWin(context.getDataWrapper(), context.getPlayingColor(), new int[5][2])) {
			return EngineConstants.WIN_EVALUATION;
		}
		
		if (checkWinService.checkWin(context.getDataWrapper(), -context.getPlayingColor(), new int[5][2])) {
			return -EngineConstants.WIN_EVALUATION;
		}
		
		ThreatContext playingThreatContext = threatContextService.computeThreatContext(context.getDataWrapper(), context.getPlayingColor());
		ThreatContext opponentThreatContext = threatContextService.computeThreatContext(context.getDataWrapper(), -context.getPlayingColor());

		return evaluateThreats(playingThreatContext, opponentThreatContext);
		
	}

	private int evaluateThreats(ThreatContext playingThreatContext, ThreatContext opponentThreatContext) {
		
		int evaluation = 0;
		
		Map<CompoThreatType, Map<Cell, Pair<Threat, List<Threat>>>> candidateThreatMap = new HashMap<>();
		
		Map<CompoThreatType, List<Entry<Cell, Pair<Threat, List<Threat>>>>> validatedThreatMap = new HashMap<>();
		
		Map<CompoThreatType, List<Entry<Cell, Pair<Threat, List<Threat>>>>> pendingThreatMap = new HashMap<>();
		
		for (CompoThreatType tryContext : EngineConstants.COMPO_THREAT_TYPES) {
			
			if (tryContext.isPlaying()) {
				candidateThreatMap.put(tryContext, threatContextService.findCompositeThreats(playingThreatContext, tryContext));
			} else {
				candidateThreatMap.put(tryContext, threatContextService.findCompositeThreats(opponentThreatContext, tryContext));
			}
			
			validatedThreatMap.put(tryContext, new ArrayList<>());
			pendingThreatMap.put(tryContext, new ArrayList<>());
		}
		
		for (CompoThreatType compoThreatType : EngineConstants.COMPO_THREAT_TYPES) {
			
			if (compoThreatType.isPlaying()) {
				for (Entry<Cell, Pair<Threat, List<Threat>>> entry : candidateThreatMap.get(compoThreatType).entrySet()) {
					int computePlayingEvaluation = computePlayingEvaluation(opponentThreatContext, validatedThreatMap, pendingThreatMap, entry, compoThreatType);
					evaluation += computePlayingEvaluation;
					if (log.isDebugEnabled() && computePlayingEvaluation != 0) {
						log.debug(computePlayingEvaluation + " FROM " + compoThreatType);
					}
				}
			} else {
				for (Entry<Cell, Pair<Threat, List<Threat>>> entry : candidateThreatMap.get(compoThreatType).entrySet()) {
					int computePendingEvaluation = computePendingEvaluation(playingThreatContext, validatedThreatMap, entry, compoThreatType);
					evaluation += computePendingEvaluation;
					if (log.isDebugEnabled() && computePendingEvaluation != 0) {
						log.debug(computePendingEvaluation + " FROM " + compoThreatType);
					}
				}
			}
			
		}
		
		return evaluation;
	}

	private int computePlayingEvaluation(ThreatContext opponentThreatContext,
			Map<CompoThreatType, List<Entry<Cell, Pair<Threat, List<Threat>>>>> validatedThreatMap,
			Map<CompoThreatType, List<Entry<Cell, Pair<Threat, List<Threat>>>>> pendingThreatMap,
			Entry<Cell, Pair<Threat, List<Threat>>> entry,
			CompoThreatType compoThreatType) {
		
		boolean threatsBlocked = areThreatsBlocked(opponentThreatContext, compoThreatType, entry.getValue());
		
		int pendingAttackCount = pendingAttackCount(validatedThreatMap, compoThreatType, entry.getKey());
		
		if (!threatsBlocked && pendingAttackCount == 0) {
			validatedThreatMap.get(compoThreatType).add(entry);
			return compoThreatType.getPotential();
		} else if (!threatsBlocked && pendingAttackCount == 1 || pendingAttackCount == 0) {
			if (hasSimilarPendingThreat(pendingThreatMap, compoThreatType, entry.getValue(), true) ) {
				return compoThreatType.getPotential() / 2;
			}
			pendingThreatMap.get(compoThreatType).add(entry);
		}
		
		return 0;
	}

	private int computePendingEvaluation(ThreatContext playingThreatContext,
			Map<CompoThreatType, List<Entry<Cell, Pair<Threat, List<Threat>>>>> validatedThreatMap, 
			Entry<Cell, Pair<Threat, List<Threat>>> entry,
			CompoThreatType compoThreatType) {
		int evaluation = 0;
	
		boolean threatsBlocked = areThreatsBlocked(playingThreatContext, compoThreatType, entry.getValue());
		
		int pendingAttackCount = pendingAttackCount(validatedThreatMap, compoThreatType, entry.getKey());
		
		if (!threatsBlocked && pendingAttackCount == 0) {
			if (hasSimilarPendingThreat(validatedThreatMap, compoThreatType, entry.getValue(), false) ) {
				evaluation -= compoThreatType.getPotential();
			}
			validatedThreatMap.get(compoThreatType).add(entry);
		}
		
		return evaluation;
	}

	private boolean areThreatsBlocked(ThreatContext opponentThreatContext, CompoThreatType compoThreatType, Pair<Threat, List<Threat>> threats) {
		
		List<Threat> threatList = new ArrayList<>();
		
		threatList.add(threats.getFirst());
		threatList.addAll(threats.getSecond());
		
		for (Threat threat : threatList) {
			for (ThreatType blockingThreatType : compoThreatType.getBlockingThreatTypes()) {
				if (blockingThreatType.isDoubleType()) {
					for (DoubleThreat blockingThreat : opponentThreatContext.getDoubleThreatTypeToThreatMap().get(blockingThreatType)) {
						if (threat.getBlockingCells().contains(blockingThreat.getTargetCell())) {
							return true;
						}
					}
				} else {
					for (Threat blockingThreat : opponentThreatContext.getThreatTypeToThreatMap().get(blockingThreatType)) {
						if (threat.getBlockingCells().stream().filter(blockingThreat.getEmptyCells()::contains).count() > 0) {
							return true;
						}
					}
				}
			}
		}
		
		return false;
	}

	private int pendingAttackCount(Map<CompoThreatType, List<Entry<Cell, Pair<Threat, List<Threat>>>>> candidateThreatMap, CompoThreatType compoThreatType, Cell threatPosition) {
		int pendingAttackCount = 0;
		
		for (CompoThreatType killingType : compoThreatType.getKillingCompoThreatTypes()) {
			if (candidateThreatMap.get(killingType).stream().filter(c -> !c.getKey().equals(threatPosition)).count() > 0) {
				pendingAttackCount += 1;
			}
		}
		
		return pendingAttackCount;
	}

	private boolean hasSimilarPendingThreat(Map<CompoThreatType, List<Entry<Cell, Pair<Threat, List<Threat>>>>> pendingThreatMap, CompoThreatType compoThreatType, Pair<Threat, List<Threat>> threats, boolean isPlaying) {
		
		Set<Cell> mainBlockingCells = isPlaying ? threats.getFirst().getBlockingCells() : threats.getFirst().getKillingCells();
		
		if (threats.getSecond().isEmpty()) {
			for (CompoThreatType betterThreatType : compoThreatType.getSimilarOrBetterCompoThreatTypes()) {
				for (Entry<Cell, Pair<Threat, List<Threat>>> previousEntry : pendingThreatMap.get(betterThreatType)) {
					
					Set<Cell> previousBlockingCells = getBlockingCells(previousEntry.getValue(), isPlaying);
					
					if (previousBlockingCells.stream().filter(mainBlockingCells::contains).count() == 0) {
						return true;
					}
				}
			}
		} else {
			for (Threat secondThreat : threats.getSecond()) {
				Set<Cell> blockingCells = new HashSet<>();
				blockingCells.addAll(mainBlockingCells);
				blockingCells.addAll(isPlaying ? secondThreat.getBlockingCells() : secondThreat.getKillingCells());
				for (CompoThreatType betterThreatType : compoThreatType.getSimilarOrBetterCompoThreatTypes()) {
					for (Entry<Cell, Pair<Threat, List<Threat>>> previousEntry : pendingThreatMap.get(betterThreatType)) {
						
						Set<Cell> previousBlockingCells = getBlockingCells(previousEntry.getValue(), isPlaying);
						
						if (previousBlockingCells.stream().filter(blockingCells::contains).count() == 0) {
							return true;
						}
					}
				}
			}
		}
		
		return false;
	}
	
	private Set<Cell> getBlockingCells(Pair<Threat, List<Threat>> pair, boolean isPlaying) {
		Set<Cell> killingCells = new HashSet<>();
		
		killingCells.addAll(isPlaying ? pair.getFirst().getBlockingCells() : pair.getFirst().getKillingCells());
		
		for (Threat secondThreat : pair.getSecond()) {
			killingCells.addAll(isPlaying ? secondThreat.getBlockingCells() : secondThreat.getKillingCells());
		}
		
		return killingCells;
	}
}
