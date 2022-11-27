package fr.leblanc.gomoku.engine.model.messaging;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EngineSettingsDto {

	private boolean strikeEnabled;
	
	private int minMaxDepth;
	
	private int strikeDepth;
	
	private int evaluationDepth;
	
}
