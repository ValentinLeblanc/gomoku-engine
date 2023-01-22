package fr.leblanc.gomoku.engine.util.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.DataWrapper;
import fr.leblanc.gomoku.engine.model.EngineConstants;
import fr.leblanc.gomoku.engine.model.EvaluationResult;
import fr.leblanc.gomoku.engine.model.MinMaxResult;
import fr.leblanc.gomoku.engine.model.ThreatContext;

public class GomokuCache {
	
	private boolean isCacheEnabled = false;
	
	private Map<Integer, Map<DataWrapper, Cell>> directStrikeAttempts = new HashMap<>();
	
	private Map<Integer, Map<DataWrapper, Cell>> secondaryStrikeAttempts = new HashMap<>();
	
	private Map<Integer, Map<DataWrapper, List<Cell>>> recordedCounterMoves = new HashMap<>();

	private Map<Integer,  Map<DataWrapper, ThreatContext>> threatContextCache = new HashMap<>();
	
	private Map<Integer, Map<DataWrapper, EvaluationResult>> evaluationCache = new HashMap<>();
	
	private Map<Integer, Map<DataWrapper, MinMaxResult>> minMaxCache = new HashMap<>();

	public GomokuCache() {
		directStrikeAttempts.put(EngineConstants.BLACK_COLOR, new HashMap<>());
		directStrikeAttempts.put(EngineConstants.WHITE_COLOR, new HashMap<>());
		secondaryStrikeAttempts.put(EngineConstants.BLACK_COLOR, new HashMap<>());
		secondaryStrikeAttempts.put(EngineConstants.WHITE_COLOR, new HashMap<>());
		recordedCounterMoves.put(EngineConstants.BLACK_COLOR, new HashMap<>());
		recordedCounterMoves.put(EngineConstants.WHITE_COLOR, new HashMap<>());
		threatContextCache.put(EngineConstants.BLACK_COLOR, new HashMap<>());
		threatContextCache.put(EngineConstants.WHITE_COLOR, new HashMap<>());
		evaluationCache.put(EngineConstants.BLACK_COLOR, new HashMap<>());
		evaluationCache.put(EngineConstants.WHITE_COLOR, new HashMap<>());
		minMaxCache.put(EngineConstants.BLACK_COLOR, new HashMap<>());
		minMaxCache.put(EngineConstants.WHITE_COLOR, new HashMap<>());
	}
	
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

	public Map<Integer,  Map<DataWrapper, ThreatContext>> getThreatContextCache() {
		return threatContextCache;
	}

	public void setThreatContextCache(Map<Integer,  Map<DataWrapper, ThreatContext>> threatContextCache) {
		this.threatContextCache = threatContextCache;
	}

	public Map<Integer, Map<DataWrapper, EvaluationResult>> getEvaluationCache() {
		return evaluationCache;
	}

	public void setEvaluationCache(Map<Integer, Map<DataWrapper, EvaluationResult>> evaluationCache) {
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
