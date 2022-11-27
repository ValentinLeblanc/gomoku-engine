package fr.leblanc.gomoku.engine.service;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.DataWrapper;
import fr.leblanc.gomoku.engine.model.messaging.EngineSettingsDto;

public interface StrikeService extends StoppableService {

	Cell findOrCounterStrike(DataWrapper dataWrapper, int playingColor, EngineSettingsDto engineSettings) throws InterruptedException;

}