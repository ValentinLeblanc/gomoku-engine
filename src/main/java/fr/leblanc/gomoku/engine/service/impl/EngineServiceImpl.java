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
	public Cell computeMove(GameData gameData, GameSettings gameSettings) throws InterruptedException {

		if (checkWinService.checkWin(gameData).isWin()) {
			throw new IllegalStateException("Game is already over");
		}
		
		int playingColor = GameData.extractPlayingColor(gameData);
		
		if (GameData.countPlainCells(gameData) == 0) {
			return middleCell(gameData);
		}
		
		Cell strikeResult = computeStrike(new GameData(gameData), gameSettings, playingColor);
		if (strikeResult != null) {
			return strikeResult;
		}
		
		return computeMinMax(gameData, gameSettings);
			
	}

	private Cell computeMinMax(GameData gameData, GameSettings gameSettings) throws InterruptedException {
		MinMaxResult minMaxResult = minMaxService.computeMinMax(gameData, gameSettings.getMinMaxDepth(), gameSettings.getMinMaxExtent());
		return minMaxResult.getResultCell();
	}

	private Cell computeStrike(GameData gameData, GameSettings gameSettings, int playingColor) throws InterruptedException {
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
		return null;
	}

	private Cell middleCell(GameData gameData) {
		return new Cell(gameData.getData().length / 2, gameData.getData().length / 2);
	}

}
