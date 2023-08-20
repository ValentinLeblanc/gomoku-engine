package fr.leblanc.gomoku.engine.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
import fr.leblanc.gomoku.engine.model.EvaluationResult;
import fr.leblanc.gomoku.engine.model.GameData;
import fr.leblanc.gomoku.engine.model.GomokuColor;
import fr.leblanc.gomoku.engine.model.StrikeContext;
import fr.leblanc.gomoku.engine.model.Threat;
import fr.leblanc.gomoku.engine.model.ThreatContext;
import fr.leblanc.gomoku.engine.model.ThreatType;
import fr.leblanc.gomoku.engine.service.CacheService;
import fr.leblanc.gomoku.engine.service.EvaluationService;
import fr.leblanc.gomoku.engine.service.GameComputationService;
import fr.leblanc.gomoku.engine.service.StrikeService;
import fr.leblanc.gomoku.engine.service.ThreatService;

@Service
public class StrikeServiceImpl implements StrikeService {

	private static final Logger logger = LoggerFactory.getLogger(StrikeServiceImpl.class);
	
	@Autowired
	private ThreatService threatService;
	
	@Autowired
	private GameComputationService computationService;
	
	@Autowired
	private CacheService cacheService;
	
	private ExecutorService executor = Executors.newSingleThreadExecutor();
	
	private static final ThreatType[][] SECONDARY_THREAT_PAIRS = { { ThreatType.DOUBLE_THREAT_3, ThreatType.DOUBLE_THREAT_3 }
		, { ThreatType.DOUBLE_THREAT_3, ThreatType.DOUBLE_THREAT_2 }
		, { ThreatType.DOUBLE_THREAT_3, ThreatType.THREAT_3 }
		, { ThreatType.THREAT_4, ThreatType.DOUBLE_THREAT_3 }
		, { ThreatType.THREAT_4, ThreatType.DOUBLE_THREAT_2 }
		};

