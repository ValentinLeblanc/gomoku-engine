package fr.leblanc.gomoku.engine.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.DataWrapper;
import fr.leblanc.gomoku.engine.model.EngineConstants;
import fr.leblanc.gomoku.engine.model.MinMaxContext;
import fr.leblanc.gomoku.engine.model.MinMaxResult;
import fr.leblanc.gomoku.engine.model.messaging.EngineSettingsDto;
import fr.leblanc.gomoku.engine.service.CheckWinService;
import fr.leblanc.gomoku.engine.service.EvaluationService;
import fr.leblanc.gomoku.engine.service.MessagingService;
import fr.leblanc.gomoku.engine.service.MinMaxService;
import fr.leblanc.gomoku.engine.service.ThreatContextService;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class MinMaxServiceImpl implements MinMaxService {
	
	@Autowired
	private ThreatContextService threatContextService;
	
	@Autowired
	private EvaluationService evaluationService;

	@Autowired
	private MessagingService messagingService;
	
	@Autowired
	private CheckWinService checkWinService;
	
	private Boolean isComputing = false;
	
	private Boolean stopComputation = false;
	
	@Override
	public Cell computeMinMax(DataWrapper dataWrapper, int playingColor, List<Cell> analyzedMoves, EngineSettingsDto engineSettings) throws InterruptedException {
		
		if (log.isDebugEnabled()) {
			log.debug("starting minMax...");
		}
		
		isComputing = true;
		
		if (analyzedMoves == null) {
			analyzedMoves = threatContextService.buildAnalyzedMoves(dataWrapper, playingColor);
		}
		
		if (analyzedMoves.size() == 1) {
			return analyzedMoves.get(0);
		}
		
//		if (analyzedMoves.size() < 4) {
//			engineSettings.setMinMaxDepth(engineSettings.getMinMaxDepth() + 1);    
//		}

		try {
			
			Cell resultCell = null;
			
			StopWatch stopWatch = new StopWatch();
			
			stopWatch.start();
			
			MinMaxResult result = internalMinMax(dataWrapper, playingColor, analyzedMoves, (engineSettings.getMinMaxDepth() % 2 == 0), 0, new MinMaxContext(), engineSettings);
			
			resultCell = result.getOptimalMoves().get(0);
			
			stopWatch.stop();
			
			if (log.isDebugEnabled()) {
				log.debug("minMax elpased time : " + stopWatch.getTotalTimeMillis() + " ms");
				log.debug("result = " + result);
			}
			
			isComputing = false;
			
			return resultCell;
		} catch (InterruptedException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error while computing min/max : " + e.getMessage(), e);
		}

		return null;
	}
	
	@Override
	public boolean isComputing() {
		return isComputing;
	}

	@Override
	public void stopComputation() {
		stopComputation = true;
		isComputing = false;
	}
	
	private MinMaxResult internalMinMax(DataWrapper dataWrapper, int playingColor, List<Cell> analysedMoves, boolean findMax, int depth, MinMaxContext context, EngineSettingsDto engineSettings) throws InterruptedException {
		
		MinMaxResult result = new MinMaxResult();
		
		double optimalEvaluation = findMax ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
		
		if (checkWinService.checkWin(dataWrapper, -playingColor, new int[5][2])) {
			result.setEvaluation(optimalEvaluation);
			return result;
		}
		
		int advancement = 0;

		for (Cell analysedMove : analysedMoves) {
			
			if (stopComputation) {
				stopComputation = false;
				throw new InterruptedException();
			}
			
			dataWrapper.addMove(analysedMove, playingColor);

			if (depth <= 1) {
				messagingService.sendAnalysisCell(analysedMove, playingColor);
			}
			
			MinMaxResult subResult = new MinMaxResult();
			
			double currentEvaluation = 0;
			
			if (depth == engineSettings.getMinMaxDepth() - 1) {
				
				if (!context.getEvaluationCache().containsKey(-playingColor)) {
					context.getEvaluationCache().put(-playingColor, new HashMap<>());
				}
				
				if (context.getEvaluationCache().get(-playingColor).containsKey(dataWrapper)) {
					currentEvaluation = context.getEvaluationCache().get(-playingColor).get(dataWrapper);
				} else {
					currentEvaluation = evaluationService.computeEvaluation(dataWrapper, -playingColor, engineSettings.getEvaluationDepth());
					context.getEvaluationCache().get(-playingColor).put(new DataWrapper(dataWrapper), currentEvaluation);
				}
				
			} else {
				subResult = internalMinMax(dataWrapper, -playingColor, threatContextService.buildAnalyzedMoves(dataWrapper, -playingColor), !findMax, depth + 1, context, engineSettings);
				currentEvaluation = subResult.getEvaluation();
			}
			
			dataWrapper.removeMove(analysedMove);
			
			if (depth <= 1) {
				messagingService.sendAnalysisCell(analysedMove, EngineConstants.NONE_COLOR);
			}
			
			if (findMax) {
				if (currentEvaluation > optimalEvaluation) {
					optimalEvaluation = currentEvaluation;
					
					result.setEvaluation(optimalEvaluation);
					
					result.getOptimalMoves().put(depth, analysedMove);
					
					for (Entry<Integer, Cell> entry : subResult.getOptimalMoves().entrySet()) {
						result.getOptimalMoves().put(entry.getKey(), entry.getValue());
					}
					
					context.getMaxList().put(depth, optimalEvaluation);
					
					final double eval = optimalEvaluation;
					
					if (context.getMinList().entrySet().stream().anyMatch(minEntry -> minEntry.getKey() < depth && eval >= minEntry.getValue())) {
						break;
					}
				}
			} else {
				if (currentEvaluation < optimalEvaluation) {
					optimalEvaluation = currentEvaluation;
					
					result.setEvaluation(optimalEvaluation);
					
					result.getOptimalMoves().put(depth, analysedMove);
					
					context.getMinList().put(depth, optimalEvaluation);
					
					for (Entry<Integer, Cell> entry : subResult.getOptimalMoves().entrySet()) {
						result.getOptimalMoves().put(entry.getKey(), entry.getValue());
					}
					
					final double eval = optimalEvaluation;

					if (context.getMaxList().entrySet().stream().anyMatch(maxEntry -> maxEntry.getKey() < depth && eval <= maxEntry.getValue())) {
						break;
					}
				}
			}
			
			advancement++;

			Integer percentCompleted = advancement * 100 / analysedMoves.size();
			
			if (depth == 0) {
				messagingService.sendPercentCompleted(1, percentCompleted);
			}
		}
	
		context.getMaxList().remove(depth);
		context.getMinList().remove(depth);
		
		return result;
	}

}
