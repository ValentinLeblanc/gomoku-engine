package fr.leblanc.gomoku.engine.model.messaging;


import lombok.Data;

@Data
public class EngineWebSocketMessage
{
    private EngineMessageType type;
    private Object content;
    
}