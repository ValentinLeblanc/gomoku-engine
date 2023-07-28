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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.GameData;
import fr.leblanc.gomoku.engine.model.EngineConstants;
import fr.leblanc.gomoku.engine.model.MinMaxContext;
import fr.leblanc.gomoku.engine.model.MinMaxResult;
import fr.leblanc.gomoku.engine.model.messaging.MoveDTO;
import fr.leblanc.gomoku.engine.service.EvaluationService;
import fr.leblanc.gomoku.engine.service.WebSocketService;
import fr.leblanc.gomoku.engine.service.MinMaxService;
import fr.leblanc.gomoku.engine.service.ThreatContextService;
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
	private WebSocketService webSocketService;
	
	private ExecutorService multiThreadPoolExecutor;
	
	private boolean isComputing = false;
	
	private AtomicBoolean stopComputation = new AtomicBoolean(false);
	
	@Override
	public MinMaxResult computeMinMax(GameData dataWrapper, List<Cell> analyzedCells, int maxDepth, int extent) throws InterruptedException {
		
		MinMaxResult result = null;
		
		if (logger.isInfoEnabled()) {
			logger.info("starting minMax...");
		}
		
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		
		isComputing = true;
		
		try {
			
			MinMaxContext context = new MinMaxContext();
			context.setPlayingColor(GameData.extractPlayingColor(dataWrapper));
			context.setMaxDepth(maxDepth);
			context.setFindMax(maxDepth % 2 == 0);

			if (analyzedCells == null) {
				analyzedCells = threatContextService.buildAnalyzedCells(dataWrapper, context.getPlayingColor());
			}
			
			int emptyCellsCount = GameData.countEmptyCells(dataWrapper);
			
			if (extent > 0) {
				
				List<Cell> extentAnalyzedCells = new ArrayList<>();
				
				context.setEndIndex(extent * analyzedCells.size() - extent * (extent - 1) / 2 + extent * (emptyCellsCount - 1));
				
				for (int i = 0; i < extent; i++) {
					if (!analyzedCells.isEmpty()) {
						result = internalMinMax(dataWrapper, analyzedCells, context);
						Cell tempResult = result != null ? result.getOptimalMoves().get(0) : null;
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
			
		} catch (Exception e) {
			logger.error("Error while computing min/max : " + e.getMessage(), e);
		} finally {
			isComputing = false;
			webSocketService.sendComputingProgress(100);
			stopWatch.stop();
			
			if (logger.isInfoEnabled()) {
				logger.info(String.format("minMax elpased time : %d ms", stopWatch.getTotalTimeMillis()));
				logger.info(String.format("result = %s", result));
			}
		}
		return result;
	}

	@Override
	public boolean isComputing() {
		return isComputing;
	}

	@Override
	public void stopComputation() {
		stopComputation.set(true);
		isComputing = false;
		multiThreadPoolExecutor.shutdownNow();
	}
	
	
	private MinMaxResult internalMinMax(GameData dataWrapper, List<Cell> analysedMoves, MinMaxContext context) {
		
		context.setOptimumReference(context.isFindMax() ? new AtomicReference<>(Double.NEGATIVE_INFINITY) : new AtomicReference<>(Double.POSITIVE_INFINITY));

		int threadsInvolved = context.getMaxDepth() > 2 ? MAX_THREADS : 1;
		
		multiThreadPoolExecutor = Executors.newFixedThreadPool(threadsInvolved);
		
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
		
		try {
			List<Future<MinMaxResult>> futures = multiThreadPoolExecutor.invokeAll(commands);
			List<MinMaxResult> results = futures.stream().map(r -> {
				try {
					return r.get();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} catch (ExecutionException e) {
					if (e.getCause() instanceof InterruptedException) {
						if (stopComputation.get()) {
							stopComputation.set(false);
							logger.warn("engine was interrupted");
						}
					} else {
						logger.error("Error while executing minMax thread: " + e.getMessage(), e);
					}
				}
				return new MinMaxResult();
			}).filter(r -> r.getFinalEvaluation() != null).collect(Collectors.toList());
			results.sort(resultsComparator(context.isFindMax()));
			if (!results.isEmpty()) {
				return results.get(0);
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		return null;
	}

	private Comparator<? super MinMaxResult> resultsComparator(boolean findMax) {
		return (minMaxResult1, minMaxResult2) -> {
			if (minMaxResult1.getFinalEvaluation().equals(minMaxResult2.getFinalEvaluation())) {
				return 0;
			}
			if (findMax) {
				if (minMaxResult1.getFinalEvaluation() < minMaxResult2.getFinalEvaluation()) {
					return 1;
				}
				return -1;
			}
			if (minMaxResult1.getFinalEvaluation() > minMaxResult2.getFinalEvaluation()) {
				return 1;
			}
			return -1;
		};
	}
	
	private class RecursiveMinMaxCommand implements Callable<MinMaxResult> {
		
		private MinMaxContext context;
		private GameData dataWrapper;
		private List<Cell> cells;
		private GomokuCache cache;
		
		private RecursiveMinMaxCommand(GameData dataWrapper, List<Cell> cells, MinMaxContext context, GomokuCache cache) {
			this.context = new MinMaxContext(context);
			this.dataWrapper = new GameData(dataWrapper);
			this.cells = cells;
			this.cache = cache;
		}
		
		@Override
		public MinMaxResult call() throws InterruptedException {
			try {
				return L2CacheSupport.doInCacheContext(() -> recursiveMinMax(dataWrapper, context.getPlayingColor(), cells, context.isFindMax(), 0, context), cache);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw e;
			}
		}

	}

	private MinMaxResult recursiveMinMax(GameData dataWrapper, int playingColor, List<Cell> analysedMoves, boolean findMax, int currentDepth, MinMaxContext context) throws InterruptedException {
		
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		
		MinMaxResult result = new MinMaxResult();
		
		double optimalEvaluation = findMax ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
		
		for (Cell analysedMove : analysedMoves) {
			
			if (stopComputation.get()) {
				throw new InterruptedException();
			}
			
			dataWrapper.addMove(analysedMove, playingColor);

			if (currentDepth <= 1) {
				webSocketService.sendAnalysisMove(new MoveDTO(analysedMove, playingColor));
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
				webSocketService.sendAnalysisMove(new MoveDTO(analysedMove, EngineConstants.NONE_COLOR));
			}
			
			int factor = findMax ? 1 : -1;
			
			Map<Integer, Double> optimumList = findMax ? context.getMaxList() : context.getMinList();
			Map<Integer, Double> otherList = findMax ? context.getMinList() : context.getMaxList();
			AtomicReference<Double> optimumReference = context.getOptimumReference();
			
			if (factor * currentEvaluation > factor * optimalEvaluation) {
				optimalEvaluation = currentEvaluation;
				
				result.setEvaluation(optimalEvaluation);
				
				for (Entry<Integer, Cell> entry : subResult.getOptimalMoves().entrySet()) {
					result.getOptimalMoves().put(entry.getKey(), entry.getValue());
				}
				result.getOptimalMoves().put(currentDepth, analysedMove);

				if (currentDepth == 0 && factor * optimalEvaluation > factor * optimumReference.get()) {
					if (logger.isDebugEnabled()) {
						logger.debug(String.format("current best move: %s, new optimum: %d", analysedMove, (int) (factor * optimalEvaluation)));
					}
					optimumReference.set(optimalEvaluation);
					result.getOptimalMoves().put(currentDepth, analysedMove);
					result.setFinalEvaluation(optimalEvaluation);
				}
				
				optimumList.put(currentDepth, optimalEvaluation);
				
				if (isOptimumReached(currentDepth, factor, otherList, optimalEvaluation, optimumReference.get())) {
					break;
				}
			}
			if (currentDepth == context.getIndexDepth()) {
				context.getCurrentIndex().set(context.getCurrentIndex().get() + 1);
				Integer percentCompleted = context.getCurrentIndex().get() * 100 / context.getEndIndex();
				webSocketService.sendComputingProgress(percentCompleted);
			}
		}
	
		stopWatch.stop();
		
		context.getMaxList().remove(currentDepth);
		context.getMinList().remove(currentDepth);
		
		return result;
	}

	private boolean isOptimumReached(int depth, int factor, Map<Integer, Double> otherList, double eval, Double globalOptimum) {
		boolean localOtptimumReeached = otherList.entrySet().stream().anyMatch(entry -> entry.getKey() < depth && factor * eval >= factor * entry.getValue());
		
		if (localOtptimumReeached) {
			return true;
		}
		return otherList.containsKey(0) && depth > 0 && factor * eval >= factor * globalOptimum;
	}
}
