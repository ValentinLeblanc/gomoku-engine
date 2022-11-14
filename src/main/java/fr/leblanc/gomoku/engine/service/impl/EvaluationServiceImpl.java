package fr.leblanc.gomoku.engine.service.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.DataWrapper;
import fr.leblanc.gomoku.engine.model.DoubleThreat;
import fr.leblanc.gomoku.engine.model.EngineConstants;
import fr.leblanc.gomoku.engine.model.Threat;
import fr.leblanc.gomoku.engine.model.ThreatContext;
import fr.leblanc.gomoku.engine.model.ThreatType;
import fr.leblanc.gomoku.engine.service.CheckWinService;
import fr.leblanc.gomoku.engine.service.EvaluationService;
import fr.leblanc.gomoku.engine.service.ThreatContextService;

@Service
public class EvaluationServiceImpl implements EvaluationService {

	private static final int MAX_EVALUATION_DEPTH = 3;
	
	@Autowired
	private ThreatContextService threatContextService;
	
	@Autowired
	private CheckWinService checkWinService;
	
	@Override
	public double computeEvaluation(DataWrapper dataWrapper, int playingColor) {
		return internalComputeEvaluation(dataWrapper, playingColor, 0);
	}

	private int internalComputeEvaluation(DataWrapper dataWrapper, int playingColor, int depth) {
		
		if (depth >= MAX_EVALUATION_DEPTH) {
			return 0;
		}
		
		if (checkWinService.checkWin(dataWrapper, playingColor, new int[5][2])) {
			return EngineConstants.WIN_EVALUATION;
		}
		
		if (checkWinService.checkWin(dataWrapper, -playingColor, new int[5][2])) {
			return -EngineConstants.WIN_EVALUATION;
		}
		
		ThreatContext playingThreatContext = threatContextService.computeThreatContext(dataWrapper.getData(), playingColor);
		
		ThreatContext opponentThreatContext = threatContextService.computeThreatContext(dataWrapper.getData(), -playingColor);

		return evaluateThreats(dataWrapper, playingColor, playingThreatContext, opponentThreatContext, true, depth, ThreatType.THREAT_5, null);
		
	}

