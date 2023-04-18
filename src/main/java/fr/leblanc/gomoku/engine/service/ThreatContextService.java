package fr.leblanc.gomoku.engine.service;

import java.util.List;
import java.util.Set;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.CompoThreatType;
import fr.leblanc.gomoku.engine.model.DataWrapper;
import fr.leblanc.gomoku.engine.model.Threat;
import fr.leblanc.gomoku.engine.model.ThreatContext;
import fr.leblanc.gomoku.engine.model.ThreatType;
import fr.leblanc.gomoku.engine.util.Pair;

public interface ThreatContextService {

	ThreatContext computeThreatContext(DataWrapper dataWrapper, int playingColor);
	
	Set<Cell> findCombinedThreats(ThreatContext threatContext, ThreatType threatType1, ThreatType threatType2);

	List<Cell> buildAnalyzedCells(DataWrapper dataWrapper, int color);

	List<Pair<Threat, Threat>> findCompositeThreats(ThreatContext context, CompoThreatType threatTryContext);

}