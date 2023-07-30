package fr.leblanc.gomoku.engine.service;

import java.util.List;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.GameData;
import fr.leblanc.gomoku.engine.model.MinMaxResult;

public interface MinMaxService extends StoppableService {

	MinMaxResult computeMinMax(GameData gameData, int maxDepth, int extent) throws InterruptedException;
	
	MinMaxResult computeMinMax(GameData gameData, List<Cell> cells, int maxDepth, int extent) throws InterruptedException;

}