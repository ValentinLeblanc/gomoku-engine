package fr.leblanc.gomoku.engine.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.leblanc.gomoku.engine.controller.EngineWebSocketController;
import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.messaging.EngineMessageType;
import fr.leblanc.gomoku.engine.model.messaging.EngineWebSocketMessage;
import fr.leblanc.gomoku.engine.model.messaging.MoveDto;
import fr.leblanc.gomoku.engine.model.messaging.PercentCompleted;
import fr.leblanc.gomoku.engine.service.MessageService;

@Service
public class MessageServiceImpl implements MessageService {

	@Autowired
	private EngineWebSocketController webSocketController;

	@Override
	public void sendAnalysisCell(Cell analysisCell, int color) {
		send(EngineMessageType.ANALYSIS_MOVE, new MoveDto(analysisCell, color));
	}

	@Override
	public void sendPercentCompleted(int index, int percent) {
		send(EngineMessageType.COMPUTE_PROGRESS, new PercentCompleted(index, percent));

	}

	@Override
	public void sendIsRunning(boolean isRunning) {
		send(EngineMessageType.IS_RUNNING, isRunning);
	}
	
	@Override
	public void sendRefreshMove(MoveDto move) {
		send(EngineMessageType.REFRESH_MOVE, move);
	}
	
	public void send(EngineMessageType type, Object content) {
		
		EngineWebSocketMessage webSocketMessage = new EngineWebSocketMessage();
		
		webSocketMessage.setType(type);
		webSocketMessage.setContent(content);
		
		webSocketController.sendMessage(webSocketMessage);
		
	}


}
