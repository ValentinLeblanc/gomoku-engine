package fr.leblanc.gomoku.engine.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EngineSettingsDto {

	private Boolean displayAnalysis;
	
	private Boolean strikeEnabled;
	
	private int minMaxDepth;
	
	private int strikeDepth;
	
	private int evaluationDepth;
	
}
