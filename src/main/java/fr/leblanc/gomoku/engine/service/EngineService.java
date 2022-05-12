package fr.leblanc.gomoku.engine.service;

import org.springframework.stereotype.Service;

import fr.leblanc.gomoku.engine.model.CheckWinResult;
import fr.leblanc.gomoku.engine.model.GameDto;
import fr.leblanc.gomoku.engine.model.MoveDto;

@Service
public class EngineService {

	public static final int NONE_COLOR = -1;
	public static final int BLACK_COLOR = 0;
	public static final int WHITE_COLOR = 1;
	
	private static final int[] DOWN_VECTOR = {0, 1};
	private static final int[] RIGHT_VECTOR = {1, 0};
	private static final int[] DOWN_RIGHT_VECTOR = {1, 1};
	private static final int[] DOWN_LEFT_VECTOR = {-1, 1};
	
	private static final int[][] VECTORS = { DOWN_VECTOR, RIGHT_VECTOR, DOWN_RIGHT_VECTOR, DOWN_LEFT_VECTOR };
	private static final int[] COLORS = { BLACK_COLOR, WHITE_COLOR };
	
	public CheckWinResult checkWin(GameDto game) {

		int[][] data = extractData(game);
		
		for (int color : COLORS) {
			int[][] result = new int[5][2];
			if (checkWin(data, color, result)) {
				return buildResult(result, color);
			}
		}
		
		return new CheckWinResult();
		
	}

	private int[][] extractData(GameDto game) {
		
		int boardSize = game.getBoardSize();
		
		int[][] data = new int[boardSize][boardSize];

		for (int rowIndex = 0; rowIndex < boardSize; rowIndex++) {
			for (int columnIndex = 0; columnIndex < boardSize; columnIndex++) {
				data[columnIndex][rowIndex] = EngineService.NONE_COLOR;
			}
		}
		
		for (MoveDto move : game.getMoves()) {
			data[move.getColumnIndex()][move.getRowIndex()] = move.getColor();
		}
		return data;
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
					
					for (int[] vector : VECTORS) {
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
		while (j + vector[0] * k < data.length 
				&& i + vector[1] * k < data[0].length 
				&& j + vector[0] * k > -1 
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

	public MoveDto computeMove(GameDto game) {
		
		int[][] data = extractData(game);

		return null;
	}

}
