package fr.leblanc.gomoku.engine.model;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class MinMaxContext {
	
	private Map<Integer, Double> minList = new HashMap<>();
	
	private Map<Integer, Double> maxList = new HashMap<>();
	
	private Map<Integer, Map<DataWrapper, Double>> evaluationCache = new HashMap<>();
	
}
