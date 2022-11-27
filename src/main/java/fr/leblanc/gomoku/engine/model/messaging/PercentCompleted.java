package fr.leblanc.gomoku.engine.model.messaging;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PercentCompleted {

	private int index;
	private int percent;
	
}
