package fr.leblanc.gomoku.engine.util.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.DataWrapper;
import fr.leblanc.gomoku.engine.model.MinMaxResult;

public class GomokuCache {
	
	private boolean isCacheEnabled = false;
	
	private Map<Integer, Map<DataWrapper, Cell>> directStrikeAttempts = new HashMap<>();
	
	private Map<Integer, Map<DataWrapper, Cell>> secondaryStrikeAttempts = new HashMap<>();
	
	private Map<Integer, Map<DataWrapper, List<Cell>>> recordedCounterMoves = new HashMap<>();

	private Map<Integer,  Map<DataWrapper, List<Cell>>> threatContextCache = new HashMap<>();
	
	private Map<Integer, Map<DataWrapper, Double>> evaluationCache = new HashMap<>();
	
	private Map<Integer, Map<DataWrapper, MinMaxResult>> minMaxCache = new HashMap<>();

	public Map<Integer, Map<DataWrapper, Cell>> getDirectStrikeAttempts() {
		return directStrikeAttempts;
	}

	public void setDirectStrikeAttempts(Map<Integer, Map<DataWrapper, Cell>> directStrikeAttempts) {
		this.directStrikeAttempts = directStrikeAttempts;
	}

	public Map<Integer, Map<DataWrapper, Cell>> getSecondaryStrikeAttempts() {
		return secondaryStrikeAttempts;
	}

	public void setSecondaryStrikeAttempts(Map<Integer, Map<DataWrapper, Cell>> secondaryStrikeAttempts) {
		this.secondaryStrikeAttempts = secondaryStrikeAttempts;
	}

	public Map<Integer, Map<DataWrapper, List<Cell>>> getRecordedCounterMoves() {
		return recordedCounterMoves;
	}

	public void setRecordedCounterMoves(Map<Integer, Map<DataWrapper, List<Cell>>> recordedCounterMoves) {
		this.recordedCounterMoves = recordedCounterMoves;
	}

	public Map<Integer,  Map<DataWrapper, List<Cell>>> getThreatContextCache() {
		return threatContextCache;
	}

	public void setThreatContextCache(Map<Integer,  Map<DataWrapper, List<Cell>>> threatContextCache) {
		this.threatContextCache = threatContextCache;
	}

	public Map<Integer, Map<DataWrapper, Double>> getEvaluationCache() {
		return evaluationCache;
	}

	public void setEvaluationCache(Map<Integer, Map<DataWrapper, Double>> evaluationCache) {
		this.evaluationCache = evaluationCache;
	}

	public boolean isCacheEnabled() {
		return isCacheEnabled;
	}

	public void setCacheEnabled(boolean isCacheEnabled) {
		this.isCacheEnabled = isCacheEnabled;
	}

	public Map<Integer, Map<DataWrapper, MinMaxResult>> getMinMaxCache() {
		return minMaxCache;
	}

	public void setMinMaxCache(Map<Integer, Map<DataWrapper, MinMaxResult>> minMaxCache) {
		this.minMaxCache = minMaxCache;
	}
	
}
