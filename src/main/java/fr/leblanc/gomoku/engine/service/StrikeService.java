package fr.leblanc.gomoku.engine.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.DataWrapper;
import fr.leblanc.gomoku.engine.model.Threat;
import fr.leblanc.gomoku.engine.model.ThreatContext;
import fr.leblanc.gomoku.engine.model.ThreatType;
import lombok.extern.log4j.Log4j2;


@Service
@Log4j2
public class StrikeService {

	private static final int MAX_DEPTH = 3;

	@Autowired
	private ThreatContextService threatContextService;
	
	@Autowired
	private MinMaxService minMaxService;
	
	public Cell findOrCounterStrike(DataWrapper dataWrapper, int playingColor) {
		
		StopWatch stopWatch = new StopWatch("findOrCounterStrike");
		stopWatch.start();
		
		Cell directThreat = directStrike(dataWrapper, playingColor);

		if (directThreat != null) {
			
			stopWatch.stop();
			
			if (log.isDebugEnabled()) {
				log.debug("direct strike found in " + stopWatch.getTotalTimeMillis() + " ms");
			}
			
			return directThreat;
		}

		Cell opponentDirectThreat = directStrike(dataWrapper, -playingColor);

		if (opponentDirectThreat != null) {
			Set<Cell> counterOpponentThreats = counterDirectStrikeMoves(dataWrapper, playingColor);

			if (!counterOpponentThreats.isEmpty()) {
				return minMaxService.computeMinMax(dataWrapper, playingColor, counterOpponentThreats);
			}
		}
		
		for (int maxDepth = 1; maxDepth <= MAX_DEPTH; maxDepth++) {
			
			long timeElapsed = System.currentTimeMillis();	
			
			Cell secondaryStrike = secondaryStrike(dataWrapper, playingColor, 0, maxDepth);
			
			if (secondaryStrike != null) {
				
				stopWatch.stop();
				
				if (log.isDebugEnabled()) {
					log.debug("secondary strike found in " + stopWatch.getTotalTimeMillis() + " ms for maxDepth = " + maxDepth);
				}
				
				return secondaryStrike;
			}
			
			if (log.isDebugEnabled()) {
				timeElapsed = System.currentTimeMillis() - timeElapsed;
				log.debug("secondary strike failed for maxDepth = " + maxDepth + " (" + timeElapsed +" ms)");
			}
			
		}
		
		return null;
	}
	
	private Cell directStrike(DataWrapper dataWrapper, int playingColor) {

		Map<ThreatType, Set<Cell>> threatMap = threatContextService.computeThreatContext(dataWrapper.getData(), playingColor).getThreatToCellMap();

		// check for a threat5
		if (!threatMap.get(ThreatType.THREAT_5).isEmpty()) {
			return threatMap.get(ThreatType.THREAT_5).iterator().next();
		}

		Map<ThreatType, Set<Cell>> opponentThreatMap = threatContextService.computeThreatContext(dataWrapper.getData(), -playingColor).getThreatToCellMap();

		// check for an opponent threat5
		if (!opponentThreatMap.get(ThreatType.THREAT_5).isEmpty()) {

			Cell opponentThreat = opponentThreatMap.get(ThreatType.THREAT_5).iterator().next();

			// defend
			dataWrapper.addMove(opponentThreat, playingColor);

			// check for another threat5
			threatMap = threatContextService.computeThreatContext(dataWrapper.getData(), playingColor)
					.getThreatToCellMap();
			if (!threatMap.get(ThreatType.THREAT_5).isEmpty()) {

				Cell newThreat = threatMap.get(ThreatType.THREAT_5).iterator().next();

				// opponent defends
				dataWrapper.addMove(newThreat, -playingColor);

				// check for another strike
				Cell nextThreat = directStrike(dataWrapper, playingColor);

				dataWrapper.removeMove(newThreat);

				if (nextThreat != null) {
					dataWrapper.removeMove(opponentThreat);
					return opponentThreat;
				}
			}

			dataWrapper.removeMove(opponentThreat);

			return null;
		}

		// check for a double threat4 move
		if (!threatMap.get(ThreatType.DOUBLE_THREAT_4).isEmpty()) {
			return threatMap.get(ThreatType.DOUBLE_THREAT_4).iterator().next();
		}

		// check for threat4 moves
		Set<Cell> threat4List = threatMap.get(ThreatType.THREAT_4);

		for (Cell threat4Move : threat4List) {

			dataWrapper.addMove(threat4Move, playingColor);

			// opponent defends
			opponentThreatMap = threatContextService.computeThreatContext(dataWrapper.getData(), playingColor).getThreatToCellMap();

			Cell counterMove = opponentThreatMap.get(ThreatType.THREAT_5).iterator().next();

			dataWrapper.addMove(counterMove, -playingColor);

			Cell nextAttempt = directStrike(dataWrapper, playingColor);

			dataWrapper.removeMove(counterMove);
			dataWrapper.removeMove(threat4Move);

			if (nextAttempt != null) {
				return threat4Move;
			}
		}

		return null;
	}
	
