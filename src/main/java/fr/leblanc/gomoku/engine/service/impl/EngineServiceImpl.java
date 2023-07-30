package fr.leblanc.gomoku.engine.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.leblanc.gomoku.engine.exception.EngineException;
import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.GameData;
import fr.leblanc.gomoku.engine.model.MinMaxResult;
import fr.leblanc.gomoku.engine.model.StrikeContext;
import fr.leblanc.gomoku.engine.model.StrikeResult;
import fr.leblanc.gomoku.engine.model.messaging.GameSettings;
import fr.leblanc.gomoku.engine.service.CheckWinService;
import fr.leblanc.gomoku.engine.service.EngineService;
import fr.leblanc.gomoku.engine.service.MinMaxService;
import fr.leblanc.gomoku.engine.service.StrikeService;
import fr.leblanc.gomoku.engine.util.cache.L2CacheSupport;

@Service
public class EngineServiceImpl implements EngineService {

	private static final Logger logger = LoggerFactory.getLogger(EngineServiceImpl.class);
	
	@Autowired
	private StrikeService strikeService;
	
	@Autowired
	private MinMaxService minMaxService;
	
	@Autowired
	private CheckWinService checkWinService;
	
	@Override
	public Boolean isComputing(Long gameId) {
		return strikeService.isComputing(gameId) || minMaxService.isComputing(gameId);
	}
	
	@Override
	public Cell computeMove(GameData gameData, GameSettings gameSettings) {

		try {
			if (checkWinService.checkWin(gameData).isWin()) {
				return Cell.NONE_CELL;
			}
			
			int playingColor = GameData.extractPlayingColor(gameData);
			
			if (GameData.countPlainCells(gameData) == 0) {
				return middleCell(gameData);
			}
			
			return L2CacheSupport.doInCacheContext(() -> {

				// STRIKE
				if (gameSettings.isStrikeEnabled()) {
					StrikeContext strikeContext = new StrikeContext();
					strikeContext.setStrikeDepth(gameSettings.getStrikeDepth());
					strikeContext.setMinMaxDepth(gameSettings.getMinMaxDepth());
					strikeContext.setStrikeTimeout(gameSettings.getStrikeTimeout());
					StrikeResult strikeOrCounterStrike = strikeService.processStrike(gameData, playingColor, strikeContext);
					if (strikeOrCounterStrike.hasResult()) {
						return strikeOrCounterStrike.getResultCell();
					}
				}

				// MINMAX
				MinMaxResult minMaxResult = minMaxService.computeMinMax(gameData, null, gameSettings.getMinMaxDepth(), gameSettings.getMinMaxExtent());
				
				if (minMaxResult == MinMaxResult.EMPTY_RESULT) {
					return Cell.NONE_CELL;
				}
				
				return minMaxResult.getResultCell();
			});

		} catch (InterruptedException e) {
			logger.info("Interrupted engine service");
			Thread.currentThread().interrupt();
			throw new EngineException(e);
		}
	}

	private Cell middleCell(GameData gameData) {
		return new Cell(gameData.getData().length / 2, gameData.getData().length / 2);
	}

	@Override
	public void stopComputation(Long gameId) {
		if (strikeService.isComputing(gameId)) {
			strikeService.stopComputation(gameId);
		} else if (minMaxService.isComputing(gameId)) {
			minMaxService.stopComputation(gameId);
		}
	}

}
