package fr.leblanc.gomoku.engine.model;

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
}