	private int evaluateThreats(DataWrapper dataWrapper, int playingColor, ThreatContext playingThreatContext, ThreatContext opponentThreatContext, boolean isFreeToAttack, int depth, ThreatType threatType1, ThreatType threatType2) {
			
		AtomicInteger evaluation = new AtomicInteger(0);
		
		if (ThreatType.THREAT_5.equals(threatType1)) {
			Map<Threat, Set<Cell>> threats = getEffectiveThreats(playingThreatContext, opponentThreatContext, ThreatType.THREAT_5, null);
			if (isFreeToAttack) {
				if (!threats.isEmpty()) {
					threats.keySet().stream().forEach(t -> threats.get(t).stream().forEach(c -> evaluation.addAndGet(EngineConstants.THREAT_5_POTENTIAL)));
				} else {
					evaluation.addAndGet(evaluateThreats(dataWrapper, playingColor, opponentThreatContext, playingThreatContext, false, depth, ThreatType.THREAT_5, null));
				}
			} else {
				if (!threats.isEmpty()) {
					evaluation.addAndGet(-evaluateOpponentThreat(dataWrapper, playingColor, depth + 1, threats));
				} else {
					evaluation.addAndGet(evaluateThreats(dataWrapper, playingColor, opponentThreatContext, playingThreatContext, true, depth , ThreatType.DOUBLE_THREAT_4, null));
				}
			}
		} else if (ThreatType.DOUBLE_THREAT_4.equals(threatType1)) {
			Map<Threat, Set<Cell>> threats = getEffectiveThreats(playingThreatContext, opponentThreatContext, ThreatType.DOUBLE_THREAT_4, null);
			if (isFreeToAttack) {
				if (!threats.isEmpty()) {
					threats.keySet().stream().forEach(t -> threats.get(t).stream().forEach(c -> evaluation.addAndGet(EngineConstants.DOUBLE_THREAT_4_POTENTIAL)));
				} else {
					evaluation.addAndGet(evaluateThreats(dataWrapper, playingColor, playingThreatContext, opponentThreatContext, true, depth, ThreatType.THREAT_4, ThreatType.THREAT_4));
				}
			} else {
				if (!threats.isEmpty()) {
					evaluation.addAndGet(-evaluateOpponentThreat(dataWrapper, playingColor, depth + 1, threats));
				} else {
					evaluation.addAndGet(evaluateThreats(dataWrapper, playingColor, playingThreatContext, opponentThreatContext, false, depth, ThreatType.THREAT_4, ThreatType.THREAT_4));
				}
			}
		} else if (ThreatType.THREAT_4.equals(threatType1)) {
			if (ThreatType.THREAT_4.equals(threatType2)) {
				Map<Threat, Set<Cell>> threats = getEffectiveThreats(playingThreatContext, opponentThreatContext, ThreatType.THREAT_4, ThreatType.THREAT_4);
				if (isFreeToAttack) {
					
					if (!threats.isEmpty()) {
						threats.keySet().stream().forEach(t -> threats.get(t).stream().forEach(c -> evaluation.addAndGet(EngineConstants.DOUBLE_THREAT_4_POTENTIAL)));
					} else {
						evaluation.addAndGet(evaluateThreats(dataWrapper, playingColor, playingThreatContext, opponentThreatContext, true, depth, ThreatType.THREAT_4, ThreatType.DOUBLE_THREAT_3));
					}
				} else {
					if (!threats.isEmpty()) {
						evaluation.addAndGet(-evaluateOpponentThreat(dataWrapper, playingColor, depth + 1, threats));
					} else {
						evaluation.addAndGet(evaluateThreats(dataWrapper, playingColor, playingThreatContext, opponentThreatContext, false, depth, ThreatType.THREAT_4, ThreatType.DOUBLE_THREAT_3));
					}
				}
			} else if (ThreatType.DOUBLE_THREAT_3.equals(threatType2)) {
				Map<Threat, Set<Cell>> threats = getEffectiveThreats(playingThreatContext, opponentThreatContext, ThreatType.THREAT_4, ThreatType.DOUBLE_THREAT_3);
				if (isFreeToAttack) {
					if (!threats.isEmpty()) {
						threats.keySet().stream().forEach(t -> threats.get(t).stream().forEach(c -> evaluation.addAndGet(EngineConstants.THREAT_4_DOUBLE_THREAT_3_POTENTIAL)));
					} else {
						evaluation.addAndGet(evaluateThreats(dataWrapper, playingColor, opponentThreatContext, playingThreatContext, false, depth, ThreatType.DOUBLE_THREAT_4, null));
					}
				} else {
					if (!threats.isEmpty()) {
						evaluation.addAndGet(-evaluateOpponentThreat(dataWrapper, playingColor, depth + 1, threats));
					} else {
						evaluation.addAndGet(evaluateThreats(dataWrapper, playingColor, opponentThreatContext, playingThreatContext, true, depth, ThreatType.DOUBLE_THREAT_3, ThreatType.DOUBLE_THREAT_3));
					}
				}

			}
		} else if (ThreatType.DOUBLE_THREAT_3.equals(threatType1)) {
			if (ThreatType.DOUBLE_THREAT_3.equals(threatType2)) {
				Map<Threat, Set<Cell>> threats = getEffectiveThreats(playingThreatContext, opponentThreatContext, ThreatType.DOUBLE_THREAT_3, ThreatType.DOUBLE_THREAT_3);
				if (isFreeToAttack) {
					if (!threats.isEmpty()) {
						threats.keySet().stream().forEach(t -> threats.get(t).stream().forEach(c -> evaluation.addAndGet(EngineConstants.DOUBLE_THREAT_3_DOUBLE_THREAT_3_POTENTIAL)));
					} else {
						evaluation.addAndGet(evaluateThreats(dataWrapper, playingColor, opponentThreatContext, playingThreatContext, false, depth, ThreatType.DOUBLE_THREAT_3, ThreatType.DOUBLE_THREAT_3));
					}
				} else {
					if (!threats.isEmpty()) {
						evaluation.addAndGet(-evaluateOpponentThreat(dataWrapper, playingColor, depth + 1, threats));
					} else {
						evaluation.addAndGet(evaluateThreats(dataWrapper, playingColor, opponentThreatContext, playingThreatContext, true, depth, ThreatType.DOUBLE_THREAT_3, ThreatType.DOUBLE_THREAT_2));
					}
				}
			} else if (ThreatType.DOUBLE_THREAT_2.equals(threatType2)) {
				Map<Threat, Set<Cell>> threats = getEffectiveThreats(playingThreatContext, opponentThreatContext, ThreatType.DOUBLE_THREAT_3, ThreatType.DOUBLE_THREAT_2);
				if (isFreeToAttack) {
					if (!threats.isEmpty()) {
						threats.keySet().stream().forEach(t -> threats.get(t).stream().forEach(c -> evaluation.addAndGet(EngineConstants.DOUBLE_THREAT_3_DOUBLE_THREAT_2_POTENTIAL)));
					} else {
						evaluation.addAndGet(evaluateThreats(dataWrapper, playingColor, opponentThreatContext, playingThreatContext, false, depth, ThreatType.DOUBLE_THREAT_3, ThreatType.DOUBLE_THREAT_2));
					}
				} else {
					if (!threats.isEmpty()) {
						evaluation.addAndGet(-evaluateOpponentThreat(dataWrapper, playingColor, depth + 1, threats));
					} else {
						evaluation.addAndGet(evaluateThreats(dataWrapper, playingColor, opponentThreatContext, playingThreatContext, true, depth, ThreatType.DOUBLE_THREAT_2, ThreatType.DOUBLE_THREAT_2));
					}
				}
			}
		}
		
		return evaluation.intValue();
	}

