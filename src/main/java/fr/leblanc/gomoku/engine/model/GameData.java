package fr.leblanc.gomoku.engine.model;

import fr.leblanc.gomoku.engine.model.messaging.GameDTO;
import fr.leblanc.gomoku.engine.model.messaging.MoveDTO;

public class GameData {

	private int[][] data;
	
	public GameData(int[][] data) {
		this.data = data;
	}
	
	public GameData(GameData gameData) {
		data = new int[gameData.getData().length][gameData.getData().length];
		for (int rowIndex = 0; rowIndex < gameData.getData().length; rowIndex++) {
			for (int columnIndex = 0; columnIndex < gameData.getData().length; columnIndex++) {
				data[columnIndex][rowIndex] = gameData.getData()[columnIndex][rowIndex];
			}
		}
	}
	
	public GameData(int boardSize) {
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
	
	public static GameData of(GameDTO game) {

		int boardSize = game.getBoardSize();

		int[][] data = new int[boardSize][boardSize];

		for (int rowIndex = 0; rowIndex < boardSize; rowIndex++) {
			for (int columnIndex = 0; columnIndex < boardSize; columnIndex++) {
				data[columnIndex][rowIndex] = EngineConstants.NONE_COLOR;
			}
		}

		for (MoveDTO move : game.getMoves()) {
			data[move.getColumnIndex()][move.getRowIndex()] = move.getColor();
		}

		GameData gameData = new GameData(data);
		
		return gameData;
	}
	
	public static int extractPlayingColor(GameData dataWrapper) {
		
		int[][] data = dataWrapper.getData();
		
		int moveCount = 0;
		
		for (int i = 0; i < data[0].length; i++) {
			for (int j = 0; j < data.length; j++) {
				if (data[j][i] != EngineConstants.NONE_COLOR) {
					moveCount++;
				}
			}
		}
		
		return moveCount % 2 == 0 ? EngineConstants.BLACK_COLOR : EngineConstants.WHITE_COLOR;
	}
	
	public static int countEmptyCells(GameData dataWrapper) {
		int emptyCellsCount = 0;
		
		for (int i = 0; i < dataWrapper.getData().length; i++) {
			for (int j = 0; j < dataWrapper.getData().length; j++) {
				if (dataWrapper.getData()[i][j] == EngineConstants.NONE_COLOR) {
					emptyCellsCount++;
				}
			}
		}
		return emptyCellsCount;
	}
	
	public static int countPlainCells(GameData dataWrapper) {
		int plainCellsCount = 0;
		
		for (int i = 0; i < dataWrapper.getData().length; i++) {
			for (int j = 0; j < dataWrapper.getData().length; j++) {
				if (dataWrapper.getData()[i][j] != EngineConstants.NONE_COLOR) {
					plainCellsCount++;
				}
			}
		}
		return plainCellsCount;
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
		GameData other = (GameData) obj;
		
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
