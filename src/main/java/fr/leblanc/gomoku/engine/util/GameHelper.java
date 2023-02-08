package fr.leblanc.gomoku.engine.util;

import fr.leblanc.gomoku.engine.model.DataWrapper;
import fr.leblanc.gomoku.engine.model.EngineConstants;
import fr.leblanc.gomoku.engine.model.messaging.GameDto;

public class GameHelper {

	private GameHelper() {
		
	}
	
	public static int extractPlayingColor(DataWrapper dataWrapper) {
		
		int[][] data = dataWrapper.getData();
		
		int moveCount = 0;
		
		for (int i = 0; i < data[0].length; i++) {
			for (int j = 0; j < data.length; j++) {
				if (data[j][i] != EngineConstants.NONE_COLOR) {
					moveCount++;
				}
			}
		}
		
		return moveCount % 2 == 0 ? EngineConstants.BLACK_COLOR : EngineConstants.WHITE_COLOR;
	}
	
	public static int extractPlayingColor(GameDto game) {
		return game.getMoves().size() % 2 == 0 ? EngineConstants.BLACK_COLOR : EngineConstants.WHITE_COLOR;
	}
}
