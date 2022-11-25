package fr.leblanc.gomoku.engine.service;

import java.util.List;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.DataWrapper;
import fr.leblanc.gomoku.engine.model.EngineSettings;

public interface MinMaxService extends StoppableService {

	Cell computeMinMax(DataWrapper dataWrapper, int playingColor, List<Cell> analyzedMoves, EngineSettings engineSettings);

}