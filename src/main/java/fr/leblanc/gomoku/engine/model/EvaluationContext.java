package fr.leblanc.gomoku.engine.model;

import java.util.Objects;

public class EvaluationContext {
	
	private GameData dataWrapper;

	private int playingColor;
	
	private int maxDepth;
	
	private int depth;
	
	private boolean logEnabled;
	
	public EvaluationContext(GameData dataWrapper, int playingColor, int maxDepth, int depth, boolean logEnabled) {
		super();
		this.dataWrapper = dataWrapper;
		this.playingColor = playingColor;
		this.maxDepth = maxDepth;
		this.depth = depth;
		this.logEnabled = logEnabled;
	}

	public int getPlayingColor() {
		return playingColor;
	}

	public void setPlayingColor(int playingColor) {
		this.playingColor = playingColor;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public boolean isLogEnabled() {
		return logEnabled;
	}

	public void setLogEnabled(boolean logEnabled) {
		this.logEnabled = logEnabled;
	}

	public GameData getDataWrapper() {
		return dataWrapper;
	}

	public int getMaxDepth() {
		return maxDepth;
	}

	public void reversePlayingColor() {
		playingColor = -playingColor;
	}
	
	public void increaseDepth() {
		this.depth++;
	}
	
	public void decreaseDepth() {
		this.depth--;
	}

	@Override
	public int hashCode() {
		return Objects.hash(dataWrapper, depth, logEnabled, maxDepth, playingColor);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EvaluationContext other = (EvaluationContext) obj;
		return Objects.equals(dataWrapper, other.dataWrapper) && depth == other.depth && logEnabled == other.logEnabled
				&& maxDepth == other.maxDepth && playingColor == other.playingColor;
	}

	@Override
	public String toString() {
		return "EvaluationContext [dataWrapper=" + dataWrapper + ", playingColor=" + playingColor + ", maxDepth="
				+ maxDepth + ", depth=" + depth + ", external=" + logEnabled + "]";
	}
	
}
