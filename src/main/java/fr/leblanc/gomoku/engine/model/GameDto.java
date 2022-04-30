package fr.leblanc.gomoku.engine.model;

import java.util.HashSet;
import java.util.Set;

import lombok.Data;

@Data
public class GameDto {

	private int boardSize;
	
	private Set<MoveDto> moves = new HashSet<>();
	
}
