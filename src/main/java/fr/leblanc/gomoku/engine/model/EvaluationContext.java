package fr.leblanc.gomoku.engine.model;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class EvaluationContext {

	private double evaluation = 0;
	
	private int contextColor;
	
	private int depth = 0;
	
	private Map<Cell, Map<Integer, Double>> map = new HashMap<>();
	
	public void addContribution(Cell cell, int color, double value) {
		Double currentValue = map.computeIfAbsent(cell, c -> new HashMap<>()).computeIfAbsent(color, c -> 0d);
		
		if (color == contextColor) {
			currentValue += value;
			evaluation += value;
		} else {
			currentValue -= value;
			evaluation -= value;
		}
		
		map.get(cell).put(color, currentValue);
	}

	public EvaluationContext(int contextColor) {
		this.contextColor = contextColor;
	}
	
	public void increaseDepth() {
		this.depth++;
	}
	
	public void decreaseDepth() {
		this.depth--;
	}
}
