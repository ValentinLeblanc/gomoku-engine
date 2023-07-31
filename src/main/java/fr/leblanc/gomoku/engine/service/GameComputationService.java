package fr.leblanc.gomoku.engine.service;

import fr.leblanc.gomoku.engine.util.TypedAction;

public interface GameComputationService {

	boolean isGameComputing(Long gameId);

	<T> T startGameComputation(Long gameId, TypedAction<T> action) throws InterruptedException;

	void stopGameComputation(Long gameId);

	boolean isGameComputationStopped(Long gameId);

}