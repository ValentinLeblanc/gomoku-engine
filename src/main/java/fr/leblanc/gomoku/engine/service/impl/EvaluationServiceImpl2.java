package fr.leblanc.gomoku.engine.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.CompoThreatType;
import fr.leblanc.gomoku.engine.model.DataWrapper;
import fr.leblanc.gomoku.engine.model.EngineConstants;
import fr.leblanc.gomoku.engine.model.EvaluationContext;
import fr.leblanc.gomoku.engine.model.Threat;
import fr.leblanc.gomoku.engine.model.ThreatContext;
import fr.leblanc.gomoku.engine.model.ThreatType;
import fr.leblanc.gomoku.engine.service.CheckWinService;
import fr.leblanc.gomoku.engine.service.EvaluationService;
import fr.leblanc.gomoku.engine.service.ThreatContextService;
import fr.leblanc.gomoku.engine.util.Pair;
import lombok.extern.apachecommons.CommonsLog;

//@Service
@CommonsLog
public class EvaluationServiceImpl2 implements EvaluationService {

	@Autowired
	private ThreatContextService threatContextService;
	
	@Autowired
	private CheckWinService checkWinService;
	
	@Override
	public double computeEvaluation(DataWrapper dataWrapper, int playingColor, int maxDepth) {
		return internalComputeEvaluation(new EvaluationContext(dataWrapper, playingColor, maxDepth, 0));
	}

	private int internalComputeEvaluation(EvaluationContext context) {
		
		if (context.getDepth() >= context.getMaxDepth()) {
			return 0;
		}
		
		if (checkWinService.checkWin(context.getDataWrapper(), context.getPlayingColor(), new int[5][2])) {
			return EngineConstants.WIN_EVALUATION;
		}
		
		if (checkWinService.checkWin(context.getDataWrapper(), -context.getPlayingColor(), new int[5][2])) {
			return -EngineConstants.WIN_EVALUATION;
		}
		
		ThreatContext playingThreatContext = threatContextService.computeThreatContext(context.getDataWrapper().getData(), context.getPlayingColor());
		
		ThreatContext opponentThreatContext = threatContextService.computeThreatContext(context.getDataWrapper().getData(), -context.getPlayingColor());

		return evaluateThreats(context, playingThreatContext, opponentThreatContext);
		
	}

	private int evaluateThreats(EvaluationContext context, ThreatContext playingThreatContext, ThreatContext opponentThreatContext) {
		
		Map<CompoThreatType, Map<Cell, List<Pair<Threat>>>> visitedMap = new HashMap<>();
		
		Map<CompoThreatType, List<Cell>> result = new HashMap<>();
		
		CompoThreatType currentContext = CompoThreatType.of(ThreatType.THREAT_5, null, true);
		
		Map<Cell, List<Pair<Threat>>> currentThreats = threatContextService.findCompositeThreats(playingThreatContext, currentContext);
		
		visitedMap.put(currentContext, currentThreats);
		
		for (Entry<Cell, List<Pair<Threat>>> current : currentThreats.entrySet()) {
			
			Cell newTargetCell = current.getKey();
			List<Pair<Threat>> newThreatPairs = current.getValue();
			
			if (currentContext.isPlaying()) {
				
			}
			
			for (Entry<Cell, List<Pair<Threat>>> previous : visitedMap.get(currentContext).entrySet()) {
				
				if (!newTargetCell.equals(previous.getKey())) {
					result.computeIfAbsent(currentContext, k -> new ArrayList<>()).add(newTargetCell);
				}
			}
		}
		
		
		int evaluation = 0;
		
		for (CompoThreatType tryContext : EngineConstants.TRY_CONTEXTS) {
			Map<Cell, List<Pair<Threat>>> efficientThreats = threatContextService.findEfficientThreats(playingThreatContext, opponentThreatContext, tryContext);
			
			if (!efficientThreats.isEmpty() && tryContext.getThreatType1() != ThreatType.DOUBLE_THREAT_2) {
				
				if (log.isDebugEnabled()) {
					log.debug(tryContext + " | " + efficientThreats.keySet());
				}
				
				for (Entry<Cell, List<Pair<Threat>>> entry : efficientThreats.entrySet()) {
					if (tryContext.isPlaying()) {
						evaluation += entry.getValue().size() * tryContext.getPotential();
					} else {
						evaluation -= tryContext.getPotential();
					}
				}
			}
		}
		
		return evaluation;
	}
	
}
