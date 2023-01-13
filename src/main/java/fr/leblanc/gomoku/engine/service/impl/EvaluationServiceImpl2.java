package fr.leblanc.gomoku.engine.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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

		return evaluateThreats(context, playingThreatContext, opponentThreatContext);
		
	}

	private int evaluateThreats(EvaluationContext context, ThreatContext playingThreatContext, ThreatContext opponentThreatContext) {
		
		Map<CompoThreatType, Map<Cell, List<Pair<Threat>>>> compoThreatMap = new HashMap<>();
		
		int evaluation = 0;
		
		boolean jokerUsed = false;
		
		for (CompoThreatType tryContext : EngineConstants.TRY_CONTEXTS) {
			
			if (tryContext.isPlaying()) {
				compoThreatMap.put(tryContext, threatContextService.findCompositeThreats(playingThreatContext, tryContext));
			} else {
				compoThreatMap.put(tryContext, threatContextService.findCompositeThreats(opponentThreatContext, tryContext));
			}
		}
		
		CompoThreatType t5 = CompoThreatType.of(ThreatType.THREAT_5, null, true);
		
		for (Entry<Cell, List<Pair<Threat>>> entry : compoThreatMap.get(t5).entrySet()) {
			evaluation += t5.getPotential();
		}
		
		CompoThreatType dt4 = CompoThreatType.of(ThreatType.DOUBLE_THREAT_4, null, true);
		
		for (Entry<Cell, List<Pair<Threat>>> entry : compoThreatMap.get(dt4).entrySet()) {
			Cell threatPosition = entry.getKey();
			
			CompoThreatType _t5 = CompoThreatType.of(ThreatType.THREAT_5, null, false);
			
			long numberOfOpponentT5 = compoThreatMap.get(_t5).keySet().stream().filter(c -> !c.equals(threatPosition)).count();
			
			if (numberOfOpponentT5 == 0) {
				evaluation += t5.getPotential();
			}
		}
		
		CompoThreatType t4t4 = CompoThreatType.of(ThreatType.THREAT_4, ThreatType.THREAT_4, true);
		
		for (Entry<Cell, List<Pair<Threat>>> entry : compoThreatMap.get(t4t4).entrySet()) {
			Cell threatPosition = entry.getKey();
			
			CompoThreatType _t5 = CompoThreatType.of(ThreatType.THREAT_5, null, false);
			
			long numberOfOpponentT5 = compoThreatMap.get(_t5).keySet().stream().filter(c -> !c.equals(threatPosition)).count();
			
			if (numberOfOpponentT5 == 0) {
				evaluation += t5.getPotential();
			}
		}
		
		CompoThreatType _t5 = CompoThreatType.of(ThreatType.THREAT_5, null, false);
		
		for (Entry<Cell, List<Pair<Threat>>> entry : compoThreatMap.get(_t5).entrySet()) {
			Cell threatPosition = entry.getKey();
			
			long numberOfT5 = compoThreatMap.get(t5).keySet().stream().filter(c -> !c.equals(threatPosition)).count();
			
			if (numberOfT5 == 0) {
				
				if (!jokerUsed) {
					jokerUsed = true;
				} else {
					evaluation -= t5.getPotential();
				}
			}
		}
		
		CompoThreatType t4dt3 = CompoThreatType.of(ThreatType.THREAT_4, ThreatType.DOUBLE_THREAT_3, true);
		
		for (Entry<Cell, List<Pair<Threat>>> entry : compoThreatMap.get(t4dt3).entrySet()) {
			
			Cell threatPosition = entry.getKey();
			
			List<Pair<Threat>> threatPairs = entry.getValue();
			
			long numberOfOpponentT5 = compoThreatMap.get(_t5).keySet().stream().filter(c -> !c.equals(threatPosition)).count();
			
			if (numberOfOpponentT5 == 0) {
				
				for (Pair<Threat> threatPair : threatPairs) {
					
					Threat firstThreat4 = threatPair.getFirst();
					
					Cell blockingCell = firstThreat4.getEmptyCells().iterator().next();
					
					if (opponentThreatContext.getCellToThreatMap().get(blockingCell) == null
							|| opponentThreatContext.getCellToThreatMap().get(blockingCell).get(ThreatType.THREAT_4) == null
							|| opponentThreatContext.getCellToThreatMap().get(blockingCell).get(ThreatType.THREAT_4).isEmpty()) {
						evaluation += t4dt3.getPotential();
					}
					
				}
			}
		}
		
		CompoThreatType dt3dt3 = CompoThreatType.of(ThreatType.DOUBLE_THREAT_3, ThreatType.DOUBLE_THREAT_3, true);
		
		for (Entry<Cell, List<Pair<Threat>>> entry : compoThreatMap.get(dt3dt3).entrySet()) {
			
			Cell threatPosition = entry.getKey();
			
			List<Pair<Threat>> threatPairs = entry.getValue();
			
			long numberOfOpponentT5 = compoThreatMap.get(_t5).keySet().stream().filter(c -> !c.equals(threatPosition)).count();
			
			if (numberOfOpponentT5 == 0) {
				for (Pair<Threat> threatPair : threatPairs) {
					boolean isBlocked = false;
					
					DoubleThreat dt31 = (DoubleThreat) threatPair.getFirst();
					
					for (Cell blockingCell : dt31.getBlockingCells()) {
						if (opponentThreatContext.getCellToThreatMap().get(blockingCell) != null
								&& opponentThreatContext.getCellToThreatMap().get(blockingCell).get(ThreatType.THREAT_4) != null
								&& !opponentThreatContext.getCellToThreatMap().get(blockingCell).get(ThreatType.THREAT_4).isEmpty()) {
							isBlocked = true;
							break;
						}
					}
					
					DoubleThreat dt32 = (DoubleThreat) threatPair.getSecond();
					
					if(!isBlocked) {
						for (Cell blockingCell : dt32.getBlockingCells()) {
							if (opponentThreatContext.getCellToThreatMap().get(blockingCell) != null
									&& opponentThreatContext.getCellToThreatMap().get(blockingCell).get(ThreatType.THREAT_4) != null
									&& !opponentThreatContext.getCellToThreatMap().get(blockingCell).get(ThreatType.THREAT_4).isEmpty()) {
								isBlocked = true;
								break;
							}
						}
					}
					
					if (!isBlocked) {
						evaluation += t4dt3.getPotential();
					}
				}
			}
		}
		
		CompoThreatType _t4dt3 = CompoThreatType.of(ThreatType.THREAT_4, ThreatType.DOUBLE_THREAT_3, false);
		
		for (Entry<Cell, List<Pair<Threat>>> entry : compoThreatMap.get(_t4dt3).entrySet()) {
			
			Cell threatPosition = entry.getKey();
			
			List<Pair<Threat>> threatPairs = entry.getValue();
			
			long numberOfT5 = compoThreatMap.get(t5).keySet().stream().filter(c -> !c.equals(threatPosition)).count();
			
			if (numberOfT5 == 0) {
				
				for (Pair<Threat> threatPair : threatPairs) {
					
					Threat firstThreat4 = threatPair.getFirst();
					
					Cell blockingCell = firstThreat4.getEmptyCells().iterator().next();
					
					if (playingThreatContext.getCellToThreatMap().get(blockingCell) == null
							|| playingThreatContext.getCellToThreatMap().get(blockingCell).get(ThreatType.THREAT_4) == null
							|| playingThreatContext.getCellToThreatMap().get(blockingCell).get(ThreatType.THREAT_4).isEmpty()) {
						
						if (jokerUsed) {
							evaluation -= t4dt3.getPotential();
						} else {
							jokerUsed = true;
						}
					}
					
				}
			}
		}
		
		return evaluation;
	}
	
}
