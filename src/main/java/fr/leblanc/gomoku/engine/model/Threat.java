package fr.leblanc.gomoku.engine.model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@ToString
public class Threat {

	public Set<Cell> getPlainCells() {
		return plainCells;
	}

	public void setPlainCells(Set<Cell> plainCells) {
		this.plainCells = plainCells;
	}

	public Set<Cell> getEmptyCells() {
		return emptyCells;
	}
	
	public void setEmptyCells(Set<Cell> emptyCells) {
		this.emptyCells = emptyCells;
	}

	public ThreatType getThreatType() {
		return threatType;
	}

	public void setThreatType(ThreatType threatType) {
		this.threatType = threatType;
	}

	private Set<Cell> plainCells = new HashSet<>();
	private Set<Cell> emptyCells = new HashSet<>();
	private ThreatType threatType;
	
	public Threat(Set<Cell> plainCells, Set<Cell> emptyCells, ThreatType threatType) {
		this.plainCells = plainCells;
		this.emptyCells = emptyCells;
		this.threatType = threatType;
	}
	
	public Set<Cell> getKillingCells() {
		return getBlockingCells(null);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(emptyCells, plainCells, threatType);
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
		
		if (plainCells.size() != other.plainCells.size()) {
			return false;
		}
		
		if (!plainCells.containsAll(other.plainCells)) {
			return false;
		}
		
		if (emptyCells.size() != other.emptyCells.size()) {
			return false;
		}
		
		if (!emptyCells.containsAll(other.emptyCells)) {
			return false;
		}
		
		return true;
	}

	public Set<Cell> getBlockingCells(Cell playingCell) {
		return emptyCells.stream().filter(c -> !c.equals(playingCell)).collect(Collectors.toSet());
	}
	
}
