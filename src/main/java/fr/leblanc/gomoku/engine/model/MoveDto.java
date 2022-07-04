package fr.leblanc.gomoku.engine.model;

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

}