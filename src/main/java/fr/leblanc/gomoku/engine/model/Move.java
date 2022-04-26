package fr.leblanc.gomoku.engine.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Move {

	private int number;
	
	private int columnIndex;
	
	private int rowIndex;
	
	private GomokuColor color;

}
