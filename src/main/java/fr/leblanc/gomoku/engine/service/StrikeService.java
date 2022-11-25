package fr.leblanc.gomoku.engine.service;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.DataWrapper;
import fr.leblanc.gomoku.engine.model.EngineSettings;

public interface StrikeService extends StoppableService {

	Cell findOrCounterStrike(DataWrapper dataWrapper, int playingColor, EngineSettings engineSettings) throws InterruptedException;

}