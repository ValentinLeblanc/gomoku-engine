package fr.leblanc.gomoku.engine.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.DoubleThreat;
import fr.leblanc.gomoku.engine.model.GameData;
import fr.leblanc.gomoku.engine.model.StrikeContext;
import fr.leblanc.gomoku.engine.model.StrikeResult;
import fr.leblanc.gomoku.engine.model.StrikeResult.StrikeType;
import fr.leblanc.gomoku.engine.model.Threat;
import fr.leblanc.gomoku.engine.model.ThreatContext;
import fr.leblanc.gomoku.engine.model.ThreatType;
import fr.leblanc.gomoku.engine.service.MinMaxService;
import fr.leblanc.gomoku.engine.service.StrikeService;
import fr.leblanc.gomoku.engine.service.ThreatContextService;
import fr.leblanc.gomoku.engine.util.cache.GomokuCache;
import fr.leblanc.gomoku.engine.util.cache.L2CacheSupport;

@Service
public class StrikeServiceImpl implements StrikeService {

	private static final Logger logger = LoggerFactory.getLogger(StrikeServiceImpl.class);
	
	@Autowired
	private ThreatContextService threatContextService;
	
	@Autowired
	private MinMaxService minMaxService;
	
	private Boolean isComputing = false;
	
	private Boolean stopComputation = false;
	
	private ExecutorService executor = Executors.newSingleThreadExecutor();
	
	private static final ThreatType[][] SECONDARY_THREAT_PAIRS = { { ThreatType.DOUBLE_THREAT_3, ThreatType.DOUBLE_THREAT_3 }
		, { ThreatType.DOUBLE_THREAT_3, ThreatType.DOUBLE_THREAT_2 }
		, { ThreatType.DOUBLE_THREAT_3, ThreatType.THREAT_3 }
		, { ThreatType.THREAT_4, ThreatType.DOUBLE_THREAT_3 }
		, { ThreatType.THREAT_4, ThreatType.DOUBLE_THREAT_2 }
		};

	@Override
	public StrikeResult processStrike(GameData dataWrapper, int playingColor, StrikeContext strikeContext) throws InterruptedException {
		
		isComputing = true;
		
		try {
			StopWatch stopWatch = new StopWatch("processStrike");
			stopWatch.start();
			
			if (logger.isDebugEnabled()) {
				logger.debug("find direct strike...");
			}
			
			Cell directThreat = directStrike(dataWrapper, playingColor, strikeContext);
			
			if (directThreat != null) {
				
				stopWatch.stop();
				
				if (logger.isDebugEnabled()) {
					logger.debug("direct strike found in " + stopWatch.getTotalTimeMillis() + " ms");
				}
				
				return new StrikeResult(directThreat, StrikeType.DIRECT_STRIKE);
			}
			
			Cell opponentDirectThreat = directStrike(dataWrapper, -playingColor, strikeContext);
			
			if (opponentDirectThreat != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("defend from opponent direct strike...");
				}
				List<Cell> counterOpponentThreats = counterDirectStrikeMoves(dataWrapper, playingColor, strikeContext, false);
				
				if (!counterOpponentThreats.isEmpty()) {
					
					Cell defense = counterOpponentThreats.get(0);
					
					if (counterOpponentThreats.size() > 1) {
						defense = minMaxService.computeMinMax(dataWrapper, counterOpponentThreats, strikeContext.getMinMaxDepth(), 0).getOptimalMoves().get(0);
					}
					
					stopWatch.stop();
					
					if (logger.isDebugEnabled()) {
						logger.debug("best defense found in " + stopWatch.getTotalTimeMillis() + " ms");
					}
					
					return new StrikeResult(defense, StrikeType.DEFEND_STRIKE);
				}
				
				return new StrikeResult(null, StrikeType.EMPTY_STRIKE);
			}
			
			if (logger.isDebugEnabled()) {
				logger.debug("find secondary strike...");
			}
			
			Cell secondaryStrike = executeSecondaryStrikeCommand(dataWrapper, playingColor, strikeContext);
			
			if (secondaryStrike != null) {
				return new StrikeResult(secondaryStrike, StrikeType.SECONDARY_STRIKE);
			}
		} finally {
			isComputing = false;
		}
		