	@Override
	public Cell directStrike(GameData gameData, int playingColor, StrikeContext strikeContext) throws InterruptedException {
	
		if (computationService.isGameComputationStopped(strikeContext.getGameId())) {
			throw new InterruptedException();
		}
		
		if (cacheService.isCacheEnabled() && cacheService.getDirectStrikeCache(strikeContext.getGameId()).containsKey(playingColor) && cacheService.getDirectStrikeCache(strikeContext.getGameId()).get(playingColor).containsKey(gameData)) {
			return cacheService.getDirectStrikeCache(strikeContext.getGameId()).get(playingColor).get(gameData).orElse(null);
		}
		
		ThreatContext playingThreatContext = gameData.getThreatContext(playingColor);
	
		// check for a threat5
		if (!playingThreatContext.getThreatsOfType(ThreatType.THREAT_5).isEmpty()) {
			Cell move = playingThreatContext.getThreatsOfType(ThreatType.THREAT_5).iterator().next().getTargetCell();
			storeInDirectStrikeCache(gameData, strikeContext, playingColor, move);
			return move;
		}
	
		ThreatContext opponentThreatContext = gameData.getThreatContext(-playingColor);
	
		// check for an opponent threat5
		if (!opponentThreatContext.getThreatsOfType(ThreatType.THREAT_5).isEmpty()) {
			
			Set<Cell> opponentThreats = new HashSet<>();
			
			opponentThreatContext.getThreatsOfType(ThreatType.THREAT_5).stream().map(Threat::getTargetCell).forEach(opponentThreats::add);
			
			if (opponentThreats.size() == 1) {
				
				Cell opponentThreat = opponentThreats.iterator().next();
				
				try {
					// defend
					gameData.addMove(opponentThreat, playingColor);
					
					// check for another threat5
					playingThreatContext = gameData.getThreatContext(playingColor);
					if (!playingThreatContext.getThreatsOfType(ThreatType.THREAT_5).isEmpty()) {
						
						Cell newThreat = playingThreatContext.getThreatsOfType(ThreatType.THREAT_5).get(0).getTargetCell();
						
						// opponent defends
						gameData.addMove(newThreat, -playingColor);
						
						// check for another strike
						Cell nextThreat = directStrike(gameData, playingColor, strikeContext);
							
						gameData.removeMove(newThreat);
						
						if (nextThreat != null) {
							storeInDirectStrikeCache(gameData, strikeContext, playingColor, opponentThreat);
							return opponentThreat;
						}
					}
				} finally {
					gameData.removeMove(opponentThreat);
				}
			}
			
			if (cacheService.isCacheEnabled()) {
				cacheService.getDirectStrikeCache(strikeContext.getGameId()).get(playingColor).put(new GameData(gameData), Optional.empty());
			}
			
			return null;
		}
	
		// check for a double threat4 move
		if (!playingThreatContext.getThreatsOfType(ThreatType.DOUBLE_THREAT_4).isEmpty()) {
			Cell targetCell = playingThreatContext.getThreatsOfType(ThreatType.DOUBLE_THREAT_4).iterator().next().getTargetCell();
			storeInDirectStrikeCache(gameData, strikeContext, playingColor, targetCell);
			return targetCell;
		}
	
		// check for threat4 moves
		List<Threat> threat4List = playingThreatContext.getThreatsOfType(ThreatType.THREAT_4);
	
		Set<Cell> cells = new HashSet<>();
		
		threat4List.stream().forEach(t -> cells.add(t.getTargetCell()));
		
		for (Cell threat4Move : cells) {
			
			gameData.addMove(threat4Move, playingColor);
			
			// opponent defends
			opponentThreatContext = gameData.getThreatContext(playingColor);
			Cell counterMove = opponentThreatContext.getThreatsOfType(ThreatType.THREAT_5).iterator().next().getTargetCell();

			gameData.addMove(counterMove, -playingColor);
			
			Cell nextAttempt = directStrike(gameData, playingColor, strikeContext);
			
			gameData.removeMove(counterMove);
			gameData.removeMove(threat4Move);
			
			if (nextAttempt != null) {
				storeInDirectStrikeCache(gameData, strikeContext, playingColor, threat4Move);
				return threat4Move;
			}
		}
		
		if (cacheService.isCacheEnabled()) {
			cacheService.getDirectStrikeCache(strikeContext.getGameId()).get(playingColor).put(new GameData(gameData), Optional.empty());
		}
	
		return null;
	}

	@Override
	public List<Cell> defendFromDirectStrike(GameData gameData, int playingColor, StrikeContext strikeContext, boolean returnFirst) throws InterruptedException {
	
		if (cacheService.isCacheEnabled() && cacheService.getCounterStrikeCache(strikeContext.getGameId()).containsKey(playingColor) && cacheService.getCounterStrikeCache(strikeContext.getGameId()).get(playingColor).containsKey(gameData)) {
			return cacheService.getCounterStrikeCache(strikeContext.getGameId()).get(playingColor).get(gameData);
		}
		
		List<Cell> defendingMoves = new ArrayList<>();
		
		ThreatContext opponentThreatContext = gameData.getThreatContext(-playingColor);
		
		if (!opponentThreatContext.getThreatsOfType(ThreatType.THREAT_5).isEmpty()) {
			defendingMoves.add(opponentThreatContext.getThreatsOfType(ThreatType.THREAT_5).iterator().next().getTargetCell());
			storeInCounterStrikeCache(gameData, strikeContext, playingColor, defendingMoves);
			return defendingMoves;
		}
		
		List<Cell> analysedMoves =  threatService.buildAnalyzedCells(gameData, playingColor);
		
		for (Cell analysedMove : analysedMoves) {
			
			try {
				gameData.addMove(analysedMove, playingColor);
				if (directStrike(gameData, -playingColor, strikeContext) == null) {
					
					 ThreatContext newThreatContext = gameData.getThreatContext(playingColor);
					
					if (!newThreatContext.getThreatsOfType(ThreatType.THREAT_5).isEmpty()) {
						
						Cell counter = newThreatContext.getThreatsOfType(ThreatType.THREAT_5).iterator().next().getTargetCell();
						
						gameData.addMove(counter, -playingColor);
						
						List<Cell> nextCounters = defendFromDirectStrike(gameData, playingColor, strikeContext, true);
						
						gameData.removeMove(counter);
						
						if (!nextCounters.isEmpty()) {
							defendingMoves.add(analysedMove);
							if (returnFirst) {
								return defendingMoves;
							}
						} else {
							storeInDirectStrikeCache(gameData, strikeContext, -playingColor, counter);
						}
						
					} else {
						defendingMoves.add(analysedMove);
						if (returnFirst) {
							return defendingMoves;
						}
					}
				}
			} finally {
				gameData.removeMove(analysedMove);
			}
		}
		
		storeInCounterStrikeCache(gameData, strikeContext, playingColor, defendingMoves);
		
		return defendingMoves;
	}

