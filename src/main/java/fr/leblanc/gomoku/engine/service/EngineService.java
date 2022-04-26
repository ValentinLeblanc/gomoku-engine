package fr.leblanc.gomoku.engine.service;

import org.springframework.stereotype.Service;import fr.leblanc.gomoku.engine.model.GomokuColor;

@Service
public class EngineService {

	public int[][] checkForWin(int[][] data) {

		int[][] blackWin = checkForWin(data, GomokuColor.BLACK.toNumber());
		
		if (blackWin != null) {
			return blackWin;
		}
		
		int[][] whiteWin = checkForWin(data, GomokuColor.WHITE.toNumber());
		
		if (whiteWin != null) {
			return whiteWin;
		}
		
		return null;
		
	}
	
	private int[][] checkForWin(int[][] data, int color) {
		
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
