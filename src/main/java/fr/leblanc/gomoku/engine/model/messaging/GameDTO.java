package fr.leblanc.gomoku.engine.model.messaging;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class GameDTO {

	private Long id = 0l;
	
	private int boardSize;
	
	private Set<MoveDTO> moves = new HashSet<>();
	
	private GameSettings settings;
	
	public GameDTO() {
		
	}
	
	public GameDTO(Long id, int boardSize, Set<MoveDTO> moves, GameSettings settings) {
		super();
		this.id = id;
		this.boardSize = boardSize;
		this.moves = moves;
		this.settings = settings;
	}
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}

	public int getBoardSize() {
		return boardSize;
	}

	public void setBoardSize(int boardSize) {
		this.boardSize = boardSize;
	}

	public Set<MoveDTO> getMoves() {
		return moves;
	}

	public void setMoves(Set<MoveDTO> moves) {
		this.moves = moves;
	}

	public GameSettings getSettings() {
		return settings;
	}

	public void setSettings(GameSettings settings) {
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
		GameDTO other = (GameDTO) obj;
		return boardSize == other.boardSize && Objects.equals(moves, other.moves)
				&& Objects.equals(settings, other.settings);
	}

	@Override
	public String toString() {
		return "GameDto [boardSize=" + boardSize + ", moves=" + moves + ", settings=" + settings + "]";
	}
	
}
