package fr.leblanc.gomoku.engine.util.cache;

import java.util.List;
import java.util.Map;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.DataWrapper;
import fr.leblanc.gomoku.engine.model.ThreatContext;

public class L2CacheSupport {

	private L2CacheSupport() {
		
	}
	
	private static ThreadLocal<GomokuCache> threadLocalCache = ThreadLocal.withInitial(GomokuCache::new);
	
	public static <T> T doInCacheContext(CachedAction<T> action) throws InterruptedException {
		return doInCacheContext(action, threadLocalCache.get());
	}
	
	public static <T> T doInCacheContext(CachedAction<T> action, GomokuCache gomokuCache) throws InterruptedException {
		try {
			gomokuCache.setCacheEnabled(true);
			threadLocalCache.set(gomokuCache);
			return action.run();
		} finally {
			threadLocalCache.remove();
			gomokuCache.setCacheEnabled(false);
		}
	}
	
	public static GomokuCache getCurrentCache() {
		return threadLocalCache.get();
	}
	
	public static boolean isCacheEnabled() {
		return threadLocalCache.get().isCacheEnabled();
	}
	
	public static Map<Integer, Map<DataWrapper, Cell>> getDirectStrikeAttempts() {
		return threadLocalCache.get().getDirectStrikeAttempts();
	}
	
	public static Map<Integer, Map<DataWrapper, Cell>> getSecondaryStrikeAttempts() {
		return threadLocalCache.get().getSecondaryStrikeAttempts();
	}

	public static Map<Integer, Map<DataWrapper, List<Cell>>> getRecordedCounterMoves() {
		return threadLocalCache.get().getRecordedCounterMoves();
	}

	public static Map<Integer,  Map<DataWrapper, ThreatContext>> getThreatContextCache() {
		return threadLocalCache.get().getThreatContextCache();
	}

	public static Map<Integer, Map<DataWrapper, Double>> getEvaluationCache() {
		return threadLocalCache.get().getEvaluationCache();
	}


}
