package fr.leblanc.gomoku.engine.util.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.EvaluationResult;
import fr.leblanc.gomoku.engine.model.GameData;
import fr.leblanc.gomoku.engine.model.GomokuColor;
import fr.leblanc.gomoku.engine.model.MinMaxResult;

public class GomokuCache {
	
	private boolean isCacheEnabled = false;
	
	private Map<Integer, Map<GameData, Optional<Cell>>> directStrikeCache = new HashMap<>();
	private Map<Integer, Map<GameData, Optional<Cell>>> secondaryStrikeCache = new HashMap<>();
	private Map<Integer, Map<GameData, List<Cell>>> counterStrikeCache = new HashMap<>();
	private Map<Integer, Map<GameData, EvaluationResult>> evaluationCache = new HashMap<>();
	private Map<Integer, Map<GameData, MinMaxResult>> minMaxCache = new HashMap<>();

	public GomokuCache() {
		directStrikeCache.put(GomokuColor.BLACK_COLOR, new HashMap<>());
		directStrikeCache.put(GomokuColor.WHITE_COLOR, new HashMap<>());
		secondaryStrikeCache.put(GomokuColor.BLACK_COLOR, new HashMap<>());
		secondaryStrikeCache.put(GomokuColor.WHITE_COLOR, new HashMap<>());
		counterStrikeCache.put(GomokuColor.BLACK_COLOR, new HashMap<>());
		counterStrikeCache.put(GomokuColor.WHITE_COLOR, new HashMap<>());
		evaluationCache.put(GomokuColor.BLACK_COLOR, new HashMap<>());
		evaluationCache.put(GomokuColor.WHITE_COLOR, new HashMap<>());
		minMaxCache.put(GomokuColor.BLACK_COLOR, new HashMap<>());
		minMaxCache.put(GomokuColor.WHITE_COLOR, new HashMap<>());
	}
	
	public Map<Integer, Map<GameData, Optional<Cell>>> getDirectStrikeCache() {
		return directStrikeCache;
	}

	public Map<Integer, Map<GameData, Optional<Cell>>> getSecondaryStrikeCache() {
		return secondaryStrikeCache;
	}

	public Map<Integer, Map<GameData, List<Cell>>> getCounterStrikeCache() {
		return counterStrikeCache;
	}

	public Map<Integer, Map<GameData, EvaluationResult>> getEvaluationCache() {
		return evaluationCache;
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

}