	@Override
	public Cell secondaryStrike(GameData gameData, int playingColor, StrikeContext strikeContext) throws InterruptedException {
		SecondaryStrikeCommand command = new SecondaryStrikeCommand(gameData, playingColor, strikeContext);
		
		Cell secondaryStrike = null;
		
		try {
			if (strikeContext.getStrikeTimeout() == -1) {
				secondaryStrike = command.call();
			} else {
				// work on copy as timeout may occur and corrupt the data
				command.gameData = new GameData(gameData);
				secondaryStrike = executor.invokeAny(List.of(command), strikeContext.getStrikeTimeout(), TimeUnit.SECONDS);
			}
			
		} catch (ExecutionException e) {
			
			if (e.getCause() instanceof InterruptedException interruptedException) {
				throw interruptedException;
			}
			
			logger.error("Error while SecondaryStrikeCommand : " + e.getMessage(), e);
		} catch (TimeoutException e) {
			if (logger.isDebugEnabled()) {
				logger.debug("SecondaryStrikeCommand timeout ({}s)", strikeContext.getStrikeTimeout());
			}
		}
		return secondaryStrike;
	}
	
	private class SecondaryStrikeCommand implements Callable<Cell> {

		private GameData gameData;
		private int playingColor;
		private StrikeContext strikeContext;
		
		private SecondaryStrikeCommand(GameData gameData, int playingColor, StrikeContext strikeContext) {
			this.gameData = gameData;
			this.playingColor = playingColor;
			this.strikeContext = strikeContext;
		}
		
		@Override
		public Cell call() throws InterruptedException {

			StopWatch stopWatch = new StopWatch("findOrCounterStrike");
			stopWatch.start();

			for (int currentMaxDepth = 1; currentMaxDepth <= strikeContext.getStrikeDepth(); currentMaxDepth++) {

				long timeElapsed = System.currentTimeMillis();

				Cell secondaryStrike = executeSecondaryStrike(gameData, playingColor, 0, currentMaxDepth, strikeContext);

				if (secondaryStrike != null) {
					stopWatch.stop();
					if (logger.isDebugEnabled()) {
						logger.debug("secondary strike found in {} ms for maxDepth = {}", stopWatch.getTotalTimeMillis(), currentMaxDepth);
					}
					return secondaryStrike;
				}

				if (logger.isDebugEnabled()) {
					timeElapsed = System.currentTimeMillis() - timeElapsed;
					logger.debug("secondary strike failed for maxDepth = {} ({} ms)", currentMaxDepth, timeElapsed);
				}
			}

			return null;
		}
	}
	
