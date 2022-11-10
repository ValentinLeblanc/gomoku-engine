package fr.leblanc.gomoku.engine.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.CheckWinResult;
import fr.leblanc.gomoku.engine.model.DataWrapper;
import fr.leblanc.gomoku.engine.model.EngineConstants;
import fr.leblanc.gomoku.engine.model.GameDto;
import fr.leblanc.gomoku.engine.model.MoveDto;
import fr.leblanc.gomoku.engine.service.CheckWinService;
import fr.leblanc.gomoku.engine.service.EngineService;
import fr.leblanc.gomoku.engine.service.EvaluationService;
import fr.leblanc.gomoku.engine.service.MinMaxService;
import fr.leblanc.gomoku.engine.service.StrikeService;

@Service
public class EngineServiceImpl implements EngineService {

	@Autowired
	private EvaluationService evaluationService;
	
	@Autowired
	private StrikeService strikeService;
	
	@Autowired
	private MinMaxService minMaxService;
	
	@Autowired
	private CheckWinService checkWinService;

	@Override
	public CheckWinResult checkWin(GameDto game) {

		DataWrapper dataWrapper = DataWrapper.of(game);

		for (int color : EngineConstants.COLORS) {
			int[][] result = new int[5][2];
			if (checkWinService.checkWin(dataWrapper, color, result)) {
				return buildResult(result, color);
			}
		}

		return new CheckWinResult();

	}

	@Override
	public MoveDto computeMove(GameDto game) throws InterruptedException {

		int playingColor = game.getMoves().size() % 2 == 0 ? EngineConstants.BLACK_COLOR : EngineConstants.WHITE_COLOR;
		
		if (game.getMoves().isEmpty()) {
			return new MoveDto(game.getBoardSize() / 2, game.getBoardSize() / 2, playingColor);
		}
		
		DataWrapper dataWrapper = DataWrapper.of(game);

		Cell strikeOrCounterStrike = strikeService.findOrCounterStrike(dataWrapper, playingColor);

		if (strikeOrCounterStrike != null) {
			return new MoveDto(strikeOrCounterStrike.getColumnIndex(), strikeOrCounterStrike.getRowIndex(), playingColor);
		}
		
		Cell minMaxMove = minMaxService.computeMinMax(dataWrapper, playingColor, null);
		
		return new MoveDto(minMaxMove.getColumnIndex(), minMaxMove.getRowIndex(), playingColor);
	}


	@Override
	public Double computeEvaluation(GameDto game) {
		
		int playingColor = extractPlayingColor(game);
		
		DataWrapper dataWrapper = DataWrapper.of(game);
		
		if (playingColor == EngineConstants.BLACK_COLOR) {
			return evaluationService.computeEvaluation(dataWrapper, playingColor);
		} else if (playingColor == EngineConstants.WHITE_COLOR) {
			return -evaluationService.computeEvaluation(dataWrapper, playingColor);
		}
		
		throw new IllegalArgumentException("Game has no valid playing color");
		
	}

	@Override
	public void stopComputation() {
		strikeService.stopComputation();
		minMaxService.stopComputation();
	}

	private CheckWinResult buildResult(int[][] win, int color) {

		CheckWinResult result = new CheckWinResult(true);

		for (int i = 0; i < win.length; i++) {
			result.getWinMoves().add(new MoveDto(win[i][0], win[i][1], color));
		}

		return result;
	}

	private int extractPlayingColor(GameDto game) {
		return game.getMoves().size() % 2 == 0 ? EngineConstants.BLACK_COLOR : EngineConstants.WHITE_COLOR;
	}

}
