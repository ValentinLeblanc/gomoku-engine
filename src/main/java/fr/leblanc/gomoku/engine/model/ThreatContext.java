package fr.leblanc.gomoku.engine.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ThreatContext {

	private Map<Cell, Map<ThreatType, List<Threat>>> cellToThreatMap = new HashMap<>();
	private Map<ThreatType, List<Threat>> threatTypeToThreatMap = new EnumMap<>(ThreatType.class);
	private int[][] data;
	private int playingColor;

	public ThreatContext(int[][] data, int playingColor) {
		this.data = data;
		this.playingColor = playingColor;
		
		for (ThreatType threatType : ThreatType.values()) {
			threatTypeToThreatMap.put(threatType, new ArrayList<>());
		}
		
		threatTypeToThreatMap.put(ThreatType.THREAT_5, new ArrayList<>());
		threatTypeToThreatMap.put(ThreatType.THREAT_4, new ArrayList<>());
		threatTypeToThreatMap.put(ThreatType.THREAT_3, new ArrayList<>());
		threatTypeToThreatMap.put(ThreatType.THREAT_2, new ArrayList<>());
		threatTypeToThreatMap.put(ThreatType.DOUBLE_THREAT_4, new ArrayList<>());
		threatTypeToThreatMap.put(ThreatType.DOUBLE_THREAT_3, new ArrayList<>());
		threatTypeToThreatMap.put(ThreatType.DOUBLE_THREAT_2, new ArrayList<>());

	}

	@Deprecated(forRemoval = true)
	public Map<Cell, Map<ThreatType, List<Threat>>> getCellToThreatMap() {
		return cellToThreatMap;
	}
	
	public Map<ThreatType, List<Threat>> getThreatsOfCell(Cell cell) {
		return cellToThreatMap.computeIfAbsent(cell, k -> {
			EnumMap<ThreatType, List<Threat>> enumMap = new EnumMap<>(ThreatType.class);
			for (ThreatType threatType : ThreatType.values()) {
				enumMap.put(threatType, new ArrayList<>());
			}
			return enumMap;
		});
	}
	
	public List<Threat> getThreatsOfType(ThreatType threatType) {
		return threatTypeToThreatMap.get(threatType);
	}
	
	public int[][] getData() {
		return data;
	}

	public void setData(int[][] data) {
		this.data = data;
	}

	public int getPlayingColor() {
		return playingColor;
	}

	public void setPlayingColor(int playingColor) {
		this.playingColor = playingColor;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.deepHashCode(data);
		result = prime * result
				+ Objects.hash(cellToThreatMap, playingColor, threatTypeToThreatMap);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ThreatContext other = (ThreatContext) obj;
		return Objects.equals(cellToThreatMap, other.cellToThreatMap) && Arrays.deepEquals(data, other.data)
				&& playingColor == other.playingColor
				&& Objects.equals(threatTypeToThreatMap, other.threatTypeToThreatMap);
	}

	@Override
	public String toString() {
		return "ThreatContext [cellToThreatMap=" + cellToThreatMap + ", threatTypeToThreatMap=" + threatTypeToThreatMap
				+ ", data=" + Arrays.toString(data)
				+ ", playingColor=" + playingColor + "]";
	}
	
}