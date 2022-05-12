package fr.leblanc.gomoku.engine.model;

import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CheckWinResult {

	public CheckWinResult(boolean isWin) {
		this.isWin = isWin;
	}

	private boolean isWin = false;

	private Set<MoveDto> winMoves = new HashSet<>();
}
