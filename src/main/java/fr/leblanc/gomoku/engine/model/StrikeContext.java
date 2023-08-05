package fr.leblanc.gomoku.engine.model;

public class StrikeContext {

	private Long gameId;
	private int strikeDepth;
	private int strikeTimeout;
	
	public StrikeContext(Long gameId, int strikeDepth, int strikeTimeout) {
		super();
		this.gameId = gameId;
		this.strikeDepth = strikeDepth;
		this.strikeTimeout = strikeTimeout;
	}

	public Long getGameId() {
		return gameId;
	}
	
	public void setGameId(Long gameId) {
		this.gameId = gameId;
	}
	
	public int getStrikeDepth() {
		return strikeDepth;
	}

	public void setStrikeDepth(int depth) {
		this.strikeDepth = depth;
	}

	public int getStrikeTimeout() {
		return strikeTimeout;
	}

	public void setStrikeTimeout(int strikeTimeout) {
		this.strikeTimeout = strikeTimeout;
	}
	
}
