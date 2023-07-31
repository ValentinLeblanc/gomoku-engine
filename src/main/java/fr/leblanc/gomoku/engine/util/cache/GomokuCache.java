package fr.leblanc.gomoku.engine.util.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.EngineConstants;
import fr.leblanc.gomoku.engine.model.EvaluationResult;
import fr.leblanc.gomoku.engine.model.GameData;
import fr.leblanc.gomoku.engine.model.MinMaxResult;

public class GomokuCache {
	
	private boolean isCacheEnabled = false;
	
	private Map<Integer, Map<GameData, Optional<Cell>>> directStrikeAttempts = new HashMap<>();
	private Map<Integer, Map<GameData, Optional<Cell>>> secondaryStrikeAttempts = new HashMap<>();
	private Map<Integer, Map<GameData, List<Cell>>> recordedCounterMoves = new HashMap<>();
	private Map<Integer, Map<GameData, EvaluationResult>> evaluationCache = new HashMap<>();
	private Map<Integer, Map<GameData, MinMaxResult>> minMaxCache = new HashMap<>();

	public GomokuCache() {
		directStrikeAttempts.put(EngineConstants.BLACK_COLOR, new HashMap<>());
		directStrikeAttempts.put(EngineConstants.WHITE_COLOR, new HashMap<>());
		secondaryStrikeAttempts.put(EngineConstants.BLACK_COLOR, new HashMap<>());
		secondaryStrikeAttempts.put(EngineConstants.WHITE_COLOR, new HashMap<>());
		recordedCounterMoves.put(EngineConstants.BLACK_COLOR, new HashMap<>());
		recordedCounterMoves.put(EngineConstants.WHITE_COLOR, new HashMap<>());
		evaluationCache.put(EngineConstants.BLACK_COLOR, new HashMap<>());
		evaluationCache.put(EngineConstants.WHITE_COLOR, new HashMap<>());
		minMaxCache.put(EngineConstants.BLACK_COLOR, new HashMap<>());
		minMaxCache.put(EngineConstants.WHITE_COLOR, new HashMap<>());
	}
	
	public Map<Integer, Map<GameData, Optional<Cell>>> getDirectStrikeAttempts() {
		return directStrikeAttempts;
	}

	public void setDirectStrikeAttempts(Map<Integer, Map<GameData, Optional<Cell>>> directStrikeAttempts) {
		this.directStrikeAttempts = directStrikeAttempts;
	}

	public Map<Integer, Map<GameData, Optional<Cell>>> getSecondaryStrikeAttempts() {
		return secondaryStrikeAttempts;
	}

	public void setSecondaryStrikeAttempts(Map<Integer, Map<GameData, Optional<Cell>>> secondaryStrikeAttempts) {
		this.secondaryStrikeAttempts = secondaryStrikeAttempts;
	}

	public Map<Integer, Map<GameData, List<Cell>>> getRecordedCounterMoves() {
		return recordedCounterMoves;
	}

	public void setRecordedCounterMoves(Map<Integer, Map<GameData, List<Cell>>> recordedCounterMoves) {
		this.recordedCounterMoves = recordedCounterMoves;
	}

	public Map<Integer, Map<GameData, EvaluationResult>> getEvaluationCache() {
		return evaluationCache;
	}

	public void setEvaluationCache(Map<Integer, Map<GameData, EvaluationResult>> evaluationCache) {
		this.evaluationCache = evaluationCache;
	}

	public boolean isCacheEnabled() {
		return isCacheEnabled;
	}

	public void setCacheEnabled(boolean isCacheEnabled) {
		this.isCacheEnabled = isCacheEnabled;
	}

	public Map<Integer, Map<GameData, MinMaxResult>> getMinMaxCache() {
		return minMaxCache;
	}

	public void setMinMaxCache(Map<Integer, Map<GameData, MinMaxResult>> minMaxCache) {
		this.minMaxCache = minMaxCache;
	}
	
}
