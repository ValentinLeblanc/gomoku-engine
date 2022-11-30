package fr.leblanc.gomoku.engine.service.impl;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.DataWrapper;
import fr.leblanc.gomoku.engine.model.DoubleThreat;
import fr.leblanc.gomoku.engine.model.EngineConstants;
import fr.leblanc.gomoku.engine.model.EvaluationContext;
import fr.leblanc.gomoku.engine.model.Threat;
import fr.leblanc.gomoku.engine.model.ThreatContext;
import fr.leblanc.gomoku.engine.model.ThreatTryContext;
import fr.leblanc.gomoku.engine.model.ThreatType;
import fr.leblanc.gomoku.engine.service.CheckWinService;
import fr.leblanc.gomoku.engine.service.EvaluationService;
import fr.leblanc.gomoku.engine.service.ThreatContextService;

@Service
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

		return evaluateThreats(context, playingThreatContext, opponentThreatContext, new ThreatTryContext(ThreatType.THREAT_5, null, true));
		
	}

	private int evaluateThreats(EvaluationContext context, ThreatContext playingThreatContext, ThreatContext opponentThreatContext, ThreatTryContext tryContext) {
		
		if (tryContext == null) {
			return 0;
		}
		
		AtomicInteger evaluation = new AtomicInteger(0);
		
		boolean change = tryContext.getNext() != null && tryContext.isPlaying() != tryContext.getNext().isPlaying();
		
		Map<Threat, Integer> threats = threatContextService.getEffectiveThreats(playingThreatContext, opponentThreatContext, tryContext.getThreatType1(), tryContext.getThreatType2());
		
		if (!threats.isEmpty()) {
			if (tryContext.isPlaying()) {
				threats.keySet().stream()
				.forEach(t -> evaluation.addAndGet(threats.get(t) * tryContext.getPotential()));
			} else {
				int evaluateOpponentThreat = evaluateOpponentThreat(context, threats);
				evaluation.addAndGet(-evaluateOpponentThreat);
			}
		} else {
			if (change) {
				evaluation.addAndGet(evaluateThreats(context, opponentThreatContext, playingThreatContext, tryContext.getNext()));
			} else {
				evaluation.addAndGet(evaluateThreats(context, playingThreatContext, opponentThreatContext, tryContext.getNext()));
			}
		}
		
		return evaluation.intValue();
	}

	private int evaluateOpponentThreat(EvaluationContext context, Map<Threat, Integer> opponentEffectiveThreats) {
	
		int maxEval = Integer.MIN_VALUE;

		for (Threat threat : opponentEffectiveThreats.keySet()) {
		
			int minEval = Integer.MAX_VALUE;
			
			Set<Cell> cellsToEval = threat instanceof DoubleThreat doubleThreat ? doubleThreat.getBlockingCells() : threat.getEmptyCells();
			
			if (threat instanceof DoubleThreat doubleThreat) {
				cellsToEval.add(doubleThreat.getTargetCell());
			}
			
			for (Cell threatCell : cellsToEval) {
				context.getDataWrapper().addMove(threatCell, context.getPlayingColor());
				
				context.increaseDepth();
				context.reversePlayingColor();
				int eval = internalComputeEvaluation(context);
				context.reversePlayingColor();
				context.decreaseDepth();
				
				context.getDataWrapper().removeMove(threatCell);

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
	
}
