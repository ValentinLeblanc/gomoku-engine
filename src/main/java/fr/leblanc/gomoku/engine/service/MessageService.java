package fr.leblanc.gomoku.engine.service;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.messaging.MoveDto;

public interface MessageService {

	public void sendAnalysisCell(Cell analysisCell, int color);
	
	void sendPercentCompleted(int index, int percent);
	
	void sendIsRunning(boolean isRunning);
	
	void sendRefreshMove(MoveDto move);

}
