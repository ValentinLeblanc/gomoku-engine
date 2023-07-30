package fr.leblanc.gomoku.engine.service;

import fr.leblanc.gomoku.engine.model.messaging.MoveDTO;

public interface WebSocketService {

	void sendRefreshMove(Long id, MoveDTO move);

	void sendIsComputing(Long id, boolean isComputing);

	void sendComputingProgress(Long id, int progress);

	void sendAnalysisMove(Long id, MoveDTO move);

}
