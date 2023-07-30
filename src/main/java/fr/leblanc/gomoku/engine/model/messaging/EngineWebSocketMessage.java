package fr.leblanc.gomoku.engine.model.messaging;

public class EngineWebSocketMessage
{
    private EngineMessageType type;
    
    private Object content;
    
    private Long gameId;
    
    public Long getGameId() {
		return gameId;
	}
    
    public void setGameId(Long gameId) {
		this.gameId = gameId;
	}
    
	public EngineMessageType getType() {
		return type;
	}
	public void setType(EngineMessageType type) {
		this.type = type;
	}
	public Object getContent() {
		return content;
	}
	public void setContent(Object content) {
		this.content = content;
	}
    
}