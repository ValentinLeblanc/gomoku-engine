package fr.leblanc.gomoku.engine.service;

import java.util.List;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.DataWrapper;

public interface MinMaxService extends ComputationService {

	Cell computeMinMax(DataWrapper dataWrapper, int playingColor, List<Cell> analyzedMoves);

}