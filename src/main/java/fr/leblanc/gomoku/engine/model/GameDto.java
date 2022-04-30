package fr.leblanc.gomoku.engine.model;

import java.util.HashSet;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GameDto {

	private int boardSize;
	
	private Set<MoveDto> moves = new HashSet<>();
	
}
