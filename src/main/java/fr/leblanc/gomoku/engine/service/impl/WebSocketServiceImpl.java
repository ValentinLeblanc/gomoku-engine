package fr.leblanc.gomoku.engine.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import fr.leblanc.gomoku.engine.model.messaging.EngineMessageType;
import fr.leblanc.gomoku.engine.model.messaging.EngineWebSocketMessage;
import fr.leblanc.gomoku.engine.model.messaging.MoveDTO;
import fr.leblanc.gomoku.engine.service.WebSocketService;

@Service
public class WebSocketServiceImpl implements WebSocketService {

	 @Autowired
    private SimpMessagingTemplate template;

	@Override
	public void sendAnalysisMove(Long id, MoveDTO move) {
		sendWebSocketMessage(EngineMessageType.ANALYSIS_MOVE, id, move);
	}

	@Override
	public void sendComputingProgress(Long id, int progress) {
		sendWebSocketMessage(EngineMessageType.COMPUTING_PROGRESS, id, progress);
	}

	@Override
	public void sendIsComputing(Long id, boolean isComputing) {
		sendWebSocketMessage(EngineMessageType.IS_COMPUTING, id, isComputing);
	}
	
	@Override
	public void sendRefreshMove(Long id, MoveDTO move) {
		sendWebSocketMessage(EngineMessageType.REFRESH_MOVE, id, move);
	}
	
	private void sendWebSocketMessage(EngineMessageType type, Long gameId, Object content) {
		EngineWebSocketMessage webSocketMessage = new EngineWebSocketMessage();
		webSocketMessage.setGameId(gameId);
		webSocketMessage.setType(type);
		webSocketMessage.setContent(content);
		template.convertAndSend("/engine/public", webSocketMessage);
	}
	
}
