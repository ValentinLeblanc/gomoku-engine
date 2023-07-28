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
	public void sendAnalysisMove(MoveDTO move) {
		sendWebSocketMessage(EngineMessageType.ANALYSIS_MOVE, move);
	}

	@Override
	public void sendComputingProgress(int progress) {
		sendWebSocketMessage(EngineMessageType.COMPUTING_PROGRESS, progress);
	}

	@Override
	public void sendIsComputing(boolean isComputing) {
		sendWebSocketMessage(EngineMessageType.IS_COMPUTING, isComputing);
	}
	
	@Override
	public void sendRefreshMove(MoveDTO move) {
		sendWebSocketMessage(EngineMessageType.REFRESH_MOVE, move);
	}
	
	private void sendWebSocketMessage(EngineMessageType type, Object content) {
		EngineWebSocketMessage webSocketMessage = new EngineWebSocketMessage();
		webSocketMessage.setType(type);
		webSocketMessage.setContent(content);
		template.convertAndSend("/engine/public", webSocketMessage);
	}
	
}