	private void storeInDirectStrikeCache(GameData gameData, StrikeContext strikeContext, int playingColor, Cell move) {
		if (cacheService.isCacheEnabled()) {
			if (move != null) {
				cacheService.getDirectStrikeCache(strikeContext.getGameId()).get(playingColor).put(new GameData(gameData), Optional.of(move));
				EvaluationResult evaluation = new EvaluationResult();
				evaluation.setEvaluation(EvaluationService.STRIKE_EVALUATION);
				cacheService.getEvaluationCache(strikeContext.getGameId()).get(playingColor).put(new GameData(gameData), evaluation);
				GameData newGameData = new GameData(gameData);
				newGameData.addMove(move, playingColor);
				EvaluationResult opponentEvaluation = new EvaluationResult();
				opponentEvaluation.setEvaluation(-EvaluationService.STRIKE_EVALUATION);
				cacheService.getEvaluationCache(strikeContext.getGameId()).get(-playingColor).put(newGameData, opponentEvaluation);
			} else {
				cacheService.getDirectStrikeCache(strikeContext.getGameId()).get(playingColor).put(new GameData(gameData), Optional.empty());
			}
		}
	}
	
	private void storeInSecondaryStrikeCache(GameData gameData, StrikeContext strikeContext, int playingColor, Cell move) {
		if (move != null) {
			cacheService.getSecondaryStrikeCache(strikeContext.getGameId()).get(playingColor).put(new GameData(gameData), Optional.of(move));
			EvaluationResult evaluation = new EvaluationResult();
			evaluation.setEvaluation(EvaluationService.STRIKE_EVALUATION);
			cacheService.getEvaluationCache(strikeContext.getGameId()).get(playingColor).put(new GameData(gameData), evaluation);
			GameData newGameData = new GameData(gameData);
			newGameData.addMove(move, playingColor);
			EvaluationResult opponentEvaluation = new EvaluationResult();
			opponentEvaluation.setEvaluation(-EvaluationService.STRIKE_EVALUATION);
			cacheService.getEvaluationCache(strikeContext.getGameId()).get(-playingColor).put(newGameData, opponentEvaluation);
		}
	}
	
	private void storeInCounterStrikeCache(GameData gameData, StrikeContext strikeContext, int playingColor, List<Cell> defendingMoves) {
		if (cacheService.isCacheEnabled()) {
			cacheService.getCounterStrikeCache(strikeContext.getGameId()).get(playingColor).put(new GameData(gameData), defendingMoves);
			for (int column = 0; column < gameData.getData().length; column++) {
				for (int row = 0; row < gameData.getData().length; row++) {
					if (gameData.getValue(column, row) == GomokuColor.NONE_COLOR) {
						Cell move = new Cell(column, row);
						if (!defendingMoves.contains(move)) {
							GameData newGameData = new GameData(gameData);
							newGameData.addMove(move, playingColor);
							EvaluationResult badEvaluation = new EvaluationResult();
							badEvaluation.setEvaluation(-EvaluationService.STRIKE_EVALUATION);
							cacheService.getEvaluationCache(strikeContext.getGameId()).get(playingColor).put(newGameData, badEvaluation);
						}
					}
				}
			}
		}
	}
	
	private Cell executeSecondaryStrike(GameData gameData, int playingColor, int depth, int maxDepth, StrikeContext strikeContext) throws InterruptedException {

		if (computationService.isGameComputationStopped(strikeContext.getGameId())) {
			throw new InterruptedException();
		}
		
		if (cacheService.isCacheEnabled() && cacheService.getSecondaryStrikeCache(strikeContext.getGameId()).containsKey(playingColor) && cacheService.getSecondaryStrikeCache(strikeContext.getGameId()).get(playingColor).containsKey(gameData)) {
			return cacheService.getSecondaryStrikeCache(strikeContext.getGameId()).get(playingColor).get(gameData).orElse(null);
		}
		
		if (depth == maxDepth) {
			return null;
		}
		
		// check for a strike
		Cell directStrike = directStrike(gameData, playingColor, strikeContext);

		if (directStrike != null) {
			return directStrike;
		}
		
		// check for an opponent strike
		Cell opponentDirectStrike = directStrike(gameData, -playingColor, strikeContext);

		if (opponentDirectStrike != null) {
			return defendThenSecondaryStrike(gameData, playingColor, depth, maxDepth, strikeContext);
		}
		
		ThreatContext threatContext = gameData.getThreatContext(playingColor);

		Set<Cell> newSet = threatContext.getThreatsOfType(ThreatType.THREAT_4).stream().map(t -> t.getTargetCell()).collect(Collectors.toSet());
		
		Cell retry = secondaryWithGivenSet(gameData, playingColor, newSet, depth, maxDepth, strikeContext);
		if (retry != null) {
			return retry;
		}
		
		for (ThreatType[] secondaryThreatTypePair : SECONDARY_THREAT_PAIRS) {
			retry = secondaryWithGivenSet(gameData, playingColor, threatService.findCombinedThreats(threatContext, secondaryThreatTypePair[0], secondaryThreatTypePair[1]), depth, maxDepth, strikeContext);
			
			if (retry != null) {
				return retry;
			}
		}
		
		newSet = threatContext.getThreatsOfType(ThreatType.DOUBLE_THREAT_3).stream().map(Threat::getTargetCell).collect(Collectors.toSet());
		
		retry = secondaryWithGivenSet(gameData, playingColor, newSet, depth, maxDepth, strikeContext);
		
		if (retry != null) {
			return retry;
		}
		
		return null;
	}

