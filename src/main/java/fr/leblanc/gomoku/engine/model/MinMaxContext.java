package fr.leblanc.gomoku.engine.model;

import java.util.HashMap;
import java.util.Map;

public class MinMaxContext {
	
	private int currentIndex = 0;
	
	private int endIndex;
	
	private int indexDepth = 0;
	
	private Map<Integer, Double> minList = new HashMap<>();
	
	private Map<Integer, Double> maxList = new HashMap<>();

	public int getCurrentIndex() {
		return currentIndex;
	}

	public void setCurrentIndex(int currentIndex) {
		this.currentIndex = currentIndex;
	}

	public int getEndIndex() {
		return endIndex;
	}

	public void setEndIndex(int endIndex) {
		this.endIndex = endIndex;
	}

	public int getIndexDepth() {
		return indexDepth;
	}

	public void setIndexDepth(int indexDepth) {
		this.indexDepth = indexDepth;
	}

	public Map<Integer, Double> getMinList() {
		return minList;
	}

	public void setMinList(Map<Integer, Double> minList) {
		this.minList = minList;
	}

	public Map<Integer, Double> getMaxList() {
		return maxList;
	}

	public void setMaxList(Map<Integer, Double> maxList) {
		this.maxList = maxList;
	}
	
}
