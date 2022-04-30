package fr.leblanc.gomoku.engine.service;

import org.springframework.stereotype.Service;

import fr.leblanc.gomoku.engine.model.CheckWinResultDto;
import fr.leblanc.gomoku.engine.model.GameDto;
import fr.leblanc.gomoku.engine.model.MoveDto;

@Service
public class EngineService {

	private static final int NONE_COLOR = -1;
	private static final int BLACK_COLOR = 0;
	private static final int WHITE_COLOR = 1;
	
	public CheckWinResultDto checkWin(GameDto game) {

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
		
		int[][] blackWin = checkWin(data, BLACK_COLOR);
		
		if (blackWin != null) {
			return buildResult(blackWin);
		}
		
		int[][] whiteWin = checkWin(data, WHITE_COLOR);
		
		if (whiteWin != null) {
			return buildResult(whiteWin);
		}
		
		return new CheckWinResultDto();
		
	}

	private CheckWinResultDto buildResult(int[][] blackWin) {
		
		CheckWinResultDto result = new CheckWinResultDto();
		
		result.setWin(true);
		
		for (int i = 0; i < blackWin.length; i++) {
			MoveDto move = new MoveDto();
			
			move.setColumnIndex(blackWin[i][0]);
			move.setRowIndex(blackWin[i][1]);
			
			result.getWinMoves().add(move);
		}
		
		return result;
	}
	
	private int[][] checkWin(int[][] data, int color) {
		
		for (int i = 0; i < data[0].length; i++) {
			for (int j = 0; j < data.length; j++) {
				if (data[j][i] == color) {
					
					int[][] result = checkForWin(data, i, j, color, 0, 1);
					
					if (result != null) {
						return result;
					}
					
					result = checkForWin(data, i, j, color, 1, 0);
					
					if (result != null) {
						return result;
					}
					
					result = checkForWin(data, i, j, color, 1, 1);
					
					if (result != null) {
						return result;
					}
					
					result = checkForWin(data, i, j, color, -1, 1);
					
					if (result != null) {
						return result;
					}
					
				}
			}
		}
		
		return null;
	}

	private int[][] checkForWin(int[][] data, int i, int j, int color, int vector0, int vector1) {
		int[][] result = new int[5][2];

		int k = 1;
		int linedUpCount = 1;
		result[0][0] = j;
		result[0][1] = i;
		while (j + vector0 * k < data.length && j + vector0 * k > -1 && i + vector1 * k < data[0].length && data[j + vector0 * k][i + vector1 * k] == color) {
			linedUpCount++;
			result[k][0] = j + vector0 * k;
			result[k][1] = i + vector1 * k;
			if (linedUpCount == 5) {
				return result;
			}
			k++;
		}

		return null;
	}

}
