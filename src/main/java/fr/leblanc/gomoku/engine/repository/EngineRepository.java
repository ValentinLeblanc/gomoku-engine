package fr.leblanc.gomoku.engine.repository;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import fr.leblanc.gomoku.engine.controller.EngineWebSocketController;
import fr.leblanc.gomoku.engine.model.EngineMessageType;
import fr.leblanc.gomoku.engine.model.EngineWebSocketMessage;

@Repository
public class EngineRepository {
	
	@Autowired
	private EngineWebSocketController webSocketController;
	
	public void sendMessageToWebApp(JSONObject engineMessage) {

		EngineWebSocketMessage webSocketMessage = new EngineWebSocketMessage();
		
		webSocketMessage.setType(EngineMessageType.valueOf(engineMessage.getString("type")));
		webSocketMessage.setContent(engineMessage.get("content"));

		webSocketController.sendMessage(webSocketMessage);
		
	}
}
