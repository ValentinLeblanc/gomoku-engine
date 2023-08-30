package fr.leblanc.gomoku.engine.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
import fr.leblanc.gomoku.engine.util.ThreadComputeUtils;

@Service
public class StrikeServiceImpl implements StrikeService {

	private static final Logger logger = LoggerFactory.getLogger(StrikeServiceImpl.class);
	
	@Autowired
	private ThreatService threatService;
	
	@Autowired
	private GameComputationService computationService;
	
	@Autowired
	private CacheService cacheService;
	
	private static final ThreatType[][] SECONDARY_THREAT_PAIRS = { { ThreatType.DOUBLE_THREAT_3, ThreatType.DOUBLE_THREAT_3 }
		, { ThreatType.DOUBLE_THREAT_3, ThreatType.DOUBLE_THREAT_2 }
		, { ThreatType.DOUBLE_THREAT_3, ThreatType.THREAT_3 }
		, { ThreatType.THREAT_4, ThreatType.DOUBLE_THREAT_3 }
		, { ThreatType.THREAT_4, ThreatType.DOUBLE_THREAT_2 }
		};
	
	private static final int SECONDARY_STRIKE_THREAD_POOL_SIZE = 2;
	private static final int COUNTER_STRIKE_THREAD_POOL_SIZE = 3;

	@Override
	public Cell directStrike(GameData gameData, int playingColor, StrikeContext strikeContext) throws InterruptedException {
		return internalDirectStrike(gameData, playingColor, strikeContext);
	}
	
	private Cell internalDirectStrike(GameData gameData, int playingColor, StrikeContext strikeContext) throws InterruptedException {
		if (computationService.isGameComputationStopped(strikeContext.getGameId()) || Thread.interrupted()) {
			throw new InterruptedException();
		}
		if (hasInDirectStrikeCache(gameData, playingColor, strikeContext)) {
			return cacheService.getDirectStrikeCache(strikeContext.getGameId()).get(playingColor).get(gameData).orElse(null);
		}
		ThreatContext playingThreatContext = threatService.getOrUpdateThreatContext(gameData, playingColor);
		// check for a threat5
		if (!playingThreatContext.getThreatsOfType(ThreatType.THREAT_5).isEmpty()) {
			Cell move = playingThreatContext.getThreatsOfType(ThreatType.THREAT_5).iterator().next().getTargetCell();
			storeInDirectStrikeCache(gameData, strikeContext, playingColor, move);
			return move;
		}
		ThreatContext opponentThreatContext = threatService.getOrUpdateThreatContext(gameData, -playingColor);
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
					playingThreatContext = threatService.getOrUpdateThreatContext(gameData, playingColor);
					if (!playingThreatContext.getThreatsOfType(ThreatType.THREAT_5).isEmpty()) {
						Cell newThreat = playingThreatContext.getThreatsOfType(ThreatType.THREAT_5).get(0).getTargetCell();
						// opponent defends
						gameData.addMove(newThreat, -playingColor);
						// check for another strike
						Cell nextThreat = internalDirectStrike(gameData, playingColor, strikeContext);
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
			storeInDirectStrikeCache(gameData, strikeContext, playingColor, null);
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
			opponentThreatContext = threatService.getOrUpdateThreatContext(gameData, playingColor);
			Cell counterMove = opponentThreatContext.getThreatsOfType(ThreatType.THREAT_5).iterator().next().getTargetCell();
			gameData.addMove(counterMove, -playingColor);
			Cell nextAttempt = internalDirectStrike(gameData, playingColor, strikeContext);
			gameData.removeMove(counterMove);
			gameData.removeMove(threat4Move);
			if (nextAttempt != null) {
				storeInDirectStrikeCache(gameData, strikeContext, playingColor, threat4Move);
				return threat4Move;
			}
		}
		storeInDirectStrikeCache(gameData, strikeContext, playingColor, null);
		return null;
	}

