package fr.leblanc.gomoku.engine.model;

import java.util.HashSet;
import java.util.Set;

import lombok.Data;

@Data
public class Game {

	private int boardSize;
	
	private Set<Move> moves = new HashSet<Move>();

}
