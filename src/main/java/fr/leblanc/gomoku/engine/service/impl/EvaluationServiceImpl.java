package fr.leblanc.gomoku.engine.service.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.leblanc.gomoku.engine.model.Cell;
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

//@Service
public class EvaluationServiceImpl implements EvaluationService {

	private static final int MAX_EVALUATION_DEPTH = 3;
	
	@Autowired
	private ThreatContextService threatContextService;
	
	@Autowired
	private CheckWinService checkWinService;
	
	@Override
	public double computeEvaluation(DataWrapper dataWrapper, int playingColor) {
		return internalComputeEvaluation(dataWrapper, playingColor, new EvaluationContext(playingColor)).getEvaluation();
	}

	private EvaluationContext internalComputeEvaluation(DataWrapper dataWrapper, int playingColor, EvaluationContext context) {
		
		if (context.getDepth() == MAX_EVALUATION_DEPTH) {
			return context;
		}
		
		if (checkWinService.checkWin(dataWrapper, playingColor, new int[5][2])) {
			context.setEvaluation(EngineConstants.WIN_EVALUATION);
			return context;
		}
		
		if (checkWinService.checkWin(dataWrapper, -playingColor, new int[5][2])) {
			context.setEvaluation(-EngineConstants.WIN_EVALUATION);
			return context;
		}
		
		ThreatContext playingThreatContext = threatContextService.computeThreatContext(dataWrapper.getData(), playingColor);
		
		ThreatContext opponentThreatContext = threatContextService.computeThreatContext(dataWrapper.getData(), -playingColor);

		Map<Threat, Set<Cell>> playingThreat5 = getEffectiveThreats(playingThreatContext, opponentThreatContext, ThreatType.THREAT_5, null);
		
		// THREAT 5
		if (!playingThreat5.isEmpty()) {
			playingThreat5.keySet().stream().forEach(t -> playingThreat5.get(t).stream().forEach(c -> context.addContribution(c, playingColor, EngineConstants.THREAT_5_POTENTIAL)));
		} else {
			Map<Threat, Set<Cell>> opponentEffectiveThreats = getEffectiveThreats(opponentThreatContext, playingThreatContext, ThreatType.THREAT_5, null);
			if (!opponentEffectiveThreats.isEmpty()) {
				evaluateOpponentThreat(dataWrapper, context, opponentEffectiveThreats);
			} else {
				// THREAT 4
				Map<Threat, Set<Cell>> playingDoubleThreat4 = getEffectiveThreats(playingThreatContext, opponentThreatContext, ThreatType.DOUBLE_THREAT_4, null);
				
				if (!playingDoubleThreat4.isEmpty()) {
					playingDoubleThreat4.keySet().stream().forEach(t -> playingDoubleThreat4.get(t).stream().forEach(c -> context.addContribution(c, playingColor, EngineConstants.DOUBLE_THREAT_4_POTENTIAL)));
				}
				
				Map<Threat, Set<Cell>> playingThreat4Threat4 = getEffectiveThreats(playingThreatContext, opponentThreatContext, ThreatType.THREAT_4, ThreatType.THREAT_4);
				
				if (!playingThreat4Threat4.isEmpty()) {
					playingThreat4Threat4.keySet().stream().forEach(t -> playingThreat4Threat4.get(t).stream().forEach(c -> context.addContribution(c, playingColor, EngineConstants.DOUBLE_THREAT_4_POTENTIAL)));
				}
				
				Map<Threat, Set<Cell>> playingThreat4DoubleThreat3 = getEffectiveThreats(playingThreatContext, opponentThreatContext, ThreatType.THREAT_4, ThreatType.DOUBLE_THREAT_3);
				
				if (!playingThreat4DoubleThreat3.isEmpty()) {
					playingThreat4DoubleThreat3.keySet().stream().forEach(t -> playingThreat4DoubleThreat3.get(t).stream().forEach(c -> context.addContribution(c, playingColor, EngineConstants.THREAT_4_DOUBLE_THREAT_3_POTENTIAL)));
				}
				
				if (playingDoubleThreat4.isEmpty() && playingThreat4Threat4.isEmpty() && playingThreat4DoubleThreat3.isEmpty()) {
					Map<Threat, Set<Cell>> opponentDoubleThreat4 = getEffectiveThreats(opponentThreatContext, playingThreatContext, ThreatType.DOUBLE_THREAT_4, null);
					if (!opponentDoubleThreat4.isEmpty()) {
						evaluateOpponentThreat(dataWrapper, context, opponentDoubleThreat4);
					}
					
					Map<Threat, Set<Cell>> opponentThreat4Threat4 = getEffectiveThreats(opponentThreatContext, playingThreatContext, ThreatType.THREAT_4, ThreatType.THREAT_4);
					if (!opponentThreat4Threat4.isEmpty()) {
						evaluateOpponentThreat(dataWrapper, context, opponentThreat4Threat4);
					}
					
					Map<Threat, Set<Cell>> opponentThreat4DoubleThreat3 = getEffectiveThreats(opponentThreatContext, playingThreatContext, ThreatType.THREAT_4, ThreatType.DOUBLE_THREAT_3);
					if (!opponentThreat4DoubleThreat3.isEmpty()) {
						evaluateOpponentThreat(dataWrapper, context, opponentThreat4DoubleThreat3);
					}
					
					if (opponentDoubleThreat4.isEmpty() && opponentThreat4Threat4.isEmpty() && opponentThreat4DoubleThreat3.isEmpty()) {
						
						// DOUBLE THREAT 3
						Map<Threat, Set<Cell>> playingDoubleThreat3DoubleThreat3 = getEffectiveThreats(playingThreatContext, opponentThreatContext, ThreatType.DOUBLE_THREAT_3, ThreatType.DOUBLE_THREAT_3);
						if (!playingDoubleThreat3DoubleThreat3.isEmpty()) {
							playingDoubleThreat3DoubleThreat3.keySet().stream().forEach(t -> playingDoubleThreat3DoubleThreat3.get(t).stream().forEach(c -> context.addContribution(c, playingColor, EngineConstants.DOUBLE_THREAT_3_DOUBLE_THREAT_3_POTENTIAL)));
						} else {
							Map<Threat, Set<Cell>> opponentDoubleThreat3DoubleThreat3 = getEffectiveThreats(opponentThreatContext, playingThreatContext, ThreatType.DOUBLE_THREAT_3, ThreatType.DOUBLE_THREAT_3);
							if (!opponentDoubleThreat3DoubleThreat3.isEmpty()) {
								evaluateOpponentThreat(dataWrapper, context, opponentDoubleThreat3DoubleThreat3);
							}
						}
					}
					
				}
			}
		}
		
		return context;
	}

