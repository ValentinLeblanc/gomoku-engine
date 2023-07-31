package fr.leblanc.gomoku.engine.service;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.GameData;
import fr.leblanc.gomoku.engine.model.messaging.GameSettings;

public interface EngineService {

	Cell computeMove(Long gameId, GameData gameData, GameSettings gameSettings) throws InterruptedException;

}