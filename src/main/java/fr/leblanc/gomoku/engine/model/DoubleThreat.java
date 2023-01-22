package fr.leblanc.gomoku.engine.model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@ToString
public class DoubleThreat extends Threat {

	private Cell targetCell;
	
	private Set<Cell> blockingCells = new HashSet<>();
	
	private Set<Cell> killingCells = new HashSet<>();
	
	@Override
	public boolean equals(Object obj) {
		
		if (!(obj instanceof DoubleThreat)) {
			return false;
		}
		
		DoubleThreat other = (DoubleThreat) obj;
		
		if (blockingCells.size() != other.blockingCells.size()) {
			return false;
		}
		
		if (!blockingCells.containsAll(other.blockingCells)) {
			return false;
		}
		
		if (!targetCell.equals(other.targetCell)) {
			return false;
		}
		
		return super.equals(obj);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), targetCell, blockingCells, killingCells);
	}

	public Cell getTargetCell() {
		return targetCell;
	}

	public void setTargetCell(Cell targetCell) {
		this.targetCell = targetCell;
	}

	@Override
	public Set<Cell> getBlockingCells(Cell playingThreat) {
		return blockingCells;
	}

	public void setBlockingCells(Set<Cell> blockingCells) {
		this.blockingCells = blockingCells;
	}

	@Override
	public Set<Cell> getKillingCells() {
		return killingCells;
	}

	public void setKillingCells(Set<Cell> killingCells) {
		this.killingCells = killingCells;
	}

}
