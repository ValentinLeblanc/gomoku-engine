package fr.leblanc.gomoku.engine.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.GameData;
import fr.leblanc.gomoku.engine.model.GameSettings;
import fr.leblanc.gomoku.engine.model.MinMaxContext;
import fr.leblanc.gomoku.engine.model.MinMaxResult;
import fr.leblanc.gomoku.engine.model.StrikeContext;
import fr.leblanc.gomoku.engine.model.messaging.EngineMessageType;
import fr.leblanc.gomoku.engine.model.messaging.MoveDTO;
import fr.leblanc.gomoku.engine.service.CacheService;
import fr.leblanc.gomoku.engine.service.CheckWinService;
import fr.leblanc.gomoku.engine.service.EngineService;
import fr.leblanc.gomoku.engine.service.MinMaxService;
import fr.leblanc.gomoku.engine.service.StrikeService;
import fr.leblanc.gomoku.engine.service.WebSocketService;

@Service
public class EngineServiceImpl implements EngineService {

	private static final Logger logger = LoggerFactory.getLogger(EngineServiceImpl.class);

	private CheckWinService checkWinService;

	private StrikeService strikeService;
	
	private MinMaxService minMaxService;
	
	private WebSocketService webSocketService;
	
	private CacheService cacheService;
	
	public EngineServiceImpl(CheckWinService checkWinService, StrikeService strikeService, MinMaxService minMaxService,
			WebSocketService webSocketService, CacheService cacheService) {
		super();
		this.checkWinService = checkWinService;
		this.strikeService = strikeService;
		this.minMaxService = minMaxService;
		this.webSocketService = webSocketService;
		this.cacheService = cacheService;
	}
	
	@Override
	public Cell computeMove(Long gameId, GameData gameData, GameSettings gameSettings) throws InterruptedException {

		if (checkWinService.checkWin(gameData).isWin()) {
			throw new IllegalStateException("Game is already over");
		}
		
		if (GameData.countPlainCells(gameData) == 0) {
			return middleCell(gameData);
		}
		
		if (gameSettings.strikeEnabled()) {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			Cell strikeOrCounterStrike = processStrike(gameId, gameData, gameSettings);
			if (strikeOrCounterStrike != null) {
				stopWatch.stop();
				if (logger.isInfoEnabled()) {
					logger.info("strikeService processed in {} ms", stopWatch.lastTaskInfo().getTimeMillis());
				}
				return strikeOrCounterStrike;
			}
		}
		
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		MinMaxContext minMaxContext = new MinMaxContext(gameId, gameSettings.minMaxDepth(), gameSettings.minMaxExtent());
		MinMaxResult minMaxResult = minMaxService.computeMinMax(gameData, minMaxContext);
		
		stopWatch.stop();
		if (logger.isInfoEnabled()) {
			logger.info("minMaxService processed in {} ms", stopWatch.lastTaskInfo().getTimeMillis());
		}
		
		if (gameSettings.displayAnalysis()) {
			int playingColor = GameData.extractPlayingColor(gameData);
			displayMinMaxResult(gameId, playingColor, minMaxResult);
		}
		
		return minMaxResult.getResultCell();
			
	}

	private void displayMinMaxResult(Long gameId, int playingColor, MinMaxResult minMaxResult) {
		for (Entry<Integer, Cell> entry : minMaxResult.getOptimalMoves().entrySet()) {
			Integer depth = entry.getKey();
			if (depth > 0) {
				playingColor = -playingColor;
				Cell optimalMove = entry.getValue();
				webSocketService.sendMessage(EngineMessageType.MINMAX_RESULT, gameId, new MoveDTO(optimalMove, playingColor));
			}
		}
	}
	
	private Cell processStrike(Long gameId, GameData gameData, GameSettings gameSettings) throws InterruptedException {
		
		int playingColor = GameData.extractPlayingColor(gameData);
		
		StrikeContext strikeContext = new StrikeContext(gameId, gameSettings.strikeDepth(), gameSettings.strikeTimeout());
		
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
					Cell defense = pickBestDefense(gameData, playingColor, strikeContext, gameSettings.minMaxDepth(), defendingCells);
					stopWatch.stop();
					if (logger.isInfoEnabled()) {
						logger.info("best defense found in {} ms", stopWatch.getTotalTimeMillis());
					}
					return defense;
				}
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
	
	private Cell pickBestDefense(GameData gameData, int playingColor, StrikeContext strikeContext, int minMaxDepth, List<Cell> counterOpponentThreats) throws InterruptedException {
		
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
		
		MinMaxContext minMaxContext = new MinMaxContext(strikeContext.getGameId(), minMaxDepth, 0, false);
		
		return minMaxService.computeMinMax(gameData, counterOpponentThreats, minMaxContext).getOptimalMoves().get(0);
	}

	private Cell middleCell(GameData gameData) {
		return new Cell(gameData.getData().length / 2, gameData.getData().length / 2);
	}
	
}
