package fr.leblanc.gomoku.engine.service;

import java.util.List;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.GameData;
import fr.leblanc.gomoku.engine.model.StrikeContext;

public interface StrikeService {

	Cell directStrike(GameData gameData, int i, StrikeContext strikeContext) throws InterruptedException;

	List<Cell> defendFromDirectStrike(GameData gameData, int playingColor, StrikeContext strikeContext, boolean returnFirst) throws InterruptedException;

	Cell secondaryStrike(GameData gameData, int playingColor, StrikeContext strikeContext) throws InterruptedException;

	boolean hasPlayingStrike(GameData gameData, int playingColor, Long cacheId, boolean deepSearch) throws InterruptedException;
	
	boolean hasPendingStrike(GameData gameData, int pendingColor, Long cacheId, boolean deepSearch) throws InterruptedException;

}