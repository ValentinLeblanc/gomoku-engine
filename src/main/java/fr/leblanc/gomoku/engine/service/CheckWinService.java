package fr.leblanc.gomoku.engine.service;

import fr.leblanc.gomoku.engine.model.DataWrapper;

public interface CheckWinService {

	boolean checkWin(DataWrapper dataWrapper, int color, int[][] result);

}