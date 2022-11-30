package fr.leblanc.gomoku.engine.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EvaluationContext {
	
	private final DataWrapper dataWrapper;

	private int playingColor;
	
	private final int maxDepth;
	
	private int depth;
	
	public void reversePlayingColor() {
		playingColor = -playingColor;
	}
	
	public void increaseDepth() {
		this.depth++;
	}
	
	public void decreaseDepth() {
		this.depth--;
	}
	
}