		return new StrikeResult(null, StrikeType.EMPTY_STRIKE);
	}

	private Cell executeSecondaryStrikeCommand(GameData dataWrapper, int playingColor, StrikeContext strikeContext) throws InterruptedException {
		SecondaryStrikeCommand command = new SecondaryStrikeCommand(dataWrapper, playingColor, strikeContext, L2CacheSupport.getCurrentCache());
		
		Cell secondaryStrike = null;
		
		try {

			if (strikeContext.getStrikeTimeout() == -1) {
				secondaryStrike = command.call();
			} else {
				secondaryStrike = executor.invokeAny(List.of(command), strikeContext.getStrikeTimeout(), TimeUnit.SECONDS);
			}
			
		} catch (ExecutionException e) {
			
			if (e.getCause() instanceof InterruptedException interruptedException) {
				throw interruptedException;
			}
			
			logger.error("Error while SecondaryStrikeCommand : " + e.getMessage(), e);
		} catch (TimeoutException e) {
			logger.info("SecondaryStrikeCommand timeout (" + strikeContext.getStrikeTimeout() + "s)");
		}
		return secondaryStrike;
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
	
	private class SecondaryStrikeCommand implements Callable<Cell> {

		private GameData dataWrapper;
		private int playingColor;
		private StrikeContext strikeContext;
		private GomokuCache gomokuCache;
		
		private SecondaryStrikeCommand(GameData dataWrapper, int playingColor, StrikeContext strikeContext, GomokuCache gomokuCache) {
			this.dataWrapper = dataWrapper;
			this.playingColor = playingColor;
			this.strikeContext = strikeContext;
			this.gomokuCache = gomokuCache;
		}
		
		@Override
		public Cell call() throws InterruptedException {

			return L2CacheSupport.doInCacheContext(() -> {

				StopWatch stopWatch = new StopWatch("findOrCounterStrike");
				stopWatch.start();

				for (int currentMaxDepth = 1; currentMaxDepth <= strikeContext.getStrikeDepth(); currentMaxDepth++) {

					long timeElapsed = System.currentTimeMillis();

					Cell secondaryStrike;
					secondaryStrike = secondaryStrike(dataWrapper, playingColor, 0, currentMaxDepth, strikeContext);

					if (secondaryStrike != null) {
						stopWatch.stop();
						if (logger.isDebugEnabled()) {
							logger.debug("secondary strike found in " + stopWatch.getTotalTimeMillis() + " ms for maxDepth = " + currentMaxDepth);
						}
						return secondaryStrike;
					}

					if (logger.isDebugEnabled()) {
						timeElapsed = System.currentTimeMillis() - timeElapsed;
						logger.debug("secondary strike failed for maxDepth = " + currentMaxDepth + " (" + timeElapsed + " ms)");
					}
				}

				return null;
			}, gomokuCache);
			
		}
		
	}

	private Cell directStrike(GameData dataWrapper, int playingColor, StrikeContext strikeContext) throws InterruptedException {

		if (Boolean.TRUE.equals(stopComputation)) {
			stopComputation = false;
			throw new InterruptedException();
		}
		
		if (L2CacheSupport.isCacheEnabled() && L2CacheSupport.getDirectStrikeAttempts().containsKey(playingColor) && L2CacheSupport.getDirectStrikeAttempts().get(playingColor).containsKey(dataWrapper)) {
			return L2CacheSupport.getDirectStrikeAttempts().get(playingColor).get(dataWrapper).orElse(null);
		}
		
		ThreatContext computeThreatContext = threatContextService.computeThreatContext(dataWrapper, playingColor);
		
		Map<ThreatType, List<Threat>> threatMap = computeThreatContext.getThreatTypeToThreatMap();
		
		Map<ThreatType, Set<DoubleThreat>> doubleThreatMap = computeThreatContext.getDoubleThreatTypeToThreatMap();

		// check for a threat5
		if (!threatMap.get(ThreatType.THREAT_5).isEmpty()) {
			
			Cell move = threatMap.get(ThreatType.THREAT_5).iterator().next().getEmptyCells().iterator().next();
			
			if (L2CacheSupport.isCacheEnabled()) {
				L2CacheSupport.getDirectStrikeAttempts().get(playingColor).put(new GameData(dataWrapper), Optional.of(move));
			}
			
			return move;
		}

		Map<ThreatType, List<Threat>> opponentThreatMap = threatContextService.computeThreatContext(dataWrapper, -playingColor).getThreatTypeToThreatMap();

		// check for an opponent threat5
		if (!opponentThreatMap.get(ThreatType.THREAT_5).isEmpty()) {
			
			Set<Cell> opponentThreats = new HashSet<>();
			
			opponentThreatMap.get(ThreatType.THREAT_5).stream().forEach(t -> t.getEmptyCells().stream().forEach(opponentThreats::add));
			
			if (opponentThreats.size() == 1) {
				
				Cell opponentThreat = opponentThreats.iterator().next();
				
				try {
					// defend
					dataWrapper.addMove(opponentThreat, playingColor);
					
					// check for another threat5
					threatMap = threatContextService.computeThreatContext(dataWrapper, playingColor).getThreatTypeToThreatMap();
					if (!threatMap.get(ThreatType.THREAT_5).isEmpty()) {
						
						Cell newThreat = threatMap.get(ThreatType.THREAT_5).iterator().next().getEmptyCells().iterator().next();
						
						// opponent defends
						dataWrapper.addMove(newThreat, -playingColor);
						
						// check for another strike
						Cell nextThreat = directStrike(dataWrapper, playingColor, strikeContext);
						
						dataWrapper.removeMove(newThreat);
						
						if (nextThreat != null) {
							if (L2CacheSupport.isCacheEnabled()) {
								L2CacheSupport.getDirectStrikeAttempts().get(playingColor).put(new GameData(dataWrapper), Optional.of(opponentThreat));
							}
							return opponentThreat;
						}
					}
				} finally {
					dataWrapper.removeMove(opponentThreat);
				}
			}
			
			if (L2CacheSupport.isCacheEnabled()) {
				L2CacheSupport.getDirectStrikeAttempts().get(playingColor).put(new GameData(dataWrapper), Optional.empty());
			}
			
			return null;
		}

		// check for a double threat4 move
		if (!doubleThreatMap.get(ThreatType.DOUBLE_THREAT_4).isEmpty()) {
			if (L2CacheSupport.isCacheEnabled()) {
				L2CacheSupport.getDirectStrikeAttempts().get(playingColor).put(new GameData(dataWrapper), Optional.of(doubleThreatMap.get(ThreatType.DOUBLE_THREAT_4).iterator().next().getTargetCell()));
			}
			return doubleThreatMap.get(ThreatType.DOUBLE_THREAT_4).iterator().next().getTargetCell();
		}

		// check for threat4 moves
		List<Threat> threat4List = threatMap.get(ThreatType.THREAT_4);

		Set<Cell> cells = new HashSet<>();
		
		threat4List.stream().forEach(t -> cells.addAll(t.getEmptyCells()));
		
		for (Cell threat4Move : cells) {

			dataWrapper.addMove(threat4Move, playingColor);
			
			// opponent defends
			opponentThreatMap = threatContextService.computeThreatContext(dataWrapper, playingColor).getThreatTypeToThreatMap();

			Cell counterMove = opponentThreatMap.get(ThreatType.THREAT_5).iterator().next().getEmptyCells().iterator().next();

			dataWrapper.addMove(counterMove, -playingColor);
			
			Cell nextAttempt = directStrike(dataWrapper, playingColor, strikeContext);

			dataWrapper.removeMove(counterMove);
			dataWrapper.removeMove(threat4Move);
			
			if (nextAttempt != null) {
				if (L2CacheSupport.isCacheEnabled()) {
					L2CacheSupport.getDirectStrikeAttempts().get(playingColor).put(new GameData(dataWrapper), Optional.of(threat4Move));
				}
				return threat4Move;
			}
		}
		
		if (L2CacheSupport.isCacheEnabled()) {
			L2CacheSupport.getDirectStrikeAttempts().get(playingColor).put(new GameData(dataWrapper), Optional.empty());
		}

		return null;
	}
	
	private List<Cell> counterDirectStrikeMoves(GameData dataWrapper, int playingColor, StrikeContext strikeContext, boolean onlyOne) throws InterruptedException {
	
		if (L2CacheSupport.isCacheEnabled() && L2CacheSupport.getRecordedCounterMoves().containsKey(playingColor) && L2CacheSupport.getRecordedCounterMoves().get(playingColor).containsKey(dataWrapper)) {
			return L2CacheSupport.getRecordedCounterMoves().get(playingColor).get(dataWrapper);
		}
		
		List<Cell> defendingMoves = new ArrayList<>();
		
		ThreatContext opponentThreatContext = threatContextService.computeThreatContext(dataWrapper, -playingColor);
		
		Map<ThreatType, List<Threat>> opponentThreatMap = opponentThreatContext.getThreatTypeToThreatMap();
		
		if (!opponentThreatMap.get(ThreatType.THREAT_5).isEmpty()) {
			defendingMoves.add(opponentThreatMap.get(ThreatType.THREAT_5).iterator().next().getEmptyCells().iterator().next());
			return defendingMoves;
		}
		
		List<Cell> analysedMoves =  threatContextService.buildAnalyzedCells(dataWrapper, playingColor);
		
		for (Cell analysedMove : analysedMoves) {
			
			try {
				dataWrapper.addMove(analysedMove, playingColor);
				if (directStrike(dataWrapper, -playingColor, strikeContext) == null) {
					
					Map<ThreatType, List<Threat>> newThreatContext = threatContextService.computeThreatContext(dataWrapper, playingColor).getThreatTypeToThreatMap();
					
					if (!newThreatContext.get(ThreatType.THREAT_5).isEmpty()) {
						
						Cell counter = newThreatContext.get(ThreatType.THREAT_5).iterator().next().getEmptyCells().iterator().next();
						
						try {
							dataWrapper.addMove(counter, -playingColor);
							
							List<Cell> nextCounters = counterDirectStrikeMoves(dataWrapper, playingColor, strikeContext, true);
							
							if (!nextCounters.isEmpty()) {
								defendingMoves.add(analysedMove);
								if (onlyOne) {
									return defendingMoves;
								}
							}
						} finally {
							dataWrapper.removeMove(counter);
						}
						
					} else {
						defendingMoves.add(analysedMove);
						if (onlyOne) {
							return defendingMoves;
						}
					}
				}
			} finally {
				dataWrapper.removeMove(analysedMove);
			}
		}
		
		if (L2CacheSupport.isCacheEnabled()) {
			L2CacheSupport.getRecordedCounterMoves().get(playingColor).put(new GameData(dataWrapper), defendingMoves);
		}
		
		return defendingMoves;
	}

	private Cell secondaryStrike(GameData dataWrapper, int playingColor, int depth, int maxDepth, StrikeContext strikeContext) throws InterruptedException {

		if (Thread.currentThread().isInterrupted()) {
			throw new InterruptedException();
		}
		
		if (depth == maxDepth) {
			return null;
		}
		
		if (L2CacheSupport.isCacheEnabled() && L2CacheSupport.getSecondaryStrikeAttempts().containsKey(playingColor) && L2CacheSupport.getSecondaryStrikeAttempts().get(playingColor).containsKey(dataWrapper)) {
			return L2CacheSupport.getSecondaryStrikeAttempts().get(playingColor).get(dataWrapper).orElse(null);
		}
		
		// check for a strike
		Cell directStrike = directStrike(dataWrapper, playingColor, strikeContext);

		if (directStrike != null) {
			return directStrike;
		}
		
		// check for an opponent strike
		Cell opponentDirectStrike = directStrike(dataWrapper, -playingColor, strikeContext);

		if (opponentDirectStrike != null) {
			
			Cell defend = defendThenSecondaryStrike(dataWrapper, playingColor, depth, maxDepth, strikeContext);
			
			if (L2CacheSupport.isCacheEnabled()) {
				if (defend == null) {
					if (depth + 1 == strikeContext.getStrikeDepth()) {
						L2CacheSupport.getSecondaryStrikeAttempts().get(playingColor).put(new GameData(dataWrapper), Optional.empty());
					}
				} else {
					L2CacheSupport.getSecondaryStrikeAttempts().get(playingColor).put(new GameData(dataWrapper), Optional.of(defend));
				}
			}
			
			return defend;
			
		}
		
		ThreatContext threatContext = threatContextService.computeThreatContext(dataWrapper, playingColor);

		for (ThreatType[] secondaryThreatTypePair : SECONDARY_THREAT_PAIRS) {
			Cell retry = secondaryWithGivenSet(dataWrapper, playingColor, threatContextService.findCombinedThreats(threatContext, secondaryThreatTypePair[0], secondaryThreatTypePair[1]), depth, maxDepth, strikeContext);
			
			if (retry != null) {
				if (L2CacheSupport.isCacheEnabled()) {
					L2CacheSupport.getSecondaryStrikeAttempts().get(playingColor).put(new GameData(dataWrapper), Optional.of(retry));
				}
				return retry;
			}
		}
		
		Cell retry = secondaryWithGivenSet(dataWrapper, playingColor, threatContext.getDoubleThreatTypeToThreatMap().get(ThreatType.DOUBLE_THREAT_3).stream().map(DoubleThreat::getTargetCell).collect(Collectors.toSet()), depth, maxDepth, strikeContext);
		
		if (retry != null) {
			if (L2CacheSupport.isCacheEnabled()) {
				L2CacheSupport.getSecondaryStrikeAttempts().get(playingColor).put(new GameData(dataWrapper), Optional.of(retry));
			}
			return retry;
		}
		
		if (L2CacheSupport.isCacheEnabled() && depth + 1 == strikeContext.getStrikeDepth()) {
			L2CacheSupport.getSecondaryStrikeAttempts().get(playingColor).put(new GameData(dataWrapper), Optional.empty());
		}
		
		return null;
	}

	private Cell defendThenSecondaryStrike(GameData dataWrapper, int playingColor, int depth, int maxDepth, StrikeContext strikeContext) throws InterruptedException {
		List<Cell> defendFromStrikes = counterDirectStrikeMoves(dataWrapper, playingColor, strikeContext, false);

		// defend
		for (Cell defendFromStrike : defendFromStrikes) {
			
			try  {
				
				dataWrapper.addMove(defendFromStrike, playingColor);
				
				// check for a new strike
				Cell newStrike = directStrike(dataWrapper, playingColor, strikeContext);
				
				if (newStrike != null) {
					List<Cell> opponentDefendFromStrikes = counterDirectStrikeMoves(dataWrapper, -playingColor, strikeContext, false);
					
					boolean hasDefense = false;
					
					for (Cell opponentDefendFromStrike : opponentDefendFromStrikes) {
						
						dataWrapper.addMove(opponentDefendFromStrike, -playingColor);
						
						Cell newAttempt = secondaryStrike(dataWrapper, playingColor, depth + 1, maxDepth, strikeContext);
						
						dataWrapper.removeMove(opponentDefendFromStrike);
						
						if (newAttempt == null) {
							hasDefense = true;
							break;
						}
					}
					
					if (!hasDefense) {
						return defendFromStrike;
					}
				}
				
			} finally {
				dataWrapper.removeMove(defendFromStrike);
			}
		}

		return null;
	}
	
	private Cell secondaryWithGivenSet(GameData dataWrapper, int playingColor, Set<Cell> cellsToTry, int depth, int maxDepth, StrikeContext strikeContext) throws InterruptedException {
		
		if (Thread.currentThread().isInterrupted()) {
			throw new InterruptedException();
		}
		
		for (Cell threat : cellsToTry) {
			dataWrapper.addMove(threat, playingColor);
			
			List<Cell> opponentDefendFromStrikes = counterDirectStrikeMoves(dataWrapper, -playingColor, strikeContext, false);
			
			boolean hasDefense = false;
			
			for (Cell opponentDefendFromStrike : opponentDefendFromStrikes) {
				dataWrapper.addMove(opponentDefendFromStrike, -playingColor);
				
				Cell newAttempt = secondaryStrike(dataWrapper, playingColor, depth + 1, maxDepth, strikeContext);
				
				dataWrapper.removeMove(opponentDefendFromStrike);
				
				if (newAttempt == null) {
					hasDefense = true;
					break;
				}
			}
			
			dataWrapper.removeMove(threat);
			
			if (!hasDefense) {
				return threat;
			}
		}
		
		return null;
		
	}

}
