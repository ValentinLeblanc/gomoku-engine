package fr.leblanc.gomoku.engine.service;

import fr.leblanc.gomoku.engine.model.CheckWinResult;
import fr.leblanc.gomoku.engine.model.DataWrapper;

public interface CheckWinService {

	CheckWinResult checkWin(DataWrapper dataWrapper);

}