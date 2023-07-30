package fr.leblanc.gomoku.engine.service;

import fr.leblanc.gomoku.engine.model.messaging.EngineMessageType;

public interface WebSocketService {

	void sendMessage(EngineMessageType type, Long gameId, Object content);

}
