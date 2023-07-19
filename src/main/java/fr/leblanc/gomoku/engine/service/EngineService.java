package fr.leblanc.gomoku.engine.service;

import fr.leblanc.gomoku.engine.model.messaging.GameDto;
import fr.leblanc.gomoku.engine.model.messaging.MoveDto;

public interface EngineService {

	MoveDto computeMove(GameDto game);

	void stopComputation();

}