package fr.leblanc.gomoku.engine.model.messaging;

import java.util.Objects;

public class EngineSettingsDto {

	private boolean strikeEnabled = true;
	
	private boolean minMaxEnabled = true;
	
	private int minMaxExtent = -1;
	
	private int minMaxDepth;
	
	private int strikeDepth;
	
	private int strikeTimeout;

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

	@Override
	public int hashCode() {
		return Objects.hash(minMaxDepth, minMaxEnabled, minMaxExtent, strikeDepth, strikeEnabled, strikeTimeout);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EngineSettingsDto other = (EngineSettingsDto) obj;
		return minMaxDepth == other.minMaxDepth && minMaxEnabled == other.minMaxEnabled
				&& minMaxExtent == other.minMaxExtent && strikeDepth == other.strikeDepth
				&& strikeEnabled == other.strikeEnabled && strikeTimeout == other.strikeTimeout;
	}

	@Override
	public String toString() {
		return "EngineSettingsDto [strikeEnabled=" + strikeEnabled + ", minMaxEnabled=" + minMaxEnabled
				+ ", minMaxExtent=" + minMaxExtent + ", minMaxDepth=" + minMaxDepth + ", strikeDepth=" + strikeDepth
				+ ", strikeTimeout=" + strikeTimeout + "]";
	}
	
}
