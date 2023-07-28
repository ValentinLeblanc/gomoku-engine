package fr.leblanc.gomoku.engine.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class MinMaxContext {
	
	private AtomicInteger currentIndex = new AtomicInteger(0);
	private int endIndex;
	private int indexDepth = 0;
	private int maxDepth;
	private int playingColor;
	private boolean findMax;
	private Map<Integer, Double> minList = new HashMap<>();
	private Map<Integer, Double> maxList = new HashMap<>();
	private AtomicReference<Double> optimumReference;
	
	public MinMaxContext() {
		
	}
	
	public MinMaxContext(MinMaxContext context) {
		this.currentIndex = context.currentIndex;
		this.maxDepth = context.maxDepth;
		this.endIndex = context.endIndex;
		this.indexDepth = context.indexDepth;
		this.findMax = context.findMax;
		this.currentIndex = context.currentIndex;
		this.optimumReference = context.optimumReference;
		this.playingColor = context.playingColor;
	}
	
	public int getPlayingColor() {
		return playingColor;
	}
	
	public void setPlayingColor(int playingColor) {
		this.playingColor = playingColor;
	}

	public boolean isFindMax() {
		return findMax;
	}

	public void setFindMax(boolean findMax) {
		this.findMax = findMax;
	}

	public int getMaxDepth() {
		return maxDepth;
	}

	public void setMaxDepth(int maxDepth) {
		this.maxDepth = maxDepth;
	}

	public AtomicReference<Double> getOptimumReference() {
		return optimumReference;
	}

	public void setOptimumReference(AtomicReference<Double> firstMin) {
		this.optimumReference = firstMin;
	}

	public AtomicInteger getCurrentIndex() {
		return currentIndex;
	}

	public void setCurrentIndex(AtomicInteger currentIndex) {
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

	@Override
	public int hashCode() {
		return Objects.hash(currentIndex, endIndex, indexDepth, maxList, minList);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MinMaxContext other = (MinMaxContext) obj;
		return currentIndex == other.currentIndex && endIndex == other.endIndex && indexDepth == other.indexDepth
				&& Objects.equals(maxList, other.maxList) && Objects.equals(minList, other.minList);
	}

	@Override
	public String toString() {
		return "MinMaxContext [currentIndex=" + currentIndex + ", endIndex=" + endIndex + ", indexDepth=" + indexDepth
				+ ", minList=" + minList + ", maxList=" + maxList + "]";
	}
	
}
