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
	
	public DataWrapper(int boardSize) {
		data = new int[boardSize][boardSize];

		for (int rowIndex = 0; rowIndex < boardSize; rowIndex++) {
			for (int columnIndex = 0; columnIndex < boardSize; columnIndex++) {
				data[columnIndex][rowIndex] = EngineConstants.NONE_COLOR;
			}
		}
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
	
	public static DataWrapper of(GameDto game) {

		int boardSize = game.getBoardSize();

		int[][] data = new int[boardSize][boardSize];

		for (int rowIndex = 0; rowIndex < boardSize; rowIndex++) {
			for (int columnIndex = 0; columnIndex < boardSize; columnIndex++) {
				data[columnIndex][rowIndex] = EngineConstants.NONE_COLOR;
			}
		}

		for (MoveDto move : game.getMoves()) {
			data[move.getColumnIndex()][move.getRowIndex()] = move.getColor();
		}

		return new DataWrapper(data);
	}
}
