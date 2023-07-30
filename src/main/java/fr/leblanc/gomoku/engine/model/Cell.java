package fr.leblanc.gomoku.engine.model;

import java.util.Objects;

public class Cell {
	
	private int column;
	private int row;

	public Cell(int column, int row) {
		super();
		this.column = column;
		this.row = row;
	}
	
	public int getColumn() {
		return column;
	}
	public void setColumn(int column) {
		this.column = column;
	}
	public int getRow() {
		return row;
	}
	public void setRow(int row) {
		this.row = row;
	}

	@Override
	public int hashCode() {
		return Objects.hash(column, row);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Cell other = (Cell) obj;
		return column == other.column && row == other.row;
	}

	@Override
	public String toString() {
		return "Cell [column=" + column + ", row=" + row + "]";
	}
	
}
