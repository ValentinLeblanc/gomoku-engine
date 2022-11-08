package fr.leblanc.gomoku.engine.service;

import fr.leblanc.gomoku.engine.model.CheckWinResult;
import fr.leblanc.gomoku.engine.model.GameDto;
import fr.leblanc.gomoku.engine.model.MoveDto;

public interface EngineService {

	CheckWinResult checkWin(GameDto game);

	MoveDto computeMove(GameDto game);

	Double computeEvaluation(GameDto game);

}