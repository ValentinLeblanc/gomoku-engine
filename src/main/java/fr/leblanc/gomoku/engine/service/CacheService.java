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

	Map<Integer, Map<GameData, Optional<Cell>>> getDirectStrikeCache(Long gameId);

	Map<Integer, Map<GameData, Optional<Cell>>> getSecondaryStrikeCache(Long gameId);

	Map<Integer, Map<GameData, List<Cell>>> getCounterStrikeCache(Long gameId);

	Map<Integer, Map<GameData, EvaluationResult>> getEvaluationCache(Long gameId);

	Map<Integer, Map<GameData, MinMaxResult>> getMinMaxCache(Long gameId);

	boolean isCacheEnabled();

}