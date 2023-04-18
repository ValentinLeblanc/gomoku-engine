package fr.leblanc.gomoku.engine.service;

import java.util.List;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.DataWrapper;
import fr.leblanc.gomoku.engine.model.MinMaxResult;

public interface MinMaxService extends StoppableService {

	MinMaxResult computeMinMax(DataWrapper dataWrapper, List<Cell> cells, int maxDepth, int extent) throws InterruptedException;

}