package fr.leblanc.gomoku.engine.service;

import java.util.List;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.GameData;
import fr.leblanc.gomoku.engine.model.MinMaxResult;

public interface MinMaxService {

	MinMaxResult computeMinMax(Long gameId, GameData gameData, int maxDepth, int extent) throws InterruptedException;

	MinMaxResult computeMinMax(Long gameId, GameData gameData, List<Cell> analyzedCells, int maxDepth, int extent) throws InterruptedException;

}