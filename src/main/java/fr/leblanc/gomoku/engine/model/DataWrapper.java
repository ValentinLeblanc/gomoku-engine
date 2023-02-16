package fr.leblanc.gomoku.engine.model;

import fr.leblanc.gomoku.engine.model.messaging.GameDto;
import fr.leblanc.gomoku.engine.model.messaging.MoveDto;

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
		data[cell.getColumn()][cell.getRow()] = value;
	}
	
	public void removeMove(Cell cell) {
		data[cell.getColumn()][cell.getRow()] = 0;
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

	public int[][] getData() {
		return data;
	}

	public void setData(int[][] data) {
		this.data = data;
	}

	@Override
	public int hashCode() {
		return hashCode(data);
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
		
		if (data.length != other.data.length) {
			return false;
		}
		
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data.length; j++) {
				if (data[i][j] != other.data[i][j]) {
					return false;
				}
			}
		}
		
		return true;
	}

	private int hashCode(int[][] data) {
		final int prime = 31;
		int result = 1;
		
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data.length; j++) {
				int value = data[i][j];
				result = prime * result + i;
				result = prime * result + j;
				result = prime * result + value;
			}
		}
		
		return result;
	}
	
}
