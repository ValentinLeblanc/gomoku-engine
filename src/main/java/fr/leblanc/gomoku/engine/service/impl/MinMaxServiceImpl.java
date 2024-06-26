package fr.leblanc.gomoku.engine.service.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import fr.leblanc.gomoku.engine.exception.ComputationStoppedException;
import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.EvaluationContext;
import fr.leblanc.gomoku.engine.model.GameData;
import fr.leblanc.gomoku.engine.model.GomokuColor;
import fr.leblanc.gomoku.engine.model.MinMaxContext;
import fr.leblanc.gomoku.engine.model.MinMaxResult;
import fr.leblanc.gomoku.engine.model.StrikeContext;
import fr.leblanc.gomoku.engine.model.messaging.EngineMessageType;
import fr.leblanc.gomoku.engine.model.messaging.MoveDTO;
import fr.leblanc.gomoku.engine.service.EvaluationService;
import fr.leblanc.gomoku.engine.service.GameComputationService;
import fr.leblanc.gomoku.engine.service.MinMaxService;
import fr.leblanc.gomoku.engine.service.StrikeService;
import fr.leblanc.gomoku.engine.service.ThreatService;
import fr.leblanc.gomoku.engine.service.WebSocketService;
import fr.leblanc.gomoku.engine.util.ThreadComputeUtils;

@Service
public class MinMaxServiceImpl implements MinMaxService {
	
	private static final Logger logger = LoggerFactory.getLogger(MinMaxServiceImpl.class);

	private static final int MINMAX_THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors();
	
	private ThreatService threatService;
	
	private EvaluationService evaluationService;

	private WebSocketService webSocketService;
	
	private GameComputationService computationService;
	
	private StrikeService strikeService;
	
	public MinMaxServiceImpl(ThreatService threatService, EvaluationService evaluationService,
			WebSocketService webSocketService, GameComputationService computationService, StrikeService strikeService) {
		super();
		this.threatService = threatService;
		this.evaluationService = evaluationService;
		this.webSocketService = webSocketService;
		this.computationService = computationService;
		this.strikeService = strikeService;
	}

	@Override
	public MinMaxResult computeMinMax(GameData gameData, MinMaxContext context) throws InterruptedException {
		return computeMinMax(gameData, null, context);
	}
	
