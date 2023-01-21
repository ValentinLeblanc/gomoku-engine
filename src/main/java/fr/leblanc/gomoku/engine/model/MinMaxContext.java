package fr.leblanc.gomoku.engine.model;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class MinMaxContext {
	
	private int currentIndex;
	
	private int endIndex;
	
	private int indexDepth = 0;
	
	private Map<Integer, Double> minList = new HashMap<>();
	
	private Map<Integer, Double> maxList = new HashMap<>();
	
}
