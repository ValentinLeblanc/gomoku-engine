package fr.leblanc.gomoku.engine.service.impl;

import org.springframework.stereotype.Service;

import fr.leblanc.gomoku.engine.model.CheckWinResult;
import fr.leblanc.gomoku.engine.model.GameData;
import fr.leblanc.gomoku.engine.model.GomokuColor;
import fr.leblanc.gomoku.engine.service.CheckWinService;

@Service
public class CheckWinServiceImpl implements CheckWinService {
	
	private static final int[][] VECTORS = { {0, 1}, {1, 0}, {1, 1}, {-1, 1} };
	
	private static final int[] COLORS = { GomokuColor.BLACK_COLOR, GomokuColor.WHITE_COLOR };


	@Override
	public CheckWinResult checkWin(GameData gameData) {
		CheckWinResult checkWinResult = new CheckWinResult(GomokuColor.NONE_COLOR);
		for (int color : COLORS) {
			int[][] result = new int[5][2];
			if (checkWin(gameData, color, result)) {
				return checkWinResult.build(result, color);
			}
		}
		return checkWinResult;
	}
	
	private boolean checkWin(GameData gameData, int color, int[][] result) {

		int[][] data = gameData.getData();
		
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
}
