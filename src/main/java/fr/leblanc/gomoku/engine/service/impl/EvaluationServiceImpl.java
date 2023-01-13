package fr.leblanc.gomoku.engine.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;

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
import fr.leblanc.gomoku.engine.util.cache.L2CacheSupport;

//@Service
public class EvaluationServiceImpl implements EvaluationService {

	private static final int EVALUATION_DEPTH = 2;
	
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
		
		double evaluation = internalComputeEvaluation(new EvaluationContext(dataWrapper, playingColor, EVALUATION_DEPTH, 0));
		
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
		
		ThreatContext playingThreatContext = threatContextService.computeThreatContext(context.getDataWrapper(), context.getPlayingColor());
		
		ThreatContext opponentThreatContext = threatContextService.computeThreatContext(context.getDataWrapper(), -context.getPlayingColor());

		return evaluateThreats(context, playingThreatContext, opponentThreatContext, new CompoThreatType(ThreatType.THREAT_5, null, true));
		
	}

	private int evaluateThreats(EvaluationContext context, ThreatContext playingThreatContext, ThreatContext opponentThreatContext, CompoThreatType tryContext) {
		
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
