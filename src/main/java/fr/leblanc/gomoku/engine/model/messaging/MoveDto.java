package fr.leblanc.gomoku.engine.model.messaging;

import fr.leblanc.gomoku.engine.model.Cell;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoveDto {

    private int columnIndex;
    private int rowIndex;
    private int color;
    private int number;

    public MoveDto(Cell cell, int color) {
    	this.color = color;
    	this.columnIndex = cell.getColumn();
    	this.rowIndex = cell.getRow();
    }
    
	public MoveDto(int columnIndex, int rowIndex, int color) {
		this.color = color;
		this.columnIndex = columnIndex;
		this.rowIndex = rowIndex;
	}
    
}