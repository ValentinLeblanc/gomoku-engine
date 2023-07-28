package fr.leblanc.gomoku.engine.model.messaging;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class GameDto {

	private int boardSize;
	
	private Set<MoveDto> moves = new HashSet<>();
	
	private EngineSettingsDto settings;
	
	public GameDto() {
		
	}
	
	public GameDto(int boardSize, Set<MoveDto> moves, EngineSettingsDto settings) {
		super();
		this.boardSize = boardSize;
		this.moves = moves;
		this.settings = settings;
	}

	public int getBoardSize() {
		return boardSize;
	}

	public void setBoardSize(int boardSize) {
		this.boardSize = boardSize;
	}

	public Set<MoveDto> getMoves() {
		return moves;
	}

	public void setMoves(Set<MoveDto> moves) {
		this.moves = moves;
	}

	public EngineSettingsDto getSettings() {
		return settings;
	}

	public void setSettings(EngineSettingsDto settings) {
		this.settings = settings;
	}

	@Override
	public int hashCode() {
		return Objects.hash(boardSize, moves, settings);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GameDto other = (GameDto) obj;
		return boardSize == other.boardSize && Objects.equals(moves, other.moves)
				&& Objects.equals(settings, other.settings);
	}

	@Override
	public String toString() {
		return "GameDto [boardSize=" + boardSize + ", moves=" + moves + ", settings=" + settings + "]";
	}
	
}
