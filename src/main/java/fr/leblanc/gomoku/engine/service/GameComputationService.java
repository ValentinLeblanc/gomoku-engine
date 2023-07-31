package fr.leblanc.gomoku.engine.service;

import fr.leblanc.gomoku.engine.util.TypedAction;

public interface GameComputationService {

	Long getCurrentGameId();

	void setCurrentGameId(Long gameId);

	boolean isGameComputing(Long gameId);

	boolean isGameComputationStopped();

	<T> T startGameComputation(Long gameId, TypedAction<T> action) throws InterruptedException;

	void stopGameComputation(Long gameId);

}