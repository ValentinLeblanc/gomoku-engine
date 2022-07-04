package fr.leblanc.gomoku.engine.model;

import java.util.Deque;
import java.util.LinkedList;

import lombok.Data;

@Data
public class DataWrapper {

	private int[][] data;
	
	private Deque<Cell> analysis = new LinkedList<>();
	
	public DataWrapper(int[][] data) {
		this.data = data;
	}
	
	public void addMove(Cell cell, int value) {
		data[cell.getColumnIndex()][cell.getRowIndex()] = value;
		analysis.push(cell);
	}
	
	public void removeMove(Cell cell) {
		data[cell.getColumnIndex()][cell.getRowIndex()] = 0;
		analysis.pop();
	}
	
	public int getValue(int columnIndex, int rowIndex) {
		return data[columnIndex][rowIndex];
	}
	
}
