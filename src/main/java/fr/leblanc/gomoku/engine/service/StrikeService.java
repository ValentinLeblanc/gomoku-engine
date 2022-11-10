package fr.leblanc.gomoku.engine.service;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.DataWrapper;

public interface StrikeService extends ComputationService {

	Cell findOrCounterStrike(DataWrapper dataWrapper, int playingColor) throws InterruptedException;

}