	@Override
	public MinMaxResult computeMinMax(GameData gameData, List<Cell> analyzedCells, MinMaxContext context) throws InterruptedException {
		
		webSocketService.sendMessage(EngineMessageType.MINMAX_PROGRESS, context.getGameId(), 0);
		
		MinMaxResult result = null;
		
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		
		Long gameId = context.getGameId();
		int extent = context.getExtent();
		
		try {
			int maxDepth = context.getMaxDepth();
			context.setPlayingColor(GameData.extractPlayingColor(gameData));
			context.setFindMax(maxDepth % 2 == 0);
			context.setOptimumCellReference(new AtomicReference<>());

			if (analyzedCells == null) {
				analyzedCells = threatService.buildAnalyzedCells(gameData, context.getPlayingColor());
			}
			
			int emptyCellsCount = GameData.countEmptyCells(gameData);
			
			if (extent > 0) {
				
				List<MinMaxResult> extentAnalyzedResults = new ArrayList<>();
				
				context.setEndIndex(extent * analyzedCells.size() - extent * (extent - 1) / 2 + extent * (emptyCellsCount - 1));
				
				for (int i = 0; i < extent; i++) {
					if (!analyzedCells.isEmpty()) {
						result = internalMinMax(gameData, analyzedCells, context);
						if (result != null) {
							extentAnalyzedResults.add(result);
							analyzedCells.remove(result.getResultCell());
						}
					}
				}
				
				context.setIndexDepth(1);
				context.setMaxDepth(context.getMaxDepth() + 1);
				context.setFindMax(!context.isFindMax());
				
				result = internalMinMax(gameData, extentAnalyzedResults.stream().map(r -> r.getResultCell()).toList(), context);
			} else {
				context.setEndIndex(analyzedCells.size());
				result = internalMinMax(gameData, analyzedCells, context);
			}
			
			webSocketService.sendMessage(EngineMessageType.MINMAX_PROGRESS, gameId, 100);
		} catch (InterruptedException e) {
			webSocketService.sendMessage(EngineMessageType.MINMAX_PROGRESS, gameId, 0);
			Thread.currentThread().interrupt();
			throw new InterruptedException();
		} finally {
			stopWatch.stop();
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("minMax elpased time : %d ms", stopWatch.getTotalTimeMillis()));
				logger.debug(String.format("result = %s", result));
			}
			if (computationService.isDisplayAnalysis(context.getGameId()) && context.getOptimumCellReference().get() != null) {
				webSocketService.sendMessage(EngineMessageType.ANALYSIS_MOVE, context.getGameId(), new MoveDTO(context.getOptimumCellReference().get(), GomokuColor.NONE_COLOR));
			}
		}
		return result;
	}

	private MinMaxResult internalMinMax(GameData gameData, List<Cell> analysedMoves, MinMaxContext context) throws InterruptedException {
		context.setOptimumReference(context.isFindMax() ? new AtomicReference<>(Double.NEGATIVE_INFINITY) : new AtomicReference<>(Double.POSITIVE_INFINITY));
		List<MinMaxResult> results = ThreadComputeUtils.computeAll(analysedMoves, 
				cells -> new MinMaxCommand(gameData, cells, context), 
				MINMAX_THREAD_POOL_SIZE);
		try {
			results = results.stream().filter(r -> r.getFinalEvaluation() != null).collect(Collectors.toList());
			results.sort(resultsComparator(context.isFindMax()));
			if (!results.isEmpty()) {
				return results.get(0);
			}
		} catch (ComputationStoppedException e) {
			Thread.currentThread().interrupt();
			throw new InterruptedException();
		}
		throw new IllegalStateException("MinMax service failed");
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
	
	private class MinMaxCommand implements Callable<MinMaxResult> {
		
		private MinMaxContext context;
		private GameData gameData;
		private List<Cell> cells;
		
		private MinMaxCommand(GameData gameData, List<Cell> cells, MinMaxContext context) {
			this.context = new MinMaxContext(context);
			this.gameData = new GameData(gameData);
			this.cells = cells;
		}
		
		@Override
		public MinMaxResult call() throws InterruptedException {
			try {
				return recursiveMinMax(gameData, context.getPlayingColor(), cells, context.isFindMax(), 0, context);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw e;
			}
		}

	}

	private MinMaxResult recursiveMinMax(GameData gameData, int playingColor, List<Cell> analysedMoves, boolean findMax, int currentDepth, MinMaxContext context) throws InterruptedException {
		
		MinMaxResult result = new MinMaxResult();
		
		if (context.isUseStrikeService() && currentDepth < context.getMaxDepth() - 1) {
			strikeService.secondaryStrike(gameData, playingColor, new StrikeContext(context.getGameId(), 1, 1));
		}
		
		double optimalEvaluation = findMax ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
		
		for (Cell analysedMove : analysedMoves) {
			
			if (computationService.isGameComputationStopped(context.getGameId()) || Thread.interrupted()) {
				throw new InterruptedException();
			}
			
			MinMaxResult subResult = new MinMaxResult();
			double currentEvaluation = 0;
			
			gameData.addMove(analysedMove, playingColor);
			context.getCurrentMoves().add(analysedMove);
			
			if (currentDepth == context.getMaxDepth() - 1) {
				currentEvaluation = evaluationService.computeEvaluation(context.getGameId(), new EvaluationContext(gameData).internal().useStrikeService()).getEvaluation();
			} else {
				List<Cell> subAnalyzedMoves = threatService.buildAnalyzedCells(gameData, -playingColor);
				subResult = recursiveMinMax(gameData, -playingColor, subAnalyzedMoves, !findMax, currentDepth + 1, context);
				currentEvaluation = subResult.getEvaluation();
			}
			
			gameData.removeMove(analysedMove);
			context.getCurrentMoves().remove(analysedMove);
			
			Map<Integer, Double> optimumList = findMax ? context.getMaxList() : context.getMinList();
			Map<Integer, Double> otherList = findMax ? context.getMinList() : context.getMaxList();
			AtomicReference<Double> optimumReference = context.getOptimumReference();
			int factor = findMax ? 1 : -1;
			
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
					if (computationService.isDisplayAnalysis(context.getGameId())) {
						if (context.getOptimumCellReference().get() != null) {
							webSocketService.sendMessage(EngineMessageType.ANALYSIS_MOVE, context.getGameId(), new MoveDTO(context.getOptimumCellReference().get(), GomokuColor.NONE_COLOR));
						}
						context.getOptimumCellReference().set(analysedMove);
						webSocketService.sendMessage(EngineMessageType.ANALYSIS_MOVE, context.getGameId(), new MoveDTO(analysedMove, playingColor));
					}
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
				webSocketService.sendMessage(EngineMessageType.MINMAX_PROGRESS, context.getGameId(), percentCompleted);
			}
		}
	
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
