package fr.leblanc.gomoku.engine.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EngineSettings {

	public EngineSettings(EngineSettingsDto settings) {
		this.isDisplayAnalysis = settings.getDisplayAnalysis();
		this.isStrikeEnabled = settings.getStrikeEnabled();
		this.minMaxDepth = settings.getMinMaxDepth();
		this.strikeDepth = settings.getStrikeDepth();
		this.evaluationDepth = settings.getEvaluationDepth();
	}

	private boolean isDisplayAnalysis;	
	
	private boolean isStrikeEnabled;
	
	private int minMaxDepth;
	
	private int strikeDepth;
	
	private int evaluationDepth;

}
