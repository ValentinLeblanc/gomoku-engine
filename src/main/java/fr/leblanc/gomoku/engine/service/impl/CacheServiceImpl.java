package fr.leblanc.gomoku.engine.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.EvaluationResult;
import fr.leblanc.gomoku.engine.model.GameData;
import fr.leblanc.gomoku.engine.model.MinMaxResult;
import fr.leblanc.gomoku.engine.service.CacheService;
import fr.leblanc.gomoku.engine.service.GameComputationService;
import fr.leblanc.gomoku.engine.util.cache.GomokuCache;

@Service
public class CacheServiceImpl implements CacheService {

	private final Map<Long, GomokuCache> cacheIdMap = new HashMap<>();
	
	@Autowired
	private GameComputationService gameComputationService;
	
	@Override
	public void clearCache(Long gameId) {
		cacheIdMap.remove(gameId);
	}

	@Override
	public Map<Integer, Map<GameData, Optional<Cell>>> getDirectStrikeCache() {
		return cacheIdMap.computeIfAbsent(gameComputationService.getCurrentGameId(), k-> new GomokuCache()).getDirectStrikeCache();
	}
	
	@Override
	public Map<Integer, Map<GameData, Optional<Cell>>> getSecondaryStrikeCache() {
		return cacheIdMap.computeIfAbsent(gameComputationService.getCurrentGameId(), k-> new GomokuCache()).getSecondaryStrikeCache();
	}
	
	@Override
	public Map<Integer, Map<GameData, List<Cell>>> getCounterStrikeCache() {
		return cacheIdMap.computeIfAbsent(gameComputationService.getCurrentGameId(), k-> new GomokuCache()).getCounterStrikeCache();
	}
	
	@Override
	public Map<Integer, Map<GameData, EvaluationResult>> getEvaluationCache() {
		return cacheIdMap.computeIfAbsent(gameComputationService.getCurrentGameId(), k-> new GomokuCache()).getEvaluationCache();
	}
	
	@Override
	public Map<Integer, Map<GameData, MinMaxResult>> getMinMaxCache() {
		return cacheIdMap.computeIfAbsent(gameComputationService.getCurrentGameId(), k-> new GomokuCache()).getMinMaxCache();
	}

	@Override
	public boolean isCacheEnabled() {
		return true;
	}

}
