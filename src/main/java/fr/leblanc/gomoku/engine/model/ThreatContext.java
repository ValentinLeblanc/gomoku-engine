package fr.leblanc.gomoku.engine.model;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Data;

@Data
public class ThreatContext {

	private Map<Cell, Map<ThreatType, List<Threat>>> cellToThreatMap = new HashMap<>();

	private Map<ThreatType, Set<Cell>> threatToCellMap = new EnumMap<>(ThreatType.class);

	private int[][] data;

	private int playingColor;

	public ThreatContext(int[][] data, int playingColor) {
		this.data = data;
		this.playingColor = playingColor;
		
		for (ThreatType threatType : ThreatType.values()) {
			threatToCellMap.put(threatType, new HashSet<>());
		}
	}
	
}
