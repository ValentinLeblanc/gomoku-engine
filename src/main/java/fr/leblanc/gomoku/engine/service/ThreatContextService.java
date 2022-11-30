package fr.leblanc.gomoku.engine.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.DataWrapper;
import fr.leblanc.gomoku.engine.model.Threat;
import fr.leblanc.gomoku.engine.model.ThreatContext;
import fr.leblanc.gomoku.engine.model.ThreatType;

public interface ThreatContextService {

	ThreatContext computeThreatContext(int[][] data, int playingColor);
	
	Set<Cell> findCombinedThreats(ThreatContext threatContext, ThreatType threatType1, ThreatType threatType2);

	List<Cell> buildAnalyzedMoves(DataWrapper dataWrapper, int color);

	Map<Threat, Integer> getEffectiveThreats(ThreatContext playingThreatContext, ThreatContext opponentThreatContext,
			ThreatType threatType, ThreatType secondThreatType);

}