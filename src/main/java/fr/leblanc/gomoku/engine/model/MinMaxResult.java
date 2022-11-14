package fr.leblanc.gomoku.engine.model;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class MinMaxResult {
	
	private double evaluation;
	
	private Map<Integer, Cell> optimalMoves = new HashMap<>();
	
}
