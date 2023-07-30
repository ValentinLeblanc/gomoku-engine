package fr.leblanc.gomoku.engine.service;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.util.TypedAction;

public interface ComputationService {

	Long getComputationId();

	void setComputationId(Long gameId);

	boolean isComputing(Long gameId);

	boolean isStopComputation();

	void stopComputation(Long gameId);

	void updateComputationProgress(int progress);

	<T> T doInComputationContext(Long computationId, TypedAction<T> action) throws InterruptedException;

	void sendAnalysisMove(Cell analysedMove, int playingColor);

}