	private Set<Cell> counterDirectStrikeMoves(DataWrapper dataWrapper, int playingColor) {
	
		Set<Cell> defendingMoves = new HashSet<>();
		
		ThreatContext opponentThreatContext = threatContextService.computeThreatContext(dataWrapper.getData(), -playingColor);
		
		Map<ThreatType, Set<Cell>> opponentThreatMap = opponentThreatContext.getThreatToCellMap();
		
		if (!opponentThreatMap.get(ThreatType.THREAT_5).isEmpty()) {
			defendingMoves.add(opponentThreatMap.get(ThreatType.THREAT_5).iterator().next());
			return defendingMoves;
		}
		
		if (!opponentThreatMap.get(ThreatType.DOUBLE_THREAT_4).isEmpty()) {
			Map<Cell, Map<ThreatType, List<Threat>>> opponentCellThreatMap = opponentThreatContext.getCellToThreatMap();
			
			for (Cell doubleThreat4 : opponentThreatMap.get(ThreatType.DOUBLE_THREAT_4)) {
				for (Threat threat4 : opponentCellThreatMap.get(doubleThreat4).get(ThreatType.DOUBLE_THREAT_4)) {
					for (Cell emptyCell : threat4.getEmptyCells()) {
						dataWrapper.addMove(emptyCell, playingColor);
						if (directStrike(dataWrapper, -playingColor) == null) {
							defendingMoves.add(emptyCell);
						}
						dataWrapper.removeMove(emptyCell);
					}
				}
			}
			return defendingMoves;
		}
		
		Set<Cell> combinedThreats = threatContextService.findCombinedThreats(opponentThreatContext, ThreatType.THREAT_4, ThreatType.DOUBLE_THREAT_3);
		
		if (!combinedThreats.isEmpty()) {
			for (Cell combinedThreat : combinedThreats) {
				Map<Cell, Map<ThreatType, List<Threat>>> opponentCellThreatMap = opponentThreatContext.getCellToThreatMap();
				for (Threat threat4 : opponentCellThreatMap.get(combinedThreat).get(ThreatType.THREAT_4)) {
					defendingMoves.addAll(threat4.getEmptyCells());
				}
				for (Threat doubleThreat3 : opponentCellThreatMap.get(combinedThreat).get(ThreatType.DOUBLE_THREAT_3)) {
					defendingMoves.addAll(doubleThreat3.getEmptyCells());
				}
			}
			return defendingMoves;
		}
		
		return defendingMoves;
	}

	private Cell secondaryStrike(DataWrapper dataWrapper, int playingColor, int depth, final int maxDepth) {

		// check for a strike
		Cell directStrike = directStrike(dataWrapper, playingColor);

		if (directStrike != null) {
			return directStrike;
		}
		
		// check for an opponent strike
		Cell opponentDirectStrike = directStrike(dataWrapper, -playingColor);

		if (opponentDirectStrike != null) {
			Set<Cell> defendFromStrikes = counterDirectStrikeMoves(dataWrapper, playingColor);

			// defend
			for (Cell defendFromStrike : defendFromStrikes) {
				
				dataWrapper.addMove(defendFromStrike, playingColor);

				// check for a new strike
				Cell newStrike = directStrike(dataWrapper, playingColor);

				if (newStrike != null) {
					Set<Cell> opponentDefendFromStrikes = counterDirectStrikeMoves(dataWrapper, -playingColor);

					boolean hasDefense = false;
					
					for (Cell opponentDefendFromStrike : opponentDefendFromStrikes) {
						
						dataWrapper.addMove(opponentDefendFromStrike, -playingColor);
						
						Cell newAttempt = secondaryStrike(dataWrapper, playingColor, depth + 1, maxDepth);
						
						dataWrapper.removeMove(opponentDefendFromStrike);
						
						if (newAttempt == null) {
							hasDefense = true;
							break;
						}
					}
					
					if (!hasDefense) {
						dataWrapper.removeMove(defendFromStrike);
						return defendFromStrike;
					}
					
				}
				
				dataWrapper.removeMove(defendFromStrike);
			}

			return null;
		}
		
		if (depth == maxDepth) {
			return null;
		}
		
		ThreatContext threatContext = threatContextService.computeThreatContext(dataWrapper.getData(), playingColor);

		Cell retry = retrySecondary(dataWrapper, playingColor, threatContextService.findCombinedThreats(threatContext, ThreatType.DOUBLE_THREAT_3, ThreatType.DOUBLE_THREAT_3), depth, maxDepth);
		
		if (retry != null) {
			return retry;
		}
		
		retry = retrySecondary(dataWrapper, playingColor, threatContextService.findCombinedThreats(threatContext, ThreatType.DOUBLE_THREAT_3, ThreatType.THREAT_3), depth, maxDepth);
		
		if (retry != null) {
			return retry;
		}
		
		
		retry = retrySecondary(dataWrapper, playingColor, threatContext.getThreatToCellMap().get(ThreatType.DOUBLE_THREAT_3), depth, maxDepth);
		
		if (retry != null) {
			return retry;
		}
		
		return null;
	}
	
	private Cell retrySecondary(DataWrapper dataWrapper, int playingColor, Set<Cell> cellsToTry, int depth, int maxDepth) {
		
		for (Cell threat : cellsToTry) {
			dataWrapper.addMove(threat, playingColor);
			
			Set<Cell> opponentDefendFromStrikes = counterDirectStrikeMoves(dataWrapper, -playingColor);
			
			boolean hasDefense = false;
			
			for (Cell opponentDefendFromStrike : opponentDefendFromStrikes) {
				dataWrapper.addMove(opponentDefendFromStrike, -playingColor);
				
				Cell newAttempt = secondaryStrike(dataWrapper, playingColor, depth + 1, maxDepth);
				
				dataWrapper.removeMove(opponentDefendFromStrike);
				
				if (newAttempt == null) {
					hasDefense = true;
					break;
				}
			}
			
			dataWrapper.removeMove(threat);
			
			if (!hasDefense) {
				return threat;
			}
		}
		
		return null;
		
	}

	
}
