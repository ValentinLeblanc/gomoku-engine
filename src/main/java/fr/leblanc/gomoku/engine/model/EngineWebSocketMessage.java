package fr.leblanc.gomoku.engine.model;


import lombok.Data;

@Data
public class EngineWebSocketMessage
{
    private EngineMessageType type;
    private Object content;
    
}