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

    public MoveDto(Cell cell, int color) {
    	this.color = color;
    	this.columnIndex = cell.getColumnIndex();
    	this.rowIndex = cell.getRowIndex();
    }
    
}