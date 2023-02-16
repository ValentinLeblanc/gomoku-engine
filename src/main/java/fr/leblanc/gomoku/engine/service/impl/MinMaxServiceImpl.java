package fr.leblanc.gomoku.engine.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.DataWrapper;
import fr.leblanc.gomoku.engine.model.EngineConstants;
import fr.leblanc.gomoku.engine.model.MinMaxContext;
import fr.leblanc.gomoku.engine.model.MinMaxResult;
import fr.leblanc.gomoku.engine.service.CheckWinService;
import fr.leblanc.gomoku.engine.service.EvaluationService;
import fr.leblanc.gomoku.engine.service.MessageService;
import fr.leblanc.gomoku.engine.service.MinMaxService;
import fr.leblanc.gomoku.engine.service.ThreatContextService;
import fr.leblanc.gomoku.engine.util.GameHelper;

@Service
public class MinMaxServiceImpl implements MinMaxService {
	
	private static final Logger logger = LoggerFactory.getLogger(MinMaxServiceImpl.class);
	
	@Autowired
	private ThreatContextService threatContextService;
	
	@Autowired
	private EvaluationService evaluationService;

	@Autowired
	private MessageService messagingService;
	
	@Autowired
	private CheckWinService checkWinService;
	
	private boolean isComputing = false;
	
	private boolean stopComputation = false;
	
	@Override
	public MinMaxResult computeMinMax(DataWrapper dataWrapper, List<Cell> cells, int depth, int extent) throws InterruptedException {
		
		if (logger.isDebugEnabled()) {
			logger.debug("starting minMax...");
		}
		
		isComputing = true;
		
		try {
			
			int playingColor = GameHelper.extractPlayingColor(dataWrapper);
			
			if (cells == null) {
				cells = threatContextService.buildAnalyzedMoves(dataWrapper, playingColor);
			}
			
			if (cells.size() == 1) {
				
				MinMaxResult result = new MinMaxResult();
				
				result.getOptimalMoves().put(0, cells.get(0));
				
				return result;
			}
			
			try {
				
				StopWatch stopWatch = new StopWatch();
				stopWatch.start();
				
				List<Cell> subAnalyzedCells = new ArrayList<>();
				boolean findMax = depth % 2 == 0;
				MinMaxResult result = null;
				MinMaxContext context = new MinMaxContext();
				
				if (extent != 0) {
					
					int emptyCellsCount = 0;
					
					for (int i = 0; i < dataWrapper.getData().length; i++) {
						for (int j = 0; j < dataWrapper.getData().length; j++) {
							if (dataWrapper.getData()[i][j] == EngineConstants.NONE_COLOR) {
								emptyCellsCount++;
							}
						}
					}
					
					context.setEndIndex(extent * cells.size() - extent * (extent - 1) / 2 + extent * (emptyCellsCount - 1));
					
					for (int i = 0; i < extent; i++) {
						result = internalMinMax(dataWrapper, playingColor, cells, findMax, 0, context, depth);
						Cell cellResult = result.getOptimalMoves().get(0);
						if (cellResult != null) {
							subAnalyzedCells.add(cellResult);
							cells.remove(cellResult);
						}
					}
					
					context.setIndexDepth(1);
					result = internalMinMax(dataWrapper, playingColor, subAnalyzedCells, !findMax, 0, context, depth + 1);
					messagingService.sendPercentCompleted(1, 100);
				} else {
					context.setEndIndex(cells.size());
					result = internalMinMax(dataWrapper, playingColor, cells, findMax, 0, context, depth);
				}
				
				stopWatch.stop();
				
				if (logger.isDebugEnabled()) {
					logger.debug("minMax elpased time : " + stopWatch.getTotalTimeMillis() + " ms");
					logger.debug("result = " + result);
				}
				
				return result;
			} catch (InterruptedException e) {
				throw e;
			} catch (Exception e) {
				logger.error("Error while computing min/max : " + e.getMessage(), e);
			}
		} finally {
			isComputing = false;
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
	
	private MinMaxResult internalMinMax(DataWrapper dataWrapper, int playingColor, List<Cell> analysedMoves, boolean findMax, int depth, MinMaxContext context, int minMaxDepth) throws InterruptedException {
		
		MinMaxResult result = new MinMaxResult();
		
		double optimalEvaluation = findMax ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
		
		if (checkWinService.checkWin(dataWrapper, -playingColor, new int[5][2])) {
			result.setEvaluation(optimalEvaluation);
			return result;
		}
		
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
			
			if (depth == minMaxDepth - 1) {
				currentEvaluation = evaluationService.computeEvaluation(dataWrapper).getEvaluation();
			} else {
				List<Cell> subAnalyzedMoves = threatContextService.buildAnalyzedMoves(dataWrapper, -playingColor);
				subResult = internalMinMax(dataWrapper, -playingColor, subAnalyzedMoves, !findMax, depth + 1, context, minMaxDepth);
				currentEvaluation = subResult.getEvaluation();
				if (depth < context.getIndexDepth()) {
					context.setCurrentIndex(context.getCurrentIndex() + subAnalyzedMoves.size());
					Integer percentCompleted = context.getCurrentIndex() * 100 / context.getEndIndex();
					messagingService.sendPercentCompleted(1, percentCompleted);
				}
			}
			
			dataWrapper.removeMove(analysedMove);
			
			if (depth <= 1) {
				messagingService.sendAnalysisCell(analysedMove, EngineConstants.NONE_COLOR);
			}
			
			int factor = findMax ? 1 : -1;
			
			Map<Integer, Double> optimumList = findMax ? context.getMaxList() : context.getMinList();
			Map<Integer, Double> otherList = findMax ? context.getMinList() : context.getMaxList();
			
			if (factor * currentEvaluation > factor * optimalEvaluation) {
				optimalEvaluation = currentEvaluation;
				
				result.setEvaluation(optimalEvaluation);
				result.getOptimalMoves().put(depth, analysedMove);
				
				for (Entry<Integer, Cell> entry : subResult.getOptimalMoves().entrySet()) {
					result.getOptimalMoves().put(entry.getKey(), entry.getValue());
				}
				
				optimumList.put(depth, optimalEvaluation);
				
				double eval = optimalEvaluation;
				if (isOptimumReached(depth, factor, otherList, eval)) {
					break;
				}
			}
			if (depth == 0) {
				context.setCurrentIndex(context.getCurrentIndex() + 1);
				Integer percentCompleted = context.getCurrentIndex() * 100 / context.getEndIndex();
				messagingService.sendPercentCompleted(1, percentCompleted);
			}
		}
	
		context.getMaxList().remove(depth);
		context.getMinList().remove(depth);
		
		return result;
	}

	private boolean isOptimumReached(int depth, int factor, Map<Integer, Double> otherList, double eval) {
		return otherList.entrySet().stream().anyMatch(entry -> entry.getKey() < depth && factor * eval >= factor * entry.getValue());
	}

}
