package fr.leblanc.gomoku.engine.model;

import java.util.Arrays;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DataWrapper {

	private int[][] data;
	
	public DataWrapper(int[][] data) {
		this.data = data;
	}
	
	public DataWrapper(DataWrapper dataWrapper) {
		data = new int[dataWrapper.getData().length][dataWrapper.getData().length];
		for (int rowIndex = 0; rowIndex < dataWrapper.getData().length; rowIndex++) {
			for (int columnIndex = 0; columnIndex < dataWrapper.getData().length; columnIndex++) {
				data[columnIndex][rowIndex] = dataWrapper.getData()[columnIndex][rowIndex];
			}
		}
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
	}
	
	public void removeMove(Cell cell) {
		data[cell.getColumnIndex()][cell.getRowIndex()] = 0;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.deepHashCode(data);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DataWrapper other = (DataWrapper) obj;
		return Arrays.deepEquals(data, other.data);
	}
	
}
