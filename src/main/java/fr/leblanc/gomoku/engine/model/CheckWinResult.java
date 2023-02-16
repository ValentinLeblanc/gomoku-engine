package fr.leblanc.gomoku.engine.model;

import java.util.HashSet;
import java.util.Set;

import fr.leblanc.gomoku.engine.model.messaging.MoveDto;

public class CheckWinResult {

	public boolean isWin() {
		return isWin;
	}

	public void setWin(boolean isWin) {
		this.isWin = isWin;
	}

	public Set<MoveDto> getWinMoves() {
		return winMoves;
	}

	public void setWinMoves(Set<MoveDto> winMoves) {
		this.winMoves = winMoves;
	}

	public CheckWinResult(boolean isWin) {
		this.isWin = isWin;
	}
	
	public CheckWinResult() {
	}

	private boolean isWin = false;

	private Set<MoveDto> winMoves = new HashSet<>();
}
