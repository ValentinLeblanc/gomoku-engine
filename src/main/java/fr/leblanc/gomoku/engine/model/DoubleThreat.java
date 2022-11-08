package fr.leblanc.gomoku.engine.model;

import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@RequiredArgsConstructor
@ToString
public class DoubleThreat extends Threat {

	private Cell targetCell;
	
	private Set<Cell> blockingCells = new HashSet<>();
	
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
}
