package fr.leblanc.gomoku.engine.model;

import java.util.HashSet;
import java.util.Set;

import fr.leblanc.gomoku.engine.model.messaging.MoveDTO;

public class CheckWinResult {

	private int color;
	
	public void setColor(int color) {
		this.color = color;
	}
	
	public int getColor() {
		return color;
	}
	
	public Set<MoveDTO> getWinMoves() {
		return winMoves;
	}

	public void setWinMoves(Set<MoveDTO> winMoves) {
		this.winMoves = winMoves;
	}

	public CheckWinResult(int color) {
		this.color = color;
	}
	
	public boolean isWin() {
		return color != EngineConstants.NONE_COLOR;
	}

	private Set<MoveDTO> winMoves = new HashSet<>();

	public CheckWinResult build(int[][] win, int color) {
		this.color = color;
		for (int i = 0; i < win.length; i++) {
			getWinMoves().add(new MoveDTO(win[i][0], win[i][1], color));
		}
		return this;
	}
}
