package fr.leblanc.gomoku.engine.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StrikeContext {

	private int strikeDepth;
	
	private int minMaxDepth;
	
	private int strikeTimeout;
	
	private Map<Integer, Set<DataWrapper>> directStrikeAttempts = new HashMap<>();
	
	private Map<Integer, Set<DataWrapper>> secondaryStrikeAttempts = new HashMap<>();
	
	private Map<Integer, Map<DataWrapper, List<Cell>>> recordedCounterMoves = new HashMap<>();
	
	public int getStrikeDepth() {
		return strikeDepth;
	}

	public void setStrikeDepth(int depth) {
		this.strikeDepth = depth;
	}

	public Map<Integer, Set<DataWrapper>> getDirectStrikeAttempts() {
		return directStrikeAttempts;
	}

	public void setStrikeAttempts(Map<Integer, Set<DataWrapper>> strikeAttempts) {
		this.directStrikeAttempts = strikeAttempts;
	}

	public Map<Integer, Set<DataWrapper>> getSecondaryStrikeAttempts() {
		return secondaryStrikeAttempts;
	}

	public void setSecondaryStrikeAttempts(Map<Integer, Set<DataWrapper>> secondaryStrikeAttempts) {
		this.secondaryStrikeAttempts = secondaryStrikeAttempts;
	}

	public Map<Integer, Map<DataWrapper, List<Cell>>> getRecordedCounterMoves() {
		return recordedCounterMoves;
	}

	public void setRecordedCounterMoves(Map<Integer, Map<DataWrapper, List<Cell>>> recordedCounterMoves) {
		this.recordedCounterMoves = recordedCounterMoves;
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