	private int evaluateOpponentThreat(DataWrapper dataWrapper, int playingColor, int depth, Map<Threat, Set<Cell>> opponentEffectiveThreats) {
	
		int maxEval = Integer.MIN_VALUE;

		for (Threat threat : opponentEffectiveThreats.keySet()) {
		
			int minEval = Integer.MAX_VALUE;
			
			Set<Cell> cellsToEval = threat instanceof DoubleThreat doubleThreat ? doubleThreat.getBlockingCells() : threat.getEmptyCells();
			
			if (threat instanceof DoubleThreat doubleThreat) {
				dataWrapper.addMove(doubleThreat.getTargetCell(), playingColor);
				
				int eval = internalComputeEvaluation(dataWrapper, -playingColor, depth);
				
				dataWrapper.removeMove(doubleThreat.getTargetCell());
				
				if (eval < minEval) {
					minEval = eval;
				}
			}
			
			for (Cell threatCell : cellsToEval) {
				dataWrapper.addMove(threatCell, playingColor);
				
				int eval = internalComputeEvaluation(dataWrapper, -playingColor, depth);
				
				dataWrapper.removeMove(threatCell);

				if (eval < minEval) {
					minEval = eval;
					if (minEval <= maxEval) {
						break;
					}
				}
			}
			
			if (minEval > maxEval) {
				maxEval = minEval;
			}
		}
		
		return maxEval;
	}
	