	@Override
	public List<Cell> defendFromDirectStrike(GameData gameData, int playingColor, StrikeContext strikeContext, boolean returnFirst) throws InterruptedException {
		return internalDefendFromDirectStrike(gameData, playingColor, strikeContext, returnFirst);
	}
	
	private List<Cell> internalDefendFromDirectStrike(GameData gameData, int playingColor, StrikeContext strikeContext, boolean returnFirst) throws InterruptedException {
		if (hasInCounterStrikeCache(gameData, strikeContext, playingColor)) {
			return cacheService.getCounterStrikeCache(strikeContext.getGameId()).get(playingColor).get(gameData);
		}
		List<Cell> results = new ArrayList<>();
		ThreatContext opponentThreatContext = threatService.getOrUpdateThreatContext(gameData, -playingColor);
		if (!opponentThreatContext.getThreatsOfType(ThreatType.THREAT_5).isEmpty()) {
			results.add(opponentThreatContext.getThreatsOfType(ThreatType.THREAT_5).iterator().next().getTargetCell());
		} else {
			List<Cell> analyzedCells =  threatService.buildAnalyzedCells(gameData, playingColor);
			results = ThreadComputeUtils.computeAll(analyzedCells, 
					cells -> new DefendFromDirectStrikeCommand(gameData, playingColor, strikeContext, cells, returnFirst), 
					COUNTER_STRIKE_THREAD_POOL_SIZE)
					.stream().flatMap(Collection::stream).toList();
		}
		storeInCounterStrikeCache(gameData, strikeContext, playingColor, results);
		return results;
	}

	private class DefendFromDirectStrikeCommand implements Callable<List<Cell>> {
		
		private GameData gameData;
		private int playingColor;
		private StrikeContext strikeContext;
		private List<Cell> analyzedCells;
		private boolean returnFirst;
		
		private DefendFromDirectStrikeCommand(GameData gameData, int playingColor, StrikeContext strikeContext, List<Cell> analyzedCells, boolean returnFirst) {
			this.gameData = new GameData(gameData);
			this.playingColor = playingColor;
			this.strikeContext = strikeContext;
			this.analyzedCells = analyzedCells;
			this.returnFirst = returnFirst;
		}
		
		@Override
		public List<Cell> call() throws Exception {
			if (hasInCounterStrikeCache(gameData, strikeContext, playingColor)) {
				return cacheService.getCounterStrikeCache(strikeContext.getGameId()).get(playingColor).get(gameData);
			}
			List<Cell> defendingMoves = new ArrayList<>();
			for (Cell analyzedCell : analyzedCells) {
				try {
					gameData.addMove(analyzedCell, playingColor);
					if (internalDirectStrike(gameData, -playingColor, strikeContext) == null) {
						 ThreatContext newThreatContext = threatService.getOrUpdateThreatContext(gameData, playingColor);
						if (!newThreatContext.getThreatsOfType(ThreatType.THREAT_5).isEmpty()) {
							Cell counter = newThreatContext.getThreatsOfType(ThreatType.THREAT_5).iterator().next().getTargetCell();
							gameData.addMove(counter, -playingColor);
							List<Cell> nextCounters = internalDefendFromDirectStrike(gameData, playingColor, strikeContext, true);
							gameData.removeMove(counter);
							if (!nextCounters.isEmpty()) {
								defendingMoves.add(analyzedCell);
								if (returnFirst) {
									return defendingMoves;
								}
							} else {
								storeInDirectStrikeCache(gameData, strikeContext, -playingColor, counter);
							}
						} else {
							defendingMoves.add(analyzedCell);
							if (returnFirst) {
								return defendingMoves;
							}
						}
					}
				} finally {
					gameData.removeMove(analyzedCell);
				}
			}
			return defendingMoves;
		}
	}
	@Override
	public Cell secondaryStrike(GameData gameData, int playingColor, StrikeContext strikeContext) throws InterruptedException {
		int maxDepth = 0;
		Cell result = null;
		while (result == null && maxDepth < 4) {
			maxDepth++;
			if (logger.isDebugEnabled()) {
				logger.debug("TRY NEXT DEPTH: {}", maxDepth);
			}
			strikeContext.setStrikeDepth(maxDepth);
			result = internalSecondaryStrike(gameData, playingColor, strikeContext, 0);
		}
		return result;
	}

