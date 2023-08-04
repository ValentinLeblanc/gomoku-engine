package fr.leblanc.gomoku.engine.model.messaging;

public class GameSettings {

	private boolean strikeEnabled = true;
	
	private boolean minMaxEnabled = true;
	
	private boolean displayAnalysis = false;
	
	private int minMaxExtent = -1;
	
	private int minMaxDepth;
	
	private int strikeDepth;
	
	private int strikeTimeout;
	
	public boolean isDisplayAnalysis() {
		return displayAnalysis;
	}
	
	public void setDisplayAnalysis(boolean displayAnalysis) {
		this.displayAnalysis = displayAnalysis;
	}

	public boolean isStrikeEnabled() {
		return strikeEnabled;
	}

	public void setStrikeEnabled(boolean strikeEnabled) {
		this.strikeEnabled = strikeEnabled;
	}

	public boolean isMinMaxEnabled() {
		return minMaxEnabled;
	}

	public void setMinMaxEnabled(boolean minMaxEnabled) {
		this.minMaxEnabled = minMaxEnabled;
	}

	public int getMinMaxExtent() {
		return minMaxExtent;
	}

	public void setMinMaxExtent(int minMaxExtent) {
		this.minMaxExtent = minMaxExtent;
	}

	public int getMinMaxDepth() {
		return minMaxDepth;
	}

	public void setMinMaxDepth(int minMaxDepth) {
		this.minMaxDepth = minMaxDepth;
	}

	public int getStrikeDepth() {
		return strikeDepth;
	}

	public void setStrikeDepth(int strikeDepth) {
		this.strikeDepth = strikeDepth;
	}

	public int getStrikeTimeout() {
		return strikeTimeout;
	}

	public void setStrikeTimeout(int strikeTimeout) {
		this.strikeTimeout = strikeTimeout;
	}

}
