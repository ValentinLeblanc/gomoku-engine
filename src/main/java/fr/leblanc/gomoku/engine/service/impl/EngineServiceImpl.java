package fr.leblanc.gomoku.engine.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

@Service
public class EngineServiceImpl implements EngineService {

	@Autowired
	private CheckWinService checkWinService;

	@Autowired
	private StrikeService strikeService;
	
	@Autowired
	private MinMaxService minMaxService;
	
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
			StrikeContext strikeContext = new StrikeContext();
			strikeContext.setGameId(gameId);
			strikeContext.setStrikeDepth(gameSettings.getStrikeDepth());
			strikeContext.setMinMaxDepth(gameSettings.getMinMaxDepth());
			strikeContext.setStrikeTimeout(gameSettings.getStrikeTimeout());
			StrikeResult strikeOrCounterStrike = strikeService.processStrike(gameData, playingColor, strikeContext);
			if (strikeOrCounterStrike.hasResult()) {
				return strikeOrCounterStrike.getResultCell();
			}
		}
		
		MinMaxResult minMaxResult = minMaxService.computeMinMax(gameId, gameData, gameSettings.getMinMaxDepth(), gameSettings.getMinMaxExtent());
		return minMaxResult.getResultCell();
			
	}

	private Cell middleCell(GameData gameData) {
		return new Cell(gameData.getData().length / 2, gameData.getData().length / 2);
	}

}