	private Cell internalSecondaryStrike(GameData gameData, int playingColor, StrikeContext strikeContext, int depth) throws InterruptedException {
		if (hasInSecondaryCache(gameData, playingColor, strikeContext)) {
			return cacheService.getSecondaryStrikeCache(strikeContext.getGameId()).get(playingColor).get(gameData).orElse(null);
		}
		if (depth == strikeContext.getStrikeDepth()) {
			return null;
		}
		List<Cell> analyzedCells = extractAnalyzedCells(gameData, playingColor);
		return ThreadComputeUtils.computeFirst(analyzedCells,
				cells -> new SecondaryStrikeCommand(depth, gameData, playingColor, strikeContext, cells), strikeContext.getStrikeTimeout(),
				result -> storeInSecondaryStrikeCache(gameData, strikeContext, playingColor, result), 
				SECONDARY_STRIKE_THREAD_POOL_SIZE);
	}

	private class SecondaryStrikeCommand implements Callable<Cell> {

		private int depth;
		private GameData gameData;
		private int playingColor;
		private StrikeContext strikeContext;
		private List<Cell> analyzedCells;
		
		private SecondaryStrikeCommand(int depth, GameData gameData, int playingColor, StrikeContext strikeContext, List<Cell> analyzedCells) {
			this.depth = depth;
			this.gameData = new GameData(gameData);
			this.playingColor = playingColor;
			this.strikeContext = strikeContext;
			this.analyzedCells = analyzedCells;
		}
		
