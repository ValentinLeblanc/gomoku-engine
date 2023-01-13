package fr.leblanc.gomoku.engine.util.cache;

public interface CachedAction<T> {
	T run() throws InterruptedException;
}
