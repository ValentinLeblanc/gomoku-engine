package fr.leblanc.gomoku.engine.service.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import fr.leblanc.gomoku.engine.service.GameComputationService;
import fr.leblanc.gomoku.engine.util.TypedAction;

@Service
public class GameComputationServiceImpl implements GameComputationService {
	
	private Map<Long, Boolean> gameComputingMap = new ConcurrentHashMap<>();
	private Map<Long, Boolean> stopGameComputationMap = new ConcurrentHashMap<>();
	private Map<Long, Boolean> displayAnalysisMap = new ConcurrentHashMap<>();
	
	@Override
	public <T> T startGameComputation(Long gameId, boolean displayAnalysis, TypedAction<T> action) throws InterruptedException {
		
		if (isGameComputing(gameId)) {
			throw new IllegalStateException("Computation is already ongoing: " + gameId);
		}
		
		displayAnalysisMap.put(gameId, Boolean.valueOf(displayAnalysis));
		gameComputingMap.put(gameId, Boolean.TRUE);
		try {
			return action.run();
		} finally {
			gameComputingMap.put(gameId, Boolean.FALSE);
		}
	}
	
	@Override
	public boolean isGameComputing(Long gameId) {
		return gameComputingMap.computeIfAbsent(gameId, k -> Boolean.FALSE).booleanValue();
	}
	
	@Override
	public boolean isGameComputationStopped(Long gameId) {
		if (gameId != null) {
			return stopGameComputationMap.computeIfAbsent(gameId, k -> Boolean.FALSE).booleanValue();
		}
		return false;
	}
	
	@Override
	public void stopGameComputation(Long gameId) {
		try {
			stopGameComputationMap.put(gameId, Boolean.TRUE);
			// need to wait for all threads to stop
			Thread.sleep(100);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} finally {
			gameComputingMap.put(gameId, Boolean.FALSE);
			stopGameComputationMap.put(gameId, Boolean.FALSE);
		}
	}

	@Override
	public boolean isDisplayAnalysis(Long gameId) {
		return displayAnalysisMap.computeIfAbsent(gameId, k -> Boolean.FALSE).booleanValue();
	}
	
}
