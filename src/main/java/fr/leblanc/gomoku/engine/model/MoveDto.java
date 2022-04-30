package fr.leblanc.gomoku.engine.model;

import lombok.Data;

@Data
public class MoveDto {

    private int number;
    private int columnIndex;
    private int rowIndex;
    private int color;

}
	