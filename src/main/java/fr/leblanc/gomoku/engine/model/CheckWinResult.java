package fr.leblanc.gomoku.engine.model;

import java.util.HashSet;
import java.util.Set;

import fr.leblanc.gomoku.engine.model.messaging.MoveDto;

public class CheckWinResult {

	private int color;
	
	public void setColor(int color) {
		this.color = color;
	}
	
	public int getColor() {
		return color;
	}
	
	public Set<MoveDto> getWinMoves() {
		return winMoves;
	}

	public void setWinMoves(Set<MoveDto> winMoves) {
		this.winMoves = winMoves;
	}

	public CheckWinResult(int color) {
		this.color = color;
	}

	private Set<MoveDto> winMoves = new HashSet<>();
}
