package fr.leblanc.gomoku.engine.service.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

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
import fr.leblanc.gomoku.engine.service.EvaluationService;
import fr.leblanc.gomoku.engine.service.MessageService;
import fr.leblanc.gomoku.engine.service.MinMaxService;
import fr.leblanc.gomoku.engine.service.ThreatContextService;
import fr.leblanc.gomoku.engine.util.GameHelper;
import fr.leblanc.gomoku.engine.util.cache.GomokuCache;
import fr.leblanc.gomoku.engine.util.cache.L2CacheSupport;

@Service
public class MinMaxServiceImpl implements MinMaxService {
	
	private static final Logger logger = LoggerFactory.getLogger(MinMaxServiceImpl.class);
	
	private static final int MAX_THREADS = Runtime.getRuntime().availableProcessors();

	@Autowired
	private ThreatContextService threatContextService;
	
	@Autowired
	private EvaluationService evaluationService;

	@Autowired
	private MessageService messagingService;
	
	private boolean isComputing = false;
	
	private boolean stopComputation = false;
	
	@Override
	public MinMaxResult computeMinMax(DataWrapper dataWrapper, List<Cell> analyzedCells, int maxDepth, int extent) throws InterruptedException {
		
		MinMaxResult result = null;
		
		if (logger.isDebugEnabled()) {
			logger.debug("starting minMax...");
		}
		
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		
		isComputing = true;
		
		try {
			
			MinMaxContext context = new MinMaxContext();
			context.setMaxDepth(maxDepth);
			context.setFindMax(maxDepth % 2 == 0);
			context.setPlayingColor(GameHelper.extractPlayingColor(dataWrapper));
			
			if (analyzedCells == null) {
				analyzedCells = threatContextService.buildAnalyzedCells(dataWrapper, context.getPlayingColor());
			}
			
			int emptyCellsCount = GameHelper.countEmptyCells(dataWrapper);
			
			if (extent > 0) {
				
				List<Cell> extentAnalyzedCells = new ArrayList<>();
				
				context.setEndIndex(extent * analyzedCells.size() - extent * (extent - 1) / 2 + extent * (emptyCellsCount - 1));
				
				for (int i = 0; i < extent; i++) {
					if (!analyzedCells.isEmpty()) {
						result = internalMinMax(dataWrapper, analyzedCells, context);
						Cell tempResult = result.getOptimalMoves().get(0);
						if (tempResult != null) {
							extentAnalyzedCells.add(tempResult);
							analyzedCells.remove(tempResult);
						}
					}
				}
				
				context.setIndexDepth(1);
				context.setMaxDepth(context.getMaxDepth() + 1);
				context.setFindMax(!context.isFindMax());
				
				result = internalMinMax(dataWrapper, extentAnalyzedCells, context);
			} else {
				context.setEndIndex(analyzedCells.size());
				result = internalMinMax(dataWrapper, analyzedCells, context);
			}
			
			return result;
		} catch (InterruptedException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Error while computing min/max : " + e.getMessage(), e);
		} finally {
			isComputing = false;
			messagingService.sendPercentCompleted(1, 100);
			stopWatch.stop();
			
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("minMax elpased time : %d ms", stopWatch.getTotalTimeMillis()));
				logger.debug(String.format("result = %s", result));
			}
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
	
	
	private MinMaxResult internalMinMax(DataWrapper dataWrapper, List<Cell> analysedMoves, MinMaxContext context) throws InterruptedException, ExecutionException {
		
		int threadsInvolved = context.getMaxDepth() > 2 ? MAX_THREADS : 1;
		
		List<RecursiveMinMaxCommand> commands = new ArrayList<>();
		
		Map<Integer, List<Cell>> batchMap = new HashMap<>();
		
		Iterator<Cell> iterator = analysedMoves.iterator();
		
		while (iterator.hasNext()) {
			for (int i = 0; i < threadsInvolved && iterator.hasNext(); i++) {
				batchMap.computeIfAbsent(i, key -> new ArrayList<>()).add(iterator.next());
			}
		}
		
		for (List<Cell> cells : batchMap.values()) {
			commands.add(new RecursiveMinMaxCommand(dataWrapper, cells, context, L2CacheSupport.getCurrentCache()));
		}
		
		List<Future<MinMaxResult>> results = Executors.newFixedThreadPool(threadsInvolved).invokeAll(commands);
		
		results.sort(resultsComparator(context.isFindMax()));
		
		return results.get(0).get();
	}

