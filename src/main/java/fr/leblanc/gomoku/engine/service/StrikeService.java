package fr.leblanc.gomoku.engine.service;

import fr.leblanc.gomoku.engine.model.DataWrapper;
import fr.leblanc.gomoku.engine.model.StrikeResult;

public interface StrikeService extends StoppableService {

	StrikeResult processStrike(DataWrapper dataWrapper, int playingColor, int strikeDepth, int minMaxDepth, int strikeTimeout) throws InterruptedException;

}