	private Cell defendThenSecondaryStrike(GameData gameData, int playingColor, int depth, int maxDepth, StrikeContext strikeContext) throws InterruptedException {
		List<Cell> defendFromStrikes = defendFromDirectStrike(gameData, playingColor, strikeContext, false);

		// defend
		for (Cell defendFromStrike : defendFromStrikes) {
			
			try  {
				
				gameData.addMove(defendFromStrike, playingColor);
				
				// check for a new strike
				Cell newStrike = directStrike(gameData, playingColor, strikeContext);
				
				if (newStrike != null) {
					List<Cell> opponentDefendFromStrikes = defendFromDirectStrike(gameData, -playingColor, strikeContext, false);
					
					boolean hasDefense = false;
					
					for (Cell opponentDefendFromStrike : opponentDefendFromStrikes) {
						
						gameData.addMove(opponentDefendFromStrike, -playingColor);
						
						Cell newAttempt = executeSecondaryStrike(gameData, playingColor, depth + 1, maxDepth, strikeContext);
						
						if (newAttempt != null) {
							storeInSecondaryStrikeCache(gameData, strikeContext, playingColor, newAttempt);
						}
						
						gameData.removeMove(opponentDefendFromStrike);
						
						if (newAttempt == null) {
							hasDefense = true;
							break;
						}
					}
					
					if (!hasDefense) {
						storeInSecondaryStrikeCache(gameData, strikeContext, playingColor, defendFromStrike);
						return defendFromStrike;
					}
				}
				
			} finally {
				gameData.removeMove(defendFromStrike);
			}
		}

		return null;
	}
	
	private Cell secondaryWithGivenSet(GameData gameData, int playingColor, Set<Cell> cellsToTry, int depth, int maxDepth, StrikeContext strikeContext) throws InterruptedException {
		
		if (Thread.currentThread().isInterrupted()) {
			throw new InterruptedException();
		}
		
		for (Cell threat : cellsToTry) {
			gameData.addMove(threat, playingColor);
			
			List<Cell> opponentDefendFromStrikes = defendFromDirectStrike(gameData, -playingColor, strikeContext, false);
			
			boolean hasDefense = false;
			
			for (Cell opponentDefendFromStrike : opponentDefendFromStrikes) {
				gameData.addMove(opponentDefendFromStrike, -playingColor);
				
				Cell newAttempt = executeSecondaryStrike(gameData, playingColor, depth + 1, maxDepth, strikeContext);
				
				if (newAttempt != null) {
					storeInSecondaryStrikeCache(gameData, strikeContext, playingColor, newAttempt);
				}
				
				gameData.removeMove(opponentDefendFromStrike);
				
				if (newAttempt == null) {
					hasDefense = true;
					break;
				}
			}
			
			gameData.removeMove(threat);
			
			if (!hasDefense) {
				storeInSecondaryStrikeCache(gameData, strikeContext, playingColor, threat);
				return threat;
			}
		}
		
		return null;
		
	}

}
