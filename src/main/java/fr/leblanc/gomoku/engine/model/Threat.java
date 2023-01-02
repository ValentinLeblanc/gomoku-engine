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
public class Threat {

	private Set<Cell> plainCells = new HashSet<>();
	private Set<Cell> emptyCells = new HashSet<>();
	private ThreatType threatType;
	
	public Threat(Set<Cell> plainCells, Set<Cell> emptyCells, ThreatType threatType) {
		this.plainCells = plainCells;
		this.emptyCells = emptyCells;
		this.threatType = threatType;
	}
	
	public Set<Cell> getKillingCells() {
		return emptyCells;
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if (!(obj instanceof Threat)) {
			return false;
		}
		
		Threat other = (Threat) obj;
		
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

}
