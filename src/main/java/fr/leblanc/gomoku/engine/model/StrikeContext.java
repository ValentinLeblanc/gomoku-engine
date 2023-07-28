package fr.leblanc.gomoku.engine.model;

import java.util.Objects;

public class StrikeContext {

	private int strikeDepth;
	
	private int minMaxDepth;
	
	private int strikeTimeout;
	
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
	
	@Override
	public int hashCode() {
		return Objects.hash(minMaxDepth, strikeDepth, strikeTimeout);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StrikeContext other = (StrikeContext) obj;
		return minMaxDepth == other.minMaxDepth && strikeDepth == other.strikeDepth
				&& strikeTimeout == other.strikeTimeout;
	}

	@Override
	public String toString() {
		return "StrikeContext [strikeDepth=" + strikeDepth + ", minMaxDepth=" + minMaxDepth + ", strikeTimeout="
				+ strikeTimeout + "]";
	}

	
}
