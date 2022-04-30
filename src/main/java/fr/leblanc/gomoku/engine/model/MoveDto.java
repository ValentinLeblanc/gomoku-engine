package fr.leblanc.gomoku.engine.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class MoveDto {

    private int columnIndex;
    private int rowIndex;
    private int color;

}