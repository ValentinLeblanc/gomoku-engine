package fr.leblanc.gomoku.engine.service;

import fr.leblanc.gomoku.engine.model.GameData;
import fr.leblanc.gomoku.engine.model.StrikeContext;
import fr.leblanc.gomoku.engine.model.StrikeResult;

public interface StrikeService extends StoppableService {

	StrikeResult processStrike(GameData dataWrapper, int playingColor, StrikeContext strikeContext) throws InterruptedException;

}