	private void evaluateOpponentThreat(DataWrapper dataWrapper, EvaluationContext context, Map<Threat, Set<Cell>> opponentEffectiveThreats) {
	
		int playingColor = context.getContextColor();
		
		double maxEval = Double.NEGATIVE_INFINITY;
		
		Cell bestThreatCell = null;
		
		for (Threat threat : opponentEffectiveThreats.keySet()) {
			
			double minEval = Double.POSITIVE_INFINITY;
			Cell worstThreatCell = null;
			
			Set<Cell> cellsToEval = threat instanceof DoubleThreat doubleThreat ? doubleThreat.getBlockingCells() : threat.getEmptyCells();
			
			for (Cell threatCell : cellsToEval) {
				dataWrapper.addMove(threatCell, playingColor);
				
				EvaluationContext tempContext = new EvaluationContext(-playingColor);
				
				tempContext.setDepth(context.getDepth() + 1);
				
				double eval = internalComputeEvaluation(dataWrapper, -playingColor, tempContext).getEvaluation();
				
				dataWrapper.removeMove(threatCell);

				if (eval < minEval) {
					minEval = eval;
					worstThreatCell = threatCell;
					
					if (minEval <= maxEval) {
						break;
					}
				}
			}
			
			if (minEval > maxEval) {
				maxEval = minEval;
				bestThreatCell = worstThreatCell;
			}
			
		}
		
		dataWrapper.addMove(bestThreatCell, playingColor);
		
		context.increaseDepth();
		
		internalComputeEvaluation(dataWrapper, -playingColor, context);
		
		context.decreaseDepth();
		
		dataWrapper.removeMove(bestThreatCell);
			
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
						// find double threat 3
						
						long count = playingThreatContext.getDoubleThreatTypeToThreatMap().get(ThreatType.DOUBLE_THREAT_3).stream().filter(t -> !threat.getPlainCells().containsAll(t.getPlainCells()) && t.getTargetCell().equals(threat.getTargetCell())).count();

						if (count > 0) {
							map.put(threat, Set.of(playingCell));
						}
						
					}
					
				}
			}
		}
		
		return map;
	}

	private boolean hasT4Counter(Cell blockingCell, ThreatContext opponentThreatContext) {
		return opponentThreatContext.getThreatTypeToThreatMap().get(ThreatType.THREAT_4).stream().anyMatch(t -> t.getEmptyCells().contains(blockingCell));
	}

	private boolean hasT5Counter(Cell playingCell, ThreatContext opponentThreatContext) {
		return opponentThreatContext.getThreatTypeToThreatMap().get(ThreatType.THREAT_5).stream().anyMatch(t -> !t.getEmptyCells().contains(playingCell));
	}

}
