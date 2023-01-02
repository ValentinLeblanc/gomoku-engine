package fr.leblanc.gomoku.engine.model.messaging;

import java.util.HashSet;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameDto {

	private int boardSize;
	
	private Set<MoveDto> moves = new HashSet<>();
	
	private EngineSettingsDto settings;
	
}