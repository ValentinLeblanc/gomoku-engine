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
		
		if (L2CacheSupport.isCacheEnabled()) {
			if (!L2CacheSupport.getEvaluationCache().containsKey(playingColor)) {
				L2CacheSupport.getEvaluationCache().put(playingColor, new HashMap<>());
			}
			if (L2CacheSupport.getEvaluationCache().get(playingColor).containsKey(dataWrapper)) {
				return L2CacheSupport.getEvaluationCache().get(playingColor).get(dataWrapper);
			}
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
		
		for (CompoThreatType tryContext : EngineConstants.TRY_CONTEXTS) {
			
			if (tryContext.isPlaying()) {
				candidateThreatMap.put(tryContext, threatContextService.findCompositeThreats(playingThreatContext, tryContext));
			} else {
				candidateThreatMap.put(tryContext, threatContextService.findCompositeThreats(opponentThreatContext, tryContext));
			}
			
			validatedThreatMap.put(tryContext, new ArrayList<>());
			pendingThreatMap.put(tryContext, new ArrayList<>());
		}
		
		evaluation += computePlayingEvaluation(opponentThreatContext, candidateThreatMap,validatedThreatMap, pendingThreatMap, CompoThreatType.of(ThreatType.THREAT_5, null, true));
		
		evaluation += computePendingEvaluation(playingThreatContext, candidateThreatMap, validatedThreatMap, CompoThreatType.of(ThreatType.THREAT_5, null, false));
		
		evaluation += computePlayingEvaluation(opponentThreatContext, candidateThreatMap,validatedThreatMap, pendingThreatMap, CompoThreatType.of(ThreatType.DOUBLE_THREAT_4, null, true));
		
		evaluation += computePlayingEvaluation(opponentThreatContext, candidateThreatMap,validatedThreatMap, pendingThreatMap, CompoThreatType.of(ThreatType.THREAT_4, ThreatType.THREAT_4, true));
		
		evaluation += computePlayingEvaluation(opponentThreatContext, candidateThreatMap,validatedThreatMap, pendingThreatMap, CompoThreatType.of(ThreatType.THREAT_4, ThreatType.DOUBLE_THREAT_3, true));
		
		evaluation += computePlayingEvaluation(opponentThreatContext, candidateThreatMap,validatedThreatMap, pendingThreatMap, CompoThreatType.of(ThreatType.THREAT_4, ThreatType.DOUBLE_THREAT_2, true));
		
		evaluation += computePendingEvaluation(playingThreatContext, candidateThreatMap, validatedThreatMap, CompoThreatType.of(ThreatType.DOUBLE_THREAT_4, null, false));
		
		evaluation += computePendingEvaluation(playingThreatContext, candidateThreatMap, validatedThreatMap, CompoThreatType.of(ThreatType.THREAT_4, ThreatType.THREAT_4, false));
		
		evaluation += computePendingEvaluation(playingThreatContext, candidateThreatMap, validatedThreatMap, CompoThreatType.of(ThreatType.THREAT_4, ThreatType.DOUBLE_THREAT_3, false));
		
		evaluation += computePlayingEvaluation(opponentThreatContext, candidateThreatMap,validatedThreatMap, pendingThreatMap, CompoThreatType.of(ThreatType.DOUBLE_THREAT_3, ThreatType.DOUBLE_THREAT_3, true));
		
		evaluation += computePlayingEvaluation(opponentThreatContext, candidateThreatMap,validatedThreatMap, pendingThreatMap, CompoThreatType.of(ThreatType.DOUBLE_THREAT_3, ThreatType.DOUBLE_THREAT_2, true));
		
		evaluation += computePendingEvaluation(playingThreatContext, candidateThreatMap, validatedThreatMap, CompoThreatType.of(ThreatType.DOUBLE_THREAT_3, ThreatType.DOUBLE_THREAT_3, false));
		
		evaluation += computePendingEvaluation(playingThreatContext, candidateThreatMap, validatedThreatMap, CompoThreatType.of(ThreatType.DOUBLE_THREAT_3, ThreatType.DOUBLE_THREAT_2, false));
		
		evaluation += computePlayingEvaluation(opponentThreatContext, candidateThreatMap,validatedThreatMap, pendingThreatMap, CompoThreatType.of(ThreatType.DOUBLE_THREAT_2, ThreatType.DOUBLE_THREAT_2, true));
		
		evaluation += computePendingEvaluation(playingThreatContext, candidateThreatMap, validatedThreatMap, CompoThreatType.of(ThreatType.DOUBLE_THREAT_2, ThreatType.DOUBLE_THREAT_2, false));
		
		return evaluation;
	}
	
	private int computeEntryPlayingEvaluation(ThreatContext opponentThreatContext,
			Map<CompoThreatType, Map<Cell, Pair<Threat, List<Threat>>>> candidateThreatMap,
			Map<CompoThreatType, List<Entry<Cell, Pair<Threat, List<Threat>>>>> validatedThreatMap,
			Map<CompoThreatType, List<Entry<Cell, Pair<Threat, List<Threat>>>>> pendingThreatMap,
			Entry<Cell, Pair<Threat, List<Threat>>> entry,
			CompoThreatType compoThreatType) {
		
		Cell threatPosition = entry.getKey();
		
		Pair<Threat, List<Threat>> threats = entry.getValue();

		Threat mainThreat = threats.getFirst();
		
		ThreatType mainThreatBlockingThreatType = getBlockingThreatType(opponentThreatContext, mainThreat);
		
		boolean threatsNotBlocked = mainThreatBlockingThreatType == null && threats.getSecond().isEmpty();
		
		for (Threat secondThreat : threats.getSecond()) {
			ThreatType secondThreatBlockingThreatType = getBlockingThreatType(opponentThreatContext, secondThreat);
			
			boolean isSecondThreatBlocked = secondThreatBlockingThreatType != null && !secondThreatBlockingThreatType.equals(secondThreat.getThreatType());
			
			if (!isSecondThreatBlocked && (mainThreatBlockingThreatType == null || mainThreatBlockingThreatType.equals(secondThreat.getThreatType()))) {
				threatsNotBlocked = true;
				break;
			}
		}
		
		int pendingAttackCount = pendingAttackCount(candidateThreatMap, compoThreatType, threatPosition);
		
		if (threatsNotBlocked && pendingAttackCount == 0) {
			validatedThreatMap.get(compoThreatType).add(entry);
			return compoThreatType.getPotential();
		} else if (threatsNotBlocked && pendingAttackCount == 1 || pendingAttackCount == 0) {
			Set<Cell> entryBlockingCells = new HashSet<>();

			entryBlockingCells.addAll(mainThreat.getBlockingCells());
			
			for (Threat secondThreat : threats.getSecond()) {
				entryBlockingCells.addAll(secondThreat.getBlockingCells());
			}
			
			if (hasSimilarPendingThreat(pendingThreatMap, compoThreatType, entryBlockingCells) ) {
				return compoThreatType.getPotential() / 2;
			} else {
				pendingThreatMap.get(compoThreatType).add(entry);
			}
		} else {
			return 0;
		}
		
		return 0;
	}

	private int pendingAttackCount(Map<CompoThreatType, Map<Cell, Pair<Threat, List<Threat>>>> candidateThreatMap, CompoThreatType compoThreatType, Cell threatPosition) {
		int pendingAttackCount = 0;
		
		if (!CompoThreatType.of(ThreatType.THREAT_5, null, true).equals(compoThreatType)) {
			if (candidateThreatMap.get(CompoThreatType.of(ThreatType.THREAT_5, null, false)).keySet().stream().filter(c -> !c.equals(threatPosition)).count() > 0) {
				pendingAttackCount += 1;
			}
			
			if (!CompoThreatType.of(ThreatType.DOUBLE_THREAT_4, null, true).equals(compoThreatType) && !ThreatType.THREAT_4.equals(compoThreatType.getThreatType1())) {
				
				if(candidateThreatMap.get(CompoThreatType.of(ThreatType.THREAT_4, ThreatType.THREAT_4, false)).keySet().stream().filter(c -> !c.equals(threatPosition)).count() > 0) {
					pendingAttackCount += 1;
				}
				if (candidateThreatMap.get(CompoThreatType.of(ThreatType.DOUBLE_THREAT_4, null, false)).keySet().stream().filter(c -> !c.equals(threatPosition)).count() > 0) {
					pendingAttackCount += 1;
				}
				if (candidateThreatMap.get(CompoThreatType.of(ThreatType.THREAT_4, ThreatType.DOUBLE_THREAT_3, false)).keySet().stream().filter(c -> !c.equals(threatPosition)).count() > 0) {
					pendingAttackCount += 1;
				}
				
				if (!ThreatType.DOUBLE_THREAT_3.equals(compoThreatType.getThreatType1())) {
					if(candidateThreatMap.get(CompoThreatType.of(ThreatType.DOUBLE_THREAT_3, ThreatType.DOUBLE_THREAT_3, false)).keySet().stream().filter(c -> !c.equals(threatPosition)).count() > 0) {
						pendingAttackCount += 1;
					}
					if(candidateThreatMap.get(CompoThreatType.of(ThreatType.DOUBLE_THREAT_3, ThreatType.DOUBLE_THREAT_3, false)).keySet().stream().filter(c -> !c.equals(threatPosition)).count() > 0) {
						pendingAttackCount += 1;
					}
				}
			}
		}
		
		return pendingAttackCount;
	}

	private ThreatType getBlockingThreatType(ThreatContext opponentThreatContext, Threat threat) {
		for (ThreatType blockingThreatType : threat.getThreatType().getBlockingThreatTypes()) {
			if (blockingThreatType.isDoubleType()) {
				for (DoubleThreat blockingThreat : opponentThreatContext.getDoubleThreatTypeToThreatMap().get(blockingThreatType)) {
					if (threat.getBlockingCells().contains(blockingThreat.getTargetCell())) {
						return blockingThreatType;
					}
				}
			} else {
				for (Threat blockingThreat : opponentThreatContext.getThreatTypeToThreatMap().get(blockingThreatType)) {
					if (threat.getBlockingCells().stream().filter(blockingThreat.getEmptyCells()::contains).count() > 0) {
						return blockingThreatType;
					}
				}
			}
		}
		
		return null;
	}
	
	private int computePlayingEvaluation(ThreatContext opponentThreatContext,
			Map<CompoThreatType, Map<Cell, Pair<Threat, List<Threat>>>> candidateThreatMap,
			Map<CompoThreatType, List<Entry<Cell, Pair<Threat, List<Threat>>>>> validatedThreatMap,
			Map<CompoThreatType, List<Entry<Cell, Pair<Threat, List<Threat>>>>> pendingThreatMap,
			CompoThreatType compoThreatType) {
		
		int evaluation = 0;
		
		for (Entry<Cell, Pair<Threat, List<Threat>>> entry : candidateThreatMap.get(compoThreatType).entrySet()) {
			evaluation += computeEntryPlayingEvaluation(opponentThreatContext, candidateThreatMap, validatedThreatMap, pendingThreatMap, entry, compoThreatType);
		}
		
		return evaluation;
	}

	private int computePendingEvaluation(ThreatContext playingThreatContext,
			Map<CompoThreatType, Map<Cell, Pair<Threat, List<Threat>>>> candidateThreatMap,
		
			Map<CompoThreatType, List<Entry<Cell, Pair<Threat, List<Threat>>>>> validatedThreatMap, CompoThreatType compoThreatType) {
		int evaluation = 0;
		
		CompoThreatType t5 = CompoThreatType.of(ThreatType.THREAT_5, null, true);
		CompoThreatType _t5 = CompoThreatType.of(ThreatType.THREAT_5, null, false);
		CompoThreatType dt4 = CompoThreatType.of(ThreatType.DOUBLE_THREAT_4, null, true);
		CompoThreatType _dt4 = CompoThreatType.of(ThreatType.DOUBLE_THREAT_4, null, false);
		CompoThreatType _t4t4 = CompoThreatType.of(ThreatType.THREAT_4, ThreatType.THREAT_4, false);
		CompoThreatType _t4dt3 = CompoThreatType.of(ThreatType.THREAT_4, ThreatType.DOUBLE_THREAT_3, false);
		CompoThreatType t4t4 = CompoThreatType.of(ThreatType.THREAT_4, ThreatType.THREAT_4, true);
		CompoThreatType t4dt3 = CompoThreatType.of(ThreatType.THREAT_4, ThreatType.DOUBLE_THREAT_3, true);
		CompoThreatType dt3dt3 = CompoThreatType.of(ThreatType.DOUBLE_THREAT_3, ThreatType.DOUBLE_THREAT_3, true);
		CompoThreatType dt3dt2 = CompoThreatType.of(ThreatType.DOUBLE_THREAT_3, ThreatType.DOUBLE_THREAT_2, true);

		for (Entry<Cell, Pair<Threat, List<Threat>>> entry : candidateThreatMap.get(compoThreatType).entrySet()) {
			
			boolean hasT5Attack = !validatedThreatMap.get(t5).isEmpty();
			
			if (!hasT5Attack) {
				boolean has_T5 = false;
				
				Set<Cell> entryKillingCells = getKillingCells(entry.getValue());
						
				for (Entry<Cell, Pair<Threat, List<Threat>>> previousEntry : validatedThreatMap.get(_t5)) {
					
					Set<Cell> previousKillingCells = getKillingCells(previousEntry.getValue());
					
					if (previousKillingCells.stream().filter(entryKillingCells::contains).count() == 0) {
						has_T5 = true;
						break;
					}
				}
				
				if (has_T5) {
					evaluation -= compoThreatType.getPotential();
					validatedThreatMap.get(compoThreatType).add(entry);
				} else if (isT5(compoThreatType.getThreatType1())) {
					validatedThreatMap.get(compoThreatType).add(entry);
				} else {
					
					if (!hasT4Counter(playingThreatContext, entryKillingCells)) {
						
						boolean hasT4Attack = !validatedThreatMap.get(dt4).isEmpty() || !validatedThreatMap.get(t4t4).isEmpty() || !validatedThreatMap.get(t4dt3).isEmpty();
						
						if (!hasT4Attack) {
							
							if (hasAttack(validatedThreatMap, _dt4, entryKillingCells) || hasAttack(validatedThreatMap, _t4t4, entryKillingCells) || hasAttack(validatedThreatMap, _t4dt3, entryKillingCells)) {
								evaluation -= compoThreatType.getPotential();
								validatedThreatMap.get(compoThreatType).add(entry);
							} else if (isT4(compoThreatType.getThreatType1())) {
								validatedThreatMap.get(compoThreatType).add(entry);
							} else {
								boolean hasSimilarThreat = false;
								
								for (Entry<Cell, Pair<Threat, List<Threat>>> previousEntry : validatedThreatMap.get(compoThreatType)) {
									
									Set<Cell> previousKillingCells = getKillingCells(previousEntry.getValue());
									
									if (previousKillingCells.stream().filter(entryKillingCells::contains).count() == 0) {
										hasSimilarThreat = true;
										break;
									}
								}
								
								if (hasSimilarThreat) {
									evaluation -= compoThreatType.getPotential();
									validatedThreatMap.get(compoThreatType).add(entry);
								} else {
									boolean hasDT3Attack = !validatedThreatMap.get(dt3dt3).isEmpty() || !validatedThreatMap.get(dt3dt2).isEmpty();
									
									if (!hasDT3Attack) {
										validatedThreatMap.get(compoThreatType).add(entry);
									}
								}
							}
							
						}
					}
				}
			}
		}
		
		if (evaluation > 0 && log.isDebugEnabled()) {
			log.debug(compoThreatType + " : " + evaluation);
		}
		
		return evaluation;
	}

	private boolean hasSimilarPendingThreat(Map<CompoThreatType, List<Entry<Cell, Pair<Threat, List<Threat>>>>> pendingThreatMap, CompoThreatType compoThreatType, Set<Cell> entryBlockingCells) {
		for (Entry<Cell, Pair<Threat, List<Threat>>> previousEntry : pendingThreatMap.get(compoThreatType)) {
			
			Set<Cell> previousBlockingCells = getBlockingCells(previousEntry.getValue());
			
			if (previousBlockingCells.stream().filter(entryBlockingCells::contains).count() == 0) {
				return true;
			}
		}
		
		return false;
	}
	
	private boolean hasT4Counter(ThreatContext threatContext, Set<Cell> cells) {
		for (Threat threat : threatContext.getThreatTypeToThreatMap().get(ThreatType.THREAT_4)) {
			if (threat.getEmptyCells().stream().filter(cells::contains).count() > 0) {
				return true;
			}
		}
		return false;
	}
		
	private boolean hasAttack(Map<CompoThreatType, List<Entry<Cell, Pair<Threat, List<Threat>>>>> validatedThreatMap,
			CompoThreatType _dt4, Set<Cell> entryKillingCells) {
		for (Entry<Cell, Pair<Threat, List<Threat>>> previousEntry : validatedThreatMap.get(_dt4)) {
			
			Set<Cell> previousKillingCells = getKillingCells(previousEntry.getValue());
			
			if (previousKillingCells.stream().filter(entryKillingCells::contains).count() == 0) {
				return true;
			}
		}
		return false;
	}

	private boolean isT4(ThreatType threatType) {
		return threatType.equals(ThreatType.THREAT_4) || threatType.equals(ThreatType.DOUBLE_THREAT_4);
	}
	
	private boolean isT5(ThreatType threatType) {
		return threatType.equals(ThreatType.THREAT_5);
	}
	
	private Set<Cell> getKillingCells(Pair<Threat, List<Threat>> pair) {
		Set<Cell> killingCells = new HashSet<>();
		
		killingCells.addAll(pair.getFirst().getKillingCells());
		
		for (Threat secondThreat : pair.getSecond()) {
			killingCells.addAll(secondThreat.getKillingCells());
		}
		
		return killingCells;
	}
	
	private Set<Cell> getBlockingCells(Pair<Threat, List<Threat>> pair) {
		Set<Cell> killingCells = new HashSet<>();
		
		killingCells.addAll(pair.getFirst().getBlockingCells());
		
		for (Threat secondThreat : pair.getSecond()) {
			killingCells.addAll(secondThreat.getBlockingCells());
		}
		
		return killingCells;
	}
}
