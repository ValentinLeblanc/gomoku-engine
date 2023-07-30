package fr.leblanc.gomoku.engine.service;

import fr.leblanc.gomoku.engine.util.TypedAction;

public interface ComputationService {

	Long getCurrentThreadComputationId();

	void setCurrentThreadComputationId(Long computationId);

	boolean isComputing(Long computationId);

	boolean isComputationStopped();

	<T> T startComputation(Long computationId, TypedAction<T> action) throws InterruptedException;

	void stopComputation(Long computationId);

}