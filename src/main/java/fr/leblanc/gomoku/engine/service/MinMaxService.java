package fr.leblanc.gomoku.engine.service;

import java.util.List;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.GameData;
import fr.leblanc.gomoku.engine.model.MinMaxContext;
import fr.leblanc.gomoku.engine.model.MinMaxResult;

public interface MinMaxService {

	MinMaxResult computeMinMax(GameData gameData, MinMaxContext context) throws InterruptedException;
	
	MinMaxResult computeMinMax(GameData gameData, List<Cell> analyzedCells, MinMaxContext context) throws InterruptedException;

}