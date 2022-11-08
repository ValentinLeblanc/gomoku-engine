package fr.leblanc.gomoku.engine.service;

import java.util.Set;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.ThreatContext;
import fr.leblanc.gomoku.engine.model.ThreatType;

public interface ThreatContextService {

	ThreatContext computeThreatContext(int[][] data, int playingColor);
	
	Set<Cell> findCombinedThreats(ThreatContext threatContext, ThreatType threatType1, ThreatType threatType2);

}