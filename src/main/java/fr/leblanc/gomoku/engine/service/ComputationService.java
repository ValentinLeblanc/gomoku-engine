package fr.leblanc.gomoku.engine.service;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.util.TypedAction;

public interface ComputationService {

	Long getComputationId();

	void setComputationId(Long computationId);

	boolean isComputing(Long computationId);

	boolean isComputationStopped();

	void stopComputation(Long computationId);

	void sendMinMaxProgress(int progress);
	
	void sendStrikeProgress(boolean strikeProgress);

	<T> T doInComputationContext(Long computationId, TypedAction<T> action) throws InterruptedException;

	void sendAnalysisMove(Cell analysedMove, int playingColor);

}