package fr.leblanc.gomoku.engine.service;

import fr.leblanc.gomoku.engine.model.CheckWinResult;
import fr.leblanc.gomoku.engine.model.GameData;

public interface CheckWinService {

	CheckWinResult checkWin(GameData gameData);

}