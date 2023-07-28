package fr.leblanc.gomoku.engine.service;

import fr.leblanc.gomoku.engine.model.messaging.MoveDTO;

public interface WebSocketService {

	public void sendAnalysisMove(MoveDTO move);
	
	void sendComputingProgress(int progress);
	
	void sendIsComputing(boolean isComputing);
	
	void sendRefreshMove(MoveDTO move);

}
