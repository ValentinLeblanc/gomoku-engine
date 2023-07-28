package fr.leblanc.gomoku.engine.model.messaging;

import java.util.Objects;

import fr.leblanc.gomoku.engine.model.Cell;

public class MoveDTO {

	private int columnIndex;
	private int rowIndex;
	private int color;
	private int number;
	
	public MoveDTO() {
		
	}
	
    public MoveDTO(Cell cell, int color) {
		this.color = color;
		this.columnIndex = cell.getColumn();
		this.rowIndex = cell.getRow();
	}

	public MoveDTO(int columnIndex, int rowIndex, int color) {
		this.color = color;
		this.columnIndex = columnIndex;
		this.rowIndex = rowIndex;
	}

	public int getColumnIndex() {
		return columnIndex;
	}

	public void setColumnIndex(int columnIndex) {
		this.columnIndex = columnIndex;
	}

	public int getRowIndex() {
		return rowIndex;
	}

	public void setRowIndex(int rowIndex) {
		this.rowIndex = rowIndex;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	@Override
	public int hashCode() {
		return Objects.hash(color, columnIndex, number, rowIndex);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MoveDTO other = (MoveDTO) obj;
		return color == other.color && columnIndex == other.columnIndex && number == other.number
				&& rowIndex == other.rowIndex;
	}

	@Override
	public String toString() {
		return "MoveDto [columnIndex=" + columnIndex + ", rowIndex=" + rowIndex + ", color=" + color + ", number="
				+ number + "]";
	}
    
}