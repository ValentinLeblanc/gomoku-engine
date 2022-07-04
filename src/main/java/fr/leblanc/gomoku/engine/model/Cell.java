package fr.leblanc.gomoku.engine.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Cell {

	private int columnIndex;
	private int rowIndex;
	
}
