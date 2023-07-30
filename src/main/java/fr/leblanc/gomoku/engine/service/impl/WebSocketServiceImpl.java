package fr.leblanc.gomoku.engine.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import fr.leblanc.gomoku.engine.model.messaging.EngineMessageType;
import fr.leblanc.gomoku.engine.model.messaging.EngineWebSocketMessage;
import fr.leblanc.gomoku.engine.service.WebSocketService;

@Service
public class WebSocketServiceImpl implements WebSocketService {

	 @Autowired
    private SimpMessagingTemplate template;

	@Override
	public void sendMessage(EngineMessageType type, Long gameId, Object content) {
		EngineWebSocketMessage webSocketMessage = new EngineWebSocketMessage();
		webSocketMessage.setGameId(gameId);
		webSocketMessage.setType(type);
		webSocketMessage.setContent(content);
		template.convertAndSend("/engine/public", webSocketMessage);
	}
	
}
