package fr.leblanc.gomoku.engine.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class Threat {

	private ThreatType threatType;
	private Cell targetCell;
	private Set<Cell> plainCells;
	private Set<Cell> blockingCells;
	private List<Cell> killingCells;
	
	public Threat(Cell targetCell, Set<Cell> plainCells, Set<Cell> blockingCells, ThreatType threatType) {
		this.targetCell = targetCell;
		this.plainCells = plainCells;
		this.blockingCells = blockingCells;
		this.threatType = threatType;
	}
	
	public Cell getTargetCell() {
		return targetCell;
	}
	
	public Set<Cell> getPlainCells() {
		return plainCells;
	}

	public void setPlainCells(Set<Cell> plainCells) {
		this.plainCells = plainCells;
	}

	public ThreatType getThreatType() {
		return threatType;
	}

	public void setThreatType(ThreatType threatType) {
		this.threatType = threatType;
	}

	public List<Cell> getKillingCells() {
		if (killingCells == null) {
			this.killingCells = new ArrayList<>();
			killingCells.add(targetCell);
			killingCells.addAll(blockingCells);
		}
		return killingCells;
	}
	
	public Set<Cell> getBlockingCells() {
		return blockingCells;
	}

	@Override
	public boolean equals(Object obj) {
		
		if (!(obj instanceof Threat)) {
			return false;
		}
		
		Threat other = (Threat) obj;
		
		if (!threatType.equals(other.threatType)) {
			return false;
		}
		
		if (!targetCell.equals(other.targetCell)) {
			return false;
		}
		
		if (plainCells.size() != other.plainCells.size()) {
			return false;
		}
		
		if (!plainCells.containsAll(other.plainCells)) {
			return false;
		}
		
		if (blockingCells.size() != other.blockingCells.size()) {
			return false;
		}
		
		if (!blockingCells.containsAll(other.blockingCells)) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(threatType, targetCell, plainCells, blockingCells);
	}

	@Override
	public String toString() {
		return "Threat [threatType=" + threatType + ", targetCell=" + targetCell + ", plainCells=" + plainCells
				+ ", blockingCells=" + blockingCells;
	}
	
}