	private Comparator<? super Future<MinMaxResult>> resultsComparator(boolean findMax) {
		return (f1, f2) -> {
			try {
				MinMaxResult minMaxResult1 = f1.get();
				MinMaxResult minMaxResult2 = f2.get();
				if (findMax) {
					if (minMaxResult1.getEvaluation() < minMaxResult2.getEvaluation()) {
						return 1;
					} else if (minMaxResult1.getEvaluation() == minMaxResult2.getEvaluation()) {
						return 0;
					}
					return -1;
				}
				if (minMaxResult1.getEvaluation() > minMaxResult2.getEvaluation()) {
					return 1;
				} else if (minMaxResult1.getEvaluation() == minMaxResult2.getEvaluation()) {
					return 0;
				}
				return -1;
			} catch (InterruptedException | ExecutionException e1) {
				 Thread.currentThread().interrupt();
			}
			return 0;
		};
	}
	
	private class RecursiveMinMaxCommand implements Callable<MinMaxResult> {
		
		private MinMaxContext context;
		private DataWrapper dataWrapper;
		private List<Cell> cells;
		private GomokuCache cache;
		
		private RecursiveMinMaxCommand(DataWrapper dataWrapper, List<Cell> cells, MinMaxContext context, GomokuCache cache) {
			this.context = new MinMaxContext(context);
			this.dataWrapper = new DataWrapper(dataWrapper);
			this.cells = cells;
			this.cache = cache;
		}
		
		@Override
		public MinMaxResult call() {
			try {
				return L2CacheSupport.doInCacheContext(() -> recursiveMinMax(dataWrapper, context.getPlayingColor(), cells, context.isFindMax(), 0, context), cache);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			return null;
		}

	}

	private MinMaxResult recursiveMinMax(DataWrapper dataWrapper, int playingColor, List<Cell> analysedMoves, boolean findMax, int currentDepth, MinMaxContext context) throws InterruptedException {
		
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		
		MinMaxResult result = new MinMaxResult();
		
		double optimalEvaluation = findMax ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
		
		for (Cell analysedMove : analysedMoves) {
			
			if (stopComputation) {
				stopComputation = false;
				throw new InterruptedException();
			}
			
			dataWrapper.addMove(analysedMove, playingColor);

			if (currentDepth <= 1) {
				messagingService.sendAnalysisCell(analysedMove, playingColor);
			}
			
			MinMaxResult subResult = new MinMaxResult();
			
			double currentEvaluation = 0;
			
			if (currentDepth == context.getMaxDepth() - 1) {
				currentEvaluation = evaluationService.computeEvaluation(dataWrapper).getEvaluation();
			} else {
				List<Cell> subAnalyzedMoves = threatContextService.buildAnalyzedCells(dataWrapper, -playingColor);
				subResult = recursiveMinMax(dataWrapper, -playingColor, subAnalyzedMoves, !findMax, currentDepth + 1, context);
				currentEvaluation = subResult.getEvaluation();
			}
			
			dataWrapper.removeMove(analysedMove);
			
			if (currentDepth <= 1) {
				messagingService.sendAnalysisCell(analysedMove, EngineConstants.NONE_COLOR);
			}
			
			int factor = findMax ? 1 : -1;
			
			Map<Integer, Double> optimumList = findMax ? context.getMaxList() : context.getMinList();
			Map<Integer, Double> otherList = findMax ? context.getMinList() : context.getMaxList();
			AtomicReference<Double> firstMaximum = context.getFirstMaximum();
			
			if (factor * currentEvaluation > factor * optimalEvaluation) {
				optimalEvaluation = currentEvaluation;
				
				result.setEvaluation(optimalEvaluation);
				result.getOptimalMoves().put(currentDepth, analysedMove);
				
				for (Entry<Integer, Cell> entry : subResult.getOptimalMoves().entrySet()) {
					result.getOptimalMoves().put(entry.getKey(), entry.getValue());
				}
				
				if (currentDepth == 0 && optimalEvaluation > firstMaximum.get()) {
					firstMaximum.set(optimalEvaluation);
				}
				
				optimumList.put(currentDepth, optimalEvaluation);
				
				double eval = optimalEvaluation;
				if (isOptimumReached(currentDepth, factor, otherList, eval, firstMaximum.get())) {
					break;
				}
			}
			if (currentDepth == context.getIndexDepth()) {
				context.getCurrentIndex().set(context.getCurrentIndex().get() + 1);
				Integer percentCompleted = context.getCurrentIndex().get() * 100 / context.getEndIndex();
				messagingService.sendPercentCompleted(1, percentCompleted);
			}
		}
	
		stopWatch.stop();
		
		context.getMaxList().remove(currentDepth);
		context.getMinList().remove(currentDepth);
		
		return result;
	}

	private boolean isOptimumReached(int depth, int factor, Map<Integer, Double> otherList, double eval, Double firstMaximum) {
		if (otherList.containsKey(0) && depth > 0 && factor * eval >= factor * firstMaximum) {
			return true;
		}
		return otherList.entrySet().stream().anyMatch(entry -> entry.getKey() < depth && factor * eval >= factor * entry.getValue());
	}

}
