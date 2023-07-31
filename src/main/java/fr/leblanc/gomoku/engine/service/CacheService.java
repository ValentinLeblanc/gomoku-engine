package fr.leblanc.gomoku.engine.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.EvaluationResult;
import fr.leblanc.gomoku.engine.model.GameData;
import fr.leblanc.gomoku.engine.model.MinMaxResult;

public interface CacheService {

	void clearCache(Long gameId);

	Map<Integer, Map<GameData, Optional<Cell>>> getDirectStrikeCache();

	Map<Integer, Map<GameData, Optional<Cell>>> getSecondaryStrikeCache();

	Map<Integer, Map<GameData, List<Cell>>> getCounterStrikeCache();

	Map<Integer, Map<GameData, EvaluationResult>> getEvaluationCache();

	Map<Integer, Map<GameData, MinMaxResult>> getMinMaxCache();

	boolean isCacheEnabled();

}