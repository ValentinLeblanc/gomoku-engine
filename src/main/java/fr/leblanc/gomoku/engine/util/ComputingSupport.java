package fr.leblanc.gomoku.engine.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ComputingSupport {
	
	private ComputingSupport() {
		
	}

	private static ConcurrentMap<Long, Boolean> isComputingMap = new ConcurrentHashMap<>();
	private static ConcurrentMap<Long, Boolean> stopComputationMap = new ConcurrentHashMap<>();

	public static <T> T doInComputingContext(Long gameId, TypedAction<T> action) throws InterruptedException {
		isComputingMap.put(gameId, Boolean.TRUE);
		
		try {
			return action.run();
		} finally {
			isComputingMap.put(gameId, Boolean.FALSE);
		}
	}
	
	public static boolean isComputing(Long gameId) {
		return isComputingMap.computeIfAbsent(gameId, k -> Boolean.FALSE).booleanValue();
	}
	
	public static boolean isStopComputation(Long gameId) {
		return stopComputationMap.computeIfAbsent(gameId, k -> Boolean.FALSE).booleanValue();
	}
	
	public static void stopComputation(Long gameId) {
		try {
			stopComputationMap.put(gameId, Boolean.TRUE);
			// need to wait for all threads to stop
			Thread.sleep(100);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} finally {
			isComputingMap.put(gameId, Boolean.FALSE);
			stopComputationMap.put(gameId, Boolean.FALSE);
		}
	}
}
