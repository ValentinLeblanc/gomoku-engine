package fr.leblanc.gomoku.engine.util;

import lombok.ToString;

@ToString
public class Pair<T> {

	private T first;
	
	private T second;
	
	public Pair(T first, T second) {
		this.first = first;
		this.second = second;
	}
	
	public T getFirst() {
		return first;
	}
	
	public T getSecond() {
		return second;
	}
}
