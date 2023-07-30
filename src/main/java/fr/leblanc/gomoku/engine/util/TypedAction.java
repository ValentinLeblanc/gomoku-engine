package fr.leblanc.gomoku.engine.util;

public interface TypedAction<T> {
	T run() throws InterruptedException;
}
