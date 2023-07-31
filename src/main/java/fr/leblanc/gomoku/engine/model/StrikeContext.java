package fr.leblanc.gomoku.engine.model;

public class StrikeContext {

	private Long gameId;
	private int strikeDepth;
	private int minMaxDepth;
	private int strikeTimeout;
	
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

	public int getMinMaxDepth() {
		return minMaxDepth;
	}

	public void setMinMaxDepth(int minMaxDepth) {
		this.minMaxDepth = minMaxDepth;
	}

	public int getStrikeTimeout() {
		return strikeTimeout;
	}

	public void setStrikeTimeout(int strikeTimeout) {
		this.strikeTimeout = strikeTimeout;
	}
	
}
