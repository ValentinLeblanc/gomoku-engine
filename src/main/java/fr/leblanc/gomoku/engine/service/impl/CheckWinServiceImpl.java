package fr.leblanc.gomoku.engine.service.impl;

import org.springframework.stereotype.Service;

import fr.leblanc.gomoku.engine.model.CheckWinResult;
import fr.leblanc.gomoku.engine.model.DataWrapper;
import fr.leblanc.gomoku.engine.model.EngineConstants;
import fr.leblanc.gomoku.engine.model.messaging.MoveDto;
import fr.leblanc.gomoku.engine.service.CheckWinService;

@Service
public class CheckWinServiceImpl implements CheckWinService {

	@Override
	public CheckWinResult checkWin(DataWrapper dataWrapper) {
		for (int color : EngineConstants.COLORS) {
			int[][] result = new int[5][2];
			if (checkWin(dataWrapper, color, result)) {
				return buildResult(result, color);
			}
		}
		return null;
	}
	
	private CheckWinResult buildResult(int[][] win, int color) {
		CheckWinResult result = new CheckWinResult(color);
		for (int i = 0; i < win.length; i++) {
			result.getWinMoves().add(new MoveDto(win[i][0], win[i][1], color));
		}
		return result;
	}
	
	private boolean checkWin(DataWrapper dataWrapper, int color, int[][] result) {

		int[][] data = dataWrapper.getData();
		
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
}
