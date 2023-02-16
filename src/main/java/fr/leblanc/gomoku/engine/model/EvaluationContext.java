package fr.leblanc.gomoku.engine.model;

import java.util.Objects;

public class EvaluationContext {
	
	private DataWrapper dataWrapper;

	private int playingColor;
	
	private int maxDepth;
	
	private int depth;
	
	private boolean external;
	
	public EvaluationContext(DataWrapper dataWrapper, int playingColor, int maxDepth, int depth, boolean external) {
		super();
		this.dataWrapper = dataWrapper;
		this.playingColor = playingColor;
		this.maxDepth = maxDepth;
		this.depth = depth;
		this.external = external;
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

	public boolean isExternal() {
		return external;
	}

	public void setExternal(boolean external) {
		this.external = external;
	}

	public DataWrapper getDataWrapper() {
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
		return Objects.hash(dataWrapper, depth, external, maxDepth, playingColor);
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
		return Objects.equals(dataWrapper, other.dataWrapper) && depth == other.depth && external == other.external
				&& maxDepth == other.maxDepth && playingColor == other.playingColor;
	}

	@Override
	public String toString() {
		return "EvaluationContext [dataWrapper=" + dataWrapper + ", playingColor=" + playingColor + ", maxDepth="
				+ maxDepth + ", depth=" + depth + ", external=" + external + "]";
	}
	
}
