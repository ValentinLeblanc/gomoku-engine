package fr.leblanc.gomoku.engine.model.messaging;

import java.util.Set;

public record GameDTO(Long id, Integer boardSize, Set<MoveDTO> moves, Boolean strikeEnabled, Boolean minMaxEnabled,
		Boolean displayAnalysis, Integer minMaxExtent, Integer minMaxDepth, Integer strikeDepth, Integer strikeTimeout) {

	public GameDTO(int boardSize, Set<MoveDTO> moves) {
		this(null, boardSize, moves, null, null, null, null, null, null, null);
	}

}
