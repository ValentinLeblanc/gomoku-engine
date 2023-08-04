package fr.leblanc.gomoku.engine.service.impl;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.GomokuColor;
import fr.leblanc.gomoku.engine.model.GameData;
import fr.leblanc.gomoku.engine.model.MinMaxContext;
import fr.leblanc.gomoku.engine.model.MinMaxResult;
import fr.leblanc.gomoku.engine.model.StrikeContext;
import fr.leblanc.gomoku.engine.model.messaging.EngineMessageType;
import fr.leblanc.gomoku.engine.model.messaging.GameSettings;
import fr.leblanc.gomoku.engine.service.CacheService;
import fr.leblanc.gomoku.engine.service.CheckWinService;
import fr.leblanc.gomoku.engine.service.EngineService;
import fr.leblanc.gomoku.engine.service.MinMaxService;
import fr.leblanc.gomoku.engine.service.StrikeService;
import fr.leblanc.gomoku.engine.service.WebSocketService;

@Service
public class EngineServiceImpl implements EngineService {

	private static final Logger logger = LoggerFactory.getLogger(EngineServiceImpl.class);

	@Autowired
	private CheckWinService checkWinService;

	@Autowired
	private StrikeService strikeService;
	
	@Autowired
	private MinMaxService minMaxService;
	
	@Autowired
	private WebSocketService webSocketService;
	
	@Autowired
	private CacheService cacheService;
	
	@Override
	public Cell computeMove(Long gameId, GameData gameData, GameSettings gameSettings) throws InterruptedException {

		if (checkWinService.checkWin(gameData).isWin()) {
			throw new IllegalStateException("Game is already over");
		}
		
		int playingColor = GameData.extractPlayingColor(gameData);
		
		if (GameData.countPlainCells(gameData) == 0) {
			return middleCell(gameData);
		}
		
		if (gameSettings.isStrikeEnabled()) {
			Cell strikeOrCounterStrike = processStrike(gameId, new GameData(gameData), playingColor, gameSettings.getStrikeDepth(), gameSettings.getMinMaxDepth(), gameSettings.getStrikeTimeout());
			if (strikeOrCounterStrike != null) {
				return strikeOrCounterStrike;
			}
		}
		
		MinMaxResult minMaxResult = minMaxService.computeMinMax(gameData, new MinMaxContext(gameId, gameSettings.getMinMaxDepth(), gameSettings.getMinMaxExtent()));
		return minMaxResult.getResultCell();
			
	}
	
	private Cell processStrike(Long gameId, GameData gameData, int playingColor, int strikeDepth, int minMaxDepth, int strikeTimeout) throws InterruptedException {
		
		StrikeContext strikeContext = new StrikeContext(gameId, strikeDepth, minMaxDepth, strikeTimeout);
		
		webSocketService.sendMessage(EngineMessageType.STRIKE_PROGRESS, strikeContext.getGameId(), true);
		
		try {
			StopWatch stopWatch = new StopWatch("processStrike");
			stopWatch.start();
			
			if (logger.isInfoEnabled()) {
				logger.info("find direct strike...");
			}
			
			Cell directStrike = strikeService.directStrike(gameData, playingColor, strikeContext);
			
			if (directStrike != null) {
				stopWatch.stop();
				if (logger.isDebugEnabled()) {
					logger.info("direct strike found in {} ms", stopWatch.getTotalTimeMillis());
				}
				return directStrike;
			}
			
			Cell opponentDirectStrike = strikeService.directStrike(gameData, -playingColor, strikeContext);
			
			if (opponentDirectStrike != null) {
				if (logger.isInfoEnabled()) {
					logger.info("defend from opponent direct strike...");
				}
				List<Cell> defendingCells = strikeService.defendFromDirectStrike(gameData, playingColor, strikeContext, false);
				
				if (!defendingCells.isEmpty()) {
					Cell defense = pickBestDefense(gameData, playingColor, strikeContext, defendingCells);
					stopWatch.stop();
					if (logger.isInfoEnabled()) {
						logger.info("best defense found in {} ms", stopWatch.getTotalTimeMillis());
					}
					return defense;
				}
				
				return randomCell(gameData);
			}
			
			if (logger.isInfoEnabled()) {
				logger.info("find secondary strike...");
			}
			
			Cell secondaryStrike = strikeService.secondaryStrike(gameData, playingColor, strikeContext);
			
			if (secondaryStrike != null) {
				return secondaryStrike;
			}
			
			return null;
		} finally {
			webSocketService.sendMessage(EngineMessageType.STRIKE_PROGRESS, strikeContext.getGameId(), false);
		}
	}
	
	private Cell pickBestDefense(GameData gameData, int playingColor, StrikeContext strikeContext, List<Cell> counterOpponentThreats) throws InterruptedException {
		
		if (counterOpponentThreats.size() == 1) {
			return counterOpponentThreats.get(0);
		}
		
		Map<GameData, Optional<Cell>> secondaryStrikeCache = cacheService.getSecondaryStrikeCache(strikeContext.getGameId()).get(playingColor);
		if (secondaryStrikeCache.containsKey(gameData)) {
			Optional<Cell> optional = secondaryStrikeCache.get(gameData);
			if (optional.isPresent() && counterOpponentThreats.contains(optional.get())) {
				return optional.get();
			}
		}
		
		MinMaxContext minMaxContext = new MinMaxContext(strikeContext.getGameId(), strikeContext.getMinMaxDepth(), 0, false);
		
		return minMaxService.computeMinMax(gameData, counterOpponentThreats, minMaxContext).getOptimalMoves().get(0);
	}

	private Cell middleCell(GameData gameData) {
		return new Cell(gameData.getData().length / 2, gameData.getData().length / 2);
	}
	
	private Cell randomCell(GameData gameData) {
		try {
			Random random = SecureRandom.getInstanceStrong();
			int i = 0;
			int maxCellCheck = gameData.getData().length * gameData.getData().length;
			while (i < maxCellCheck) {
				i++;
				int randomX = random.nextInt(gameData.getData().length);
				int randomY = random.nextInt(gameData.getData().length);
				if (gameData.getValue(randomX, randomY) == GomokuColor.NONE_COLOR) {
					return new Cell(randomX, randomY);
				}
			}
			throw new IllegalStateException("No empty move could be found");
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("error while generating random Cell", e);
		}
	}

}