	private Map<Threat, Set<Cell>> getEffectiveThreats(ThreatContext playingThreatContext, ThreatContext opponentThreatContext, ThreatType threatType, ThreatType secondThreatType) {
		Map<Threat, Set<Cell>> map = new HashMap<>();
		
		if (threatType == ThreatType.THREAT_5) {
			
			if (!playingThreatContext.getThreatTypeToThreatMap().get(ThreatType.THREAT_5).isEmpty()) {
				map.put(playingThreatContext.getThreatTypeToThreatMap().get(ThreatType.THREAT_5).get(0), playingThreatContext.getThreatTypeToThreatMap().get(ThreatType.THREAT_5).get(0).getEmptyCells());
			}
			
		} else if (threatType == ThreatType.DOUBLE_THREAT_4) {
			for (DoubleThreat threat : playingThreatContext.getDoubleThreatTypeToThreatMap().get(ThreatType.DOUBLE_THREAT_4)) {
				Cell playingCell = threat.getTargetCell();
				if (!hasT5Counter(playingCell, opponentThreatContext)) {
					map.put(threat, Set.of(threat.getTargetCell()));
				}
			}
		} else if (threatType == ThreatType.THREAT_4) {
			
			Set<Threat> visitedThreats = new HashSet<>();
			
			for (Threat threat : playingThreatContext.getThreatTypeToThreatMap().get(ThreatType.THREAT_4)) {
				visitedThreats.add(threat);
				for (Cell playingCell : threat.getEmptyCells()) {
					
					if (!hasT5Counter(playingCell, opponentThreatContext)) {
						
						if (ThreatType.THREAT_4.equals(secondThreatType)) {
							
							// find threat 4
							if (playingThreatContext.getCellToThreatMap().get(playingCell).get(ThreatType.THREAT_4) != null) {
								
								if (playingThreatContext.getCellToThreatMap().get(playingCell).get(ThreatType.THREAT_4).stream().anyMatch(t -> !visitedThreats.contains(t)  && !threat.getPlainCells().containsAll(t.getPlainCells()) && !threat.getEmptyCells().containsAll(t.getEmptyCells()))) {
									map.put(threat, Set.of(playingCell));
								}
								
							}
						} else if (ThreatType.DOUBLE_THREAT_3.equals(secondThreatType)) {
							Cell blockingCell = threat.getEmptyCells().stream().filter(c -> !c.equals(playingCell)).findFirst().orElseThrow();
							
							if (!hasT4Counter(blockingCell, opponentThreatContext)) {
								
								long count = playingThreatContext.getDoubleThreatTypeToThreatMap().get(ThreatType.DOUBLE_THREAT_3).stream().filter(t -> !threat.getPlainCells().containsAll(t.getPlainCells()) && t.getTargetCell().equals(playingCell)).count();

								if (count > 0) {
									map.put(threat, Set.of(playingCell));
								}
							}
						}
					}
				}
			}
		} else if (threatType == ThreatType.DOUBLE_THREAT_3) {
			Set<Threat> visitedThreats = new HashSet<>();
			
			for (DoubleThreat threat : playingThreatContext.getDoubleThreatTypeToThreatMap().get(ThreatType.DOUBLE_THREAT_3)) {
				visitedThreats.add(threat);
				Cell playingCell = threat.getTargetCell();
				if (!hasT5Counter(playingCell, opponentThreatContext)) {
					
					if (threat.getBlockingCells().stream().allMatch(blockingCell -> !hasT4Counter(blockingCell, opponentThreatContext))) {
						
						if (ThreatType.DOUBLE_THREAT_3.equals(secondThreatType)) {
							long count = playingThreatContext.getDoubleThreatTypeToThreatMap().get(ThreatType.DOUBLE_THREAT_3).stream().filter(t -> !threat.getPlainCells().containsAll(t.getPlainCells()) && t.getTargetCell().equals(threat.getTargetCell())).count();
							
							if (count > 0) {
								map.put(threat, Set.of(playingCell));
							}
						} else if (ThreatType.DOUBLE_THREAT_2.equals(secondThreatType)) {
								
							if (threat.getBlockingCells().stream().allMatch(blockingCell -> !hasDT3Counter(blockingCell, opponentThreatContext))) {
								
								long count = playingThreatContext.getDoubleThreatTypeToThreatMap().get(ThreatType.DOUBLE_THREAT_2).stream().filter(t -> !threat.getPlainCells().containsAll(t.getPlainCells()) && t.getTargetCell().equals(playingCell)).count();
								
								if (count > 0) {
									map.put(threat, Set.of(playingCell));
								}
							}
							
						}
						
					}
					
				}
			}
		}
		
		return map;
	}

	private boolean hasDT3Counter(Cell blockingCell, ThreatContext opponentThreatContext) {
		return opponentThreatContext.getDoubleThreatTypeToThreatMap().get(ThreatType.DOUBLE_THREAT_3).stream().anyMatch(t -> t.getTargetCell().equals(blockingCell));
	}

	private boolean hasT4Counter(Cell blockingCell, ThreatContext opponentThreatContext) {
		return opponentThreatContext.getThreatTypeToThreatMap().get(ThreatType.THREAT_4).stream().anyMatch(t -> t.getEmptyCells().contains(blockingCell));
	}

	private boolean hasT5Counter(Cell playingCell, ThreatContext opponentThreatContext) {
		return opponentThreatContext.getThreatTypeToThreatMap().get(ThreatType.THREAT_5).stream().anyMatch(t -> !t.getEmptyCells().contains(playingCell));
	}

}
