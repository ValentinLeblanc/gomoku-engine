package fr.leblanc.gomoku.engine.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.CheckWinResult;
import fr.leblanc.gomoku.engine.model.DataWrapper;
import fr.leblanc.gomoku.engine.model.EngineConstants;
import fr.leblanc.gomoku.engine.model.GameDto;
import fr.leblanc.gomoku.engine.model.MoveDto;

@Service
public class EngineService {

	@Autowired
	private EvaluationService evaluationService;
	
	@Autowired
	private StrikeService strikeService;
	
	@Autowired
	private MinMaxService minMaxService;

	public CheckWinResult checkWin(GameDto game) {

		DataWrapper dataWrapper = extractData(game);

		for (int color : EngineConstants.COLORS) {
			int[][] result = new int[5][2];
			if (checkWin(dataWrapper.getData(), color, result)) {
				return buildResult(result, color);
			}
		}

		return new CheckWinResult();

	}

	public MoveDto computeMove(GameDto game) {

		int playingColor = extractPlayingColor(game);
		
		if (game.getMoves().isEmpty()) {
			return new MoveDto(game.getBoardSize() / 2, game.getBoardSize() / 2, playingColor);
		}
		
		DataWrapper dataWrapper = extractData(game);

//		Cell strikeOrCounterStrike = strikeService.findOrCounterStrike(dataWrapper, playingColor);
//
//		if (strikeOrCounterStrike != null) {
//			return new MoveDto(strikeOrCounterStrike.getColumnIndex(), strikeOrCounterStrike.getRowIndex(), playingColor);
//		}
		
		Cell minMaxMove = minMaxService.computeMinMax(dataWrapper, playingColor, null);
		
		return new MoveDto(minMaxMove.getColumnIndex(), minMaxMove.getRowIndex(), playingColor);
	}


	public Double computeEvaluation(GameDto game) {
		
		int playingColor = extractPlayingColor(game);
		
		if (playingColor == EngineConstants.BLACK_COLOR) {
			return evaluationService.computeEvaluation(extractData(game), extractPlayingColor(game));
		} else if (playingColor == EngineConstants.WHITE_COLOR) {
			return -evaluationService.computeEvaluation(extractData(game), extractPlayingColor(game));
		}
		
		throw new IllegalArgumentException("Game has no valid playing color");
		
	}

	private DataWrapper extractData(GameDto game) {

		int boardSize = game.getBoardSize();

		int[][] data = new int[boardSize][boardSize];

		for (int rowIndex = 0; rowIndex < boardSize; rowIndex++) {
			for (int columnIndex = 0; columnIndex < boardSize; columnIndex++) {
				data[columnIndex][rowIndex] = EngineConstants.NONE_COLOR;
			}
		}

		for (MoveDto move : game.getMoves()) {
			data[move.getColumnIndex()][move.getRowIndex()] = move.getColor();
		}

		return new DataWrapper(data);
	}

	private CheckWinResult buildResult(int[][] win, int color) {

		CheckWinResult result = new CheckWinResult(true);

		for (int i = 0; i < win.length; i++) {
			result.getWinMoves().add(new MoveDto(win[i][0], win[i][1], color));
		}

		return result;
	}

	private boolean checkWin(int[][] data, int color, int[][] result) {

		for (int i = 0; i < data[0].length; i++) {
			for (int j = 0; j < data.length; j++) {
				if (data[j][i] == color) {

					for (int[] vector : EngineConstants.VECTORS) {
						if (checkWin(data, i, j, color, vector, result)) {
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	private boolean checkWin(int[][] data, int i, int j, int color, int[] vector, int[][] result) {

		int k = 1;
		int linedUpCount = 1;
		result[0][0] = j;
		result[0][1] = i;
		while (j + vector[0] * k < data.length && i + vector[1] * k < data[0].length && j + vector[0] * k > -1
				&& data[j + vector[0] * k][i + vector[1] * k] == color) {
			linedUpCount++;
			result[k][0] = j + vector[0] * k;
			result[k][1] = i + vector[1] * k;
			if (linedUpCount == 5) {
				return true;
			}
			k++;
		}

		return false;
	}

	private int extractPlayingColor(GameDto game) {
		return game.getMoves().size() % 2 == 0 ? EngineConstants.BLACK_COLOR : EngineConstants.WHITE_COLOR;
	}

}
