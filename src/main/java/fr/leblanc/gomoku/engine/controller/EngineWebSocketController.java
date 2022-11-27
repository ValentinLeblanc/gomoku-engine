package fr.leblanc.gomoku.engine.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import fr.leblanc.gomoku.engine.model.messaging.EngineWebSocketMessage;

@Controller
public class EngineWebSocketController
{
    @Autowired
    private SimpMessagingTemplate template;
    
    public void sendMessage(final EngineWebSocketMessage message) {
    	template.convertAndSend("/engine/public", message);
    }
}