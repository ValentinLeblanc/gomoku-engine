package fr.leblanc.gomoku.engine.model;

import java.util.HashSet;
import java.util.Set;

import lombok.Data;

@Data
public class CheckWinResultDto {

	private boolean isWin = false;

	private Set<MoveDto> winMoves = new HashSet<>();
}
