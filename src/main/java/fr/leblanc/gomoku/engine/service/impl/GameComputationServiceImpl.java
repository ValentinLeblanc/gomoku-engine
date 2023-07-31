package fr.leblanc.gomoku.engine.service.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Service;

import fr.leblanc.gomoku.engine.service.GameComputationService;
import fr.leblanc.gomoku.engine.util.TypedAction;

@Service
public class GameComputationServiceImpl implements GameComputationService {
	
	private ConcurrentMap<Long, Boolean> isGameComputingMap = new ConcurrentHashMap<>();
	private ConcurrentMap<Long, Boolean> stopGameComputationMap = new ConcurrentHashMap<>();
	
	@Override
	public <T> T startGameComputation(Long gameId, TypedAction<T> action) throws InterruptedException {
		
		if (isGameComputing(gameId)) {
			throw new IllegalStateException("Computation is already ongoing: " + gameId);
		}
		
		isGameComputingMap.put(gameId, Boolean.TRUE);
		try {
			return action.run();
		} finally {
			isGameComputingMap.put(gameId, Boolean.FALSE);
		}
	}
	
	@Override
	public boolean isGameComputing(Long gameId) {
		return isGameComputingMap.computeIfAbsent(gameId, k -> Boolean.FALSE).booleanValue();
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
			isGameComputingMap.put(gameId, Boolean.FALSE);
			stopGameComputationMap.put(gameId, Boolean.FALSE);
		}
	}
	
}
