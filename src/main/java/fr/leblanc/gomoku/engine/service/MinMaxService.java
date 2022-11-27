package fr.leblanc.gomoku.engine.service;

import java.util.List;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.DataWrapper;
import fr.leblanc.gomoku.engine.model.messaging.EngineSettingsDto;

public interface MinMaxService extends StoppableService {

	Cell computeMinMax(DataWrapper dataWrapper, int playingColor, List<Cell> analyzedMoves, EngineSettingsDto engineSettings) throws InterruptedException;

}