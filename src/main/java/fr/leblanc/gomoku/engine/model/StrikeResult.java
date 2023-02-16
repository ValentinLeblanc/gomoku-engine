package fr.leblanc.gomoku.engine.model;

import java.util.Objects;

public class StrikeResult {

	public StrikeResult(Cell resultCell, StrikeType strikeType) {
		this.resultCell = resultCell;
		this.strikeType = strikeType;
	}

	private Cell resultCell;
	
	private StrikeType strikeType;

	public StrikeType getStrikeType() {
		return strikeType;
	}

	public void setStrikeType(StrikeType strikeType) {
		this.strikeType = strikeType;
	}

	public Cell getResultCell() {
		return resultCell;
	}

	public void setResultCell(Cell resultCell) {
		this.resultCell = resultCell;
	}
	
	public boolean hasResult() {
		return resultCell != null;
	}
	
	public enum StrikeType {
		DIRECT_STRIKE,
		SECONDARY_STRIKE,
		DEFEND_STRIKE,
		EMPTY_STRIKE
	}

	@Override
	public int hashCode() {
		return Objects.hash(resultCell, strikeType);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StrikeResult other = (StrikeResult) obj;
		return Objects.equals(resultCell, other.resultCell) && strikeType == other.strikeType;
	}

	@Override
	public String toString() {
		return "StrikeResult [resultCell=" + resultCell + ", strikeType=" + strikeType + "]";
	}
	
}
