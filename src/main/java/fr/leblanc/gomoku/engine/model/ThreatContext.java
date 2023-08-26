package fr.leblanc.gomoku.engine.model;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThreatContext {

	private Map<Cell, Map<ThreatType, List<Threat>>> targetCellToThreatMap = new HashMap<>();
	private Map<ThreatType, List<Threat>> threatTypeToThreatMap = new EnumMap<>(ThreatType.class);
	private Map<Cell, List<Threat>> blockingCellsToThreatMap = new HashMap<>();
	private Map<Cell, List<Threat>> plainCellsToThreatMap = new HashMap<>();
	
	private List<Cell> dirtyAddedCells = new ArrayList<>();
	private List<Cell> dirtyRemovedCells = new ArrayList<>();
	
	private int[][] data;
	private int color;
	
	public ThreatContext(int[][] data, int color) {
		this.data = data;
		this.color = color;
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
	
	public int[][] getData() {
		return data;
	}
	
	public int getColor() {
		return color;
	}
	
	public List<Cell> getDirtyAddedCells() {
		return dirtyAddedCells;
	}
	
	public List<Cell> getDirtyRemovedCells() {
		return dirtyRemovedCells;
	}
	
	public Map<ThreatType, List<Threat>> getThreatsOfTargetCell(Cell cell) {
		return targetCellToThreatMap.computeIfAbsent(cell, k -> {
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

	public void addMove(Cell cell, int color) {
		data[cell.getColumn()][cell.getRow()] = color;
		dirtyAddedCells.add(cell);
		dirtyRemovedCells.remove(cell);
	}

	public boolean isDirty() {
		return !dirtyAddedCells.isEmpty() || !dirtyRemovedCells.isEmpty();
	}
	
	public void removeMove(Cell cell) {
		dirtyAddedCells.remove(cell);
		dirtyRemovedCells.add(cell);
		data[cell.getColumn()][cell.getRow()] = GomokuColor.NONE_COLOR;
	}

	public List<Threat> getBlockingCellThreats(Cell blockingCell) {
		return blockingCellsToThreatMap.computeIfAbsent(blockingCell, k -> new ArrayList<>());
	}
	
	public List<Threat> getPlainCellThreats(Cell blockingCell) {
		return plainCellsToThreatMap.computeIfAbsent(blockingCell, k -> new ArrayList<>());
	}
	
}