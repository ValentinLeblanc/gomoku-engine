package fr.leblanc.gomoku.engine.model.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EngineSettingsDto {

	@Default
	private boolean strikeEnabled = true;
	
	@Default
	private boolean minMaxEnabled = true;
	
	@Default
	private int minMaxAnalysisExtent = -1;
	
	private int minMaxDepth;
	
	private int strikeDepth;
	
	
	private int strikeTimeout;
	
}
