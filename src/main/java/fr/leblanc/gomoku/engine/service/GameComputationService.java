package fr.leblanc.gomoku.engine.service;

import fr.leblanc.gomoku.engine.util.TypedAction;

public interface GameComputationService {

	boolean isGameComputing(Long gameId);

	void stopGameComputation(Long gameId);

	boolean isGameComputationStopped(Long gameId);

	<T> T startGameComputation(Long gameId, boolean displayAnalysis, TypedAction<T> action) throws InterruptedException;
	
	boolean isDisplayAnalysis(Long gameId);

}