		@Override
		public Cell call() throws Exception {
			if (computationService.isGameComputationStopped(strikeContext.getGameId()) || Thread.interrupted()) {
				throw new InterruptedException();
			}
			if (hasInSecondaryCache(gameData, playingColor, strikeContext)) {
				return cacheService.getSecondaryStrikeCache(strikeContext.getGameId()).get(playingColor).get(gameData).orElse(null);
			}
			// check for a strike
			Cell directStrike = internalDirectStrike(gameData, playingColor, strikeContext);
			if (directStrike != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("[{}] Direct strike found: {}", depth, directStrike);
				}
				return directStrike;
			}
			// check for an opponent strike
			Cell opponentDirectStrike = internalDirectStrike(gameData, -playingColor, strikeContext);
			if (opponentDirectStrike != null) {
				return defendThenSecondaryStrike(gameData, playingColor, strikeContext, depth);
			}
			for (Cell cell : analyzedCells) {
				if (logger.isDebugEnabled()) {
					logger.debug("[{}] Try attack: {}", depth, cell);
				}
				gameData.addMove(cell, playingColor);
				List<Cell> opponentDefendFromStrikes = internalDefendFromDirectStrike(gameData, -playingColor, strikeContext, false);
				boolean hasDefense = false;
				for (Cell opponentDefendFromStrike : opponentDefendFromStrikes) {
					gameData.addMove(opponentDefendFromStrike, -playingColor);
					if (logger.isDebugEnabled()) {
						logger.debug("[{}] Try defense: {}", depth, opponentDefendFromStrike);
					}
					Cell nextSecondaryStrike = internalSecondaryStrike(gameData, playingColor, strikeContext, depth + 1);
					if (nextSecondaryStrike != null) {
						if (logger.isDebugEnabled()) {
							logger.debug("[{}] Next secondary strike found: {}", depth, nextSecondaryStrike);
						}
						storeInSecondaryStrikeCache(gameData, strikeContext, playingColor, nextSecondaryStrike);
					}
					gameData.removeMove(opponentDefendFromStrike);
					if (nextSecondaryStrike == null) {
						if (logger.isDebugEnabled()) {
							logger.debug("[{}] Has defense: {}", depth, opponentDefendFromStrike);
						}
						hasDefense = true;
						break;
					}
				}
				gameData.removeMove(cell);
				if (!hasDefense) {
					if (logger.isDebugEnabled()) {
						logger.debug("[{}] No defense found for: {}", depth, cell);
					}
					storeInSecondaryStrikeCache(gameData, strikeContext, playingColor, cell);
					return cell;
				}
				if (logger.isDebugEnabled()) {
					logger.debug("[{}] Cancel attack: {}", depth, cell);
				}
			}
			throw new IllegalStateException("Secondary strike not found");
		}
	}
	
	private boolean hasInDirectStrikeCache(GameData gameData, int playingColor, StrikeContext strikeContext) {
		return cacheService.isCacheEnabled() && cacheService.getDirectStrikeCache(strikeContext.getGameId()).containsKey(playingColor) && cacheService.getDirectStrikeCache(strikeContext.getGameId()).get(playingColor).containsKey(gameData);
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
	
	private boolean hasInCounterStrikeCache(GameData gameData, StrikeContext strikeContext, int playingColor) {
		return cacheService.isCacheEnabled() && cacheService.getCounterStrikeCache(strikeContext.getGameId()).containsKey(playingColor) && cacheService.getCounterStrikeCache(strikeContext.getGameId()).get(playingColor).containsKey(gameData);
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
	
	private Cell defendThenSecondaryStrike(GameData gameData, int playingColor, StrikeContext strikeContext, int depth) throws InterruptedException {
		List<Cell> defendFromStrikes = internalDefendFromDirectStrike(gameData, playingColor, strikeContext, false);
		// defend
		for (Cell defendFromStrike : defendFromStrikes) {
			try  {
				gameData.addMove(defendFromStrike, playingColor);
				// check for a new strike
				Cell newStrike = internalDirectStrike(gameData, playingColor, strikeContext);
				if (newStrike != null) {
					List<Cell> opponentDefendFromStrikes = internalDefendFromDirectStrike(gameData, -playingColor, strikeContext, false);
					boolean hasDefense = false;
					for (Cell opponentDefendFromStrike : opponentDefendFromStrikes) {
						gameData.addMove(opponentDefendFromStrike, -playingColor);
						Cell newAttempt = internalSecondaryStrike(gameData, playingColor, strikeContext, depth + 1);
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

	private boolean hasInSecondaryCache(GameData gameData, int playingColor, StrikeContext strikeContext) {
		return cacheService.isCacheEnabled() && cacheService.getSecondaryStrikeCache(strikeContext.getGameId()).containsKey(playingColor) && cacheService.getSecondaryStrikeCache(strikeContext.getGameId()).get(playingColor).containsKey(gameData);
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
		} else {
			cacheService.getSecondaryStrikeCache(strikeContext.getGameId()).get(playingColor).put(new GameData(gameData), Optional.empty());
		}
	}

	private List<Cell> extractAnalyzedCells(GameData gameData, int playingColor) {
		ThreatContext threatContext = threatService.getOrUpdateThreatContext(gameData, playingColor);
		List<Cell> analyzedCells = new ArrayList<>();
		analyzedCells.addAll(threatContext.getThreatsOfType(ThreatType.THREAT_4).stream().map(t -> t.getTargetCell()).collect(Collectors.toSet()));
		for (ThreatType[] secondaryThreatTypePair : SECONDARY_THREAT_PAIRS) {
			Set<Cell> threats = threatService.findCombinedThreats(threatContext, secondaryThreatTypePair[0], secondaryThreatTypePair[1]);
			for (Cell threat : threats) {
				if (!analyzedCells.contains(threat)) {
					analyzedCells.add(threat);
				}
			}
		}
		Set<Cell> threats = threatContext.getThreatsOfType(ThreatType.DOUBLE_THREAT_3).stream().map(Threat::getTargetCell).collect(Collectors.toSet());
		for (Cell threat : threats) {
			if (!analyzedCells.contains(threat)) {
				analyzedCells.add(threat);
			}
		}
		return analyzedCells;
	}

}
