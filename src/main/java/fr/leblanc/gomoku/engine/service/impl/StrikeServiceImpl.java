package fr.leblanc.gomoku.engine.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.CustomProperties;
import fr.leblanc.gomoku.engine.model.DataWrapper;
import fr.leblanc.gomoku.engine.model.DoubleThreat;
import fr.leblanc.gomoku.engine.model.EngineConstants;
import fr.leblanc.gomoku.engine.model.Threat;
import fr.leblanc.gomoku.engine.model.ThreatContext;
import fr.leblanc.gomoku.engine.model.ThreatType;
import fr.leblanc.gomoku.engine.service.AnalysisService;
import fr.leblanc.gomoku.engine.service.MinMaxService;
import fr.leblanc.gomoku.engine.service.StrikeService;
import lombok.extern.log4j.Log4j2;


@Service
@Log4j2
public class StrikeServiceImpl implements StrikeService {

	private static final int STRIKE_MAX_DEPTH = 3;

	@Autowired
	private ThreatContextServiceImpl threatContextService;
	
	@Autowired
	private MinMaxService minMaxService;
	
	@Autowired
	private CustomProperties customProperties;
	
	@Autowired
	private AnalysisService analysisService;
	
	private Boolean stopComputation = false;
	
	@Override
	public Cell findOrCounterStrike(DataWrapper dataWrapper, int playingColor) throws InterruptedException {
		
		StopWatch stopWatch = new StopWatch("findOrCounterStrike");
		stopWatch.start();
		
		Map<Integer, Set<DataWrapper>> failedTries = new HashMap<>();
		
		Cell directThreat = directStrike(dataWrapper, playingColor, failedTries);

		if (directThreat != null) {
			
			stopWatch.stop();
			
			if (log.isDebugEnabled()) {
				log.debug("direct strike found in " + stopWatch.getTotalTimeMillis() + " ms");
			}
			
			return directThreat;
		}

		Cell opponentDirectThreat = directStrike(dataWrapper, -playingColor, failedTries);

		if (opponentDirectThreat != null) {
			List<Cell> counterOpponentThreats = counterDirectStrikeMoves(dataWrapper, playingColor, failedTries);

			if (!counterOpponentThreats.isEmpty()) {
				return minMaxService.computeMinMax(dataWrapper, playingColor, counterOpponentThreats);
			}
		}
		
		for (int maxDepth = 1; maxDepth <= STRIKE_MAX_DEPTH; maxDepth++) {
			
			long timeElapsed = System.currentTimeMillis();	
			
			Cell secondaryStrike = secondaryStrike(dataWrapper, playingColor, 0, maxDepth, failedTries);
			
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
	
	@Override
	public void stopComputation() {
		stopComputation = true;
	}

	private Cell directStrike(DataWrapper dataWrapper, int playingColor, Map<Integer, Set<DataWrapper>> failedTries) throws InterruptedException {

		if (failedTries.containsKey(playingColor) && failedTries.get(playingColor).contains(dataWrapper)) {
			return null;
		}
		
		if (stopComputation) {
			stopComputation = false;
			throw new InterruptedException();
		}
		
		ThreatContext computeThreatContext = threatContextService.computeThreatContext(dataWrapper.getData(), playingColor);
		
		Map<ThreatType, List<Threat>> threatMap = computeThreatContext.getThreatTypeToThreatMap();
		
		Map<ThreatType, Set<DoubleThreat>> doubleThreatMap = computeThreatContext.getDoubleThreatTypeToThreatMap();

		// check for a threat5
		if (!threatMap.get(ThreatType.THREAT_5).isEmpty()) {
			return threatMap.get(ThreatType.THREAT_5).iterator().next().getEmptyCells().iterator().next();
		}

		Map<ThreatType, List<Threat>> opponentThreatMap = threatContextService.computeThreatContext(dataWrapper.getData(), -playingColor).getThreatTypeToThreatMap();

		// check for an opponent threat5
		if (!opponentThreatMap.get(ThreatType.THREAT_5).isEmpty()) {

			Cell opponentThreat = opponentThreatMap.get(ThreatType.THREAT_5).iterator().next().getEmptyCells().iterator().next();

			// defend
			dataWrapper.addMove(opponentThreat, playingColor);
			
			if (customProperties.isDisplayAnalysis()) {
				analysisService.sendAnalysisCell(opponentThreat, playingColor);
			}
			
			// check for another threat5
			threatMap = threatContextService.computeThreatContext(dataWrapper.getData(), playingColor).getThreatTypeToThreatMap();
			if (!threatMap.get(ThreatType.THREAT_5).isEmpty()) {

				Cell newThreat = threatMap.get(ThreatType.THREAT_5).iterator().next().getEmptyCells().iterator().next();

				// opponent defends
				dataWrapper.addMove(newThreat, -playingColor);
				
				if (customProperties.isDisplayAnalysis()) {
					analysisService.sendAnalysisCell(newThreat, -playingColor);
				}

				// check for another strike
				Cell nextThreat = directStrike(dataWrapper, playingColor, failedTries);

				dataWrapper.removeMove(newThreat);
				
				if (customProperties.isDisplayAnalysis()) {
					analysisService.sendAnalysisCell(newThreat, EngineConstants.NONE_COLOR);
				}

				if (nextThreat != null) {
					dataWrapper.removeMove(opponentThreat);
					if (customProperties.isDisplayAnalysis()) {
						analysisService.sendAnalysisCell(opponentThreat, EngineConstants.NONE_COLOR);
					}
					return opponentThreat;
				}
			}

			dataWrapper.removeMove(opponentThreat);
			
			if (customProperties.isDisplayAnalysis()) {
				analysisService.sendAnalysisCell(opponentThreat, EngineConstants.NONE_COLOR);
			}

			return null;
		}

		// check for a double threat4 move
		if (!doubleThreatMap.get(ThreatType.DOUBLE_THREAT_4).isEmpty()) {
			return doubleThreatMap.get(ThreatType.DOUBLE_THREAT_4).iterator().next().getTargetCell();
		}

		// check for threat4 moves
		List<Threat> threat4List = threatMap.get(ThreatType.THREAT_4);

		Set<Cell> cells = new HashSet<>();
		
		threat4List.stream().forEach(t -> cells.addAll(t.getEmptyCells()));
		
		for (Cell threat4Move : cells) {

			dataWrapper.addMove(threat4Move, playingColor);
			
			if (customProperties.isDisplayAnalysis()) {
				analysisService.sendAnalysisCell(threat4Move, playingColor);
			}

			// opponent defends
			opponentThreatMap = threatContextService.computeThreatContext(dataWrapper.getData(), playingColor).getThreatTypeToThreatMap();

			Cell counterMove = opponentThreatMap.get(ThreatType.THREAT_5).iterator().next().getEmptyCells().iterator().next();

			dataWrapper.addMove(counterMove, -playingColor);
			
			if (customProperties.isDisplayAnalysis()) {
				analysisService.sendAnalysisCell(counterMove, -playingColor);
			}

			Cell nextAttempt = directStrike(dataWrapper, playingColor, failedTries);

			dataWrapper.removeMove(counterMove);
			dataWrapper.removeMove(threat4Move);
			
			if (customProperties.isDisplayAnalysis()) {
				analysisService.sendAnalysisCell(counterMove, EngineConstants.NONE_COLOR);
				analysisService.sendAnalysisCell(threat4Move, EngineConstants.NONE_COLOR);
			}

			if (nextAttempt != null) {
				return threat4Move;
			}
		}
		
		if (!failedTries.containsKey(playingColor)) {
			failedTries.put(playingColor, new HashSet<>());
		}
		
		failedTries.get(playingColor).add(new DataWrapper(dataWrapper));

		return null;
	}
	
	private List<Cell> counterDirectStrikeMoves(DataWrapper dataWrapper, int playingColor, Map<Integer, Set<DataWrapper>> failedTries) throws InterruptedException {
	
		List<Cell> defendingMoves = new ArrayList<>();
		
		ThreatContext opponentThreatContext = threatContextService.computeThreatContext(dataWrapper.getData(), -playingColor);
		
		Map<ThreatType, List<Threat>> opponentThreatMap = opponentThreatContext.getThreatTypeToThreatMap();
		
		Map<ThreatType, Set<DoubleThreat>> opponentDoubleThreatMap = opponentThreatContext.getDoubleThreatTypeToThreatMap();
		
		if (!opponentThreatMap.get(ThreatType.THREAT_5).isEmpty()) {
			defendingMoves.add(opponentThreatMap.get(ThreatType.THREAT_5).iterator().next().getEmptyCells().iterator().next());
			return defendingMoves;
		}
		
		if (!opponentDoubleThreatMap.get(ThreatType.DOUBLE_THREAT_4).isEmpty()) {
			
			for (DoubleThreat doubleThreat4 : opponentDoubleThreatMap.get(ThreatType.DOUBLE_THREAT_4)) {
				dataWrapper.addMove(doubleThreat4.getTargetCell(), playingColor);
				if (customProperties.isDisplayAnalysis()) {
					analysisService.sendAnalysisCell(doubleThreat4.getTargetCell(), playingColor);
				}
				if (directStrike(dataWrapper, -playingColor, failedTries) == null) {
					defendingMoves.add(doubleThreat4.getTargetCell());
				}
				dataWrapper.removeMove(doubleThreat4.getTargetCell());
				if (customProperties.isDisplayAnalysis()) {
					analysisService.sendAnalysisCell(doubleThreat4.getTargetCell(), EngineConstants.NONE_COLOR);
				}
				for (Cell emptyCell : doubleThreat4.getBlockingCells()) {
					dataWrapper.addMove(emptyCell, playingColor);
					if (customProperties.isDisplayAnalysis()) {
						analysisService.sendAnalysisCell(emptyCell, playingColor);
					}
					if (directStrike(dataWrapper, -playingColor, failedTries) == null) {
						defendingMoves.add(emptyCell);
					}
					dataWrapper.removeMove(emptyCell);
					if (customProperties.isDisplayAnalysis()) {
						analysisService.sendAnalysisCell(emptyCell, EngineConstants.NONE_COLOR);
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

	private Cell secondaryStrike(DataWrapper dataWrapper, int playingColor, int depth, final int maxDepth, Map<Integer, Set<DataWrapper>> failedTries) throws InterruptedException {

		// check for a strike
		Cell directStrike = directStrike(dataWrapper, playingColor, failedTries);

		if (directStrike != null) {
			return directStrike;
		}
		
		// check for an opponent strike
		Cell opponentDirectStrike = directStrike(dataWrapper, -playingColor, failedTries);

		if (opponentDirectStrike != null) {
			List<Cell> defendFromStrikes = counterDirectStrikeMoves(dataWrapper, playingColor, failedTries);

			// defend
			for (Cell defendFromStrike : defendFromStrikes) {
				
				dataWrapper.addMove(defendFromStrike, playingColor);
				
				if (customProperties.isDisplayAnalysis()) {
					analysisService.sendAnalysisCell(defendFromStrike, playingColor);
				}
				
				// check for a new strike
				Cell newStrike = directStrike(dataWrapper, playingColor, failedTries);

				if (newStrike != null) {
					List<Cell> opponentDefendFromStrikes = counterDirectStrikeMoves(dataWrapper, -playingColor, failedTries);

					boolean hasDefense = false;
					
					for (Cell opponentDefendFromStrike : opponentDefendFromStrikes) {
						
						dataWrapper.addMove(opponentDefendFromStrike, -playingColor);
						
						if (customProperties.isDisplayAnalysis()) {
							analysisService.sendAnalysisCell(opponentDefendFromStrike, -playingColor);
						}
						
						Cell newAttempt = secondaryStrike(dataWrapper, playingColor, depth + 1, maxDepth, failedTries);
						
						dataWrapper.removeMove(opponentDefendFromStrike);
						
						if (customProperties.isDisplayAnalysis()) {
							analysisService.sendAnalysisCell(opponentDefendFromStrike, EngineConstants.NONE_COLOR);
						}
						
						if (newAttempt == null) {
							hasDefense = true;
							break;
						}
					}
					
					if (!hasDefense) {
						dataWrapper.removeMove(defendFromStrike);
						if (customProperties.isDisplayAnalysis()) {
							analysisService.sendAnalysisCell(defendFromStrike, EngineConstants.NONE_COLOR);
						}
						return defendFromStrike;
					}
					
				}
				
				dataWrapper.removeMove(defendFromStrike);
				if (customProperties.isDisplayAnalysis()) {
					analysisService.sendAnalysisCell(defendFromStrike, EngineConstants.NONE_COLOR);
				}
			}

			return null;
		}
		
		if (depth == maxDepth) {
			return null;
		}
		
		ThreatContext threatContext = threatContextService.computeThreatContext(dataWrapper.getData(), playingColor);

		Cell retry = retrySecondary(dataWrapper, playingColor, threatContextService.findCombinedThreats(threatContext, ThreatType.DOUBLE_THREAT_3, ThreatType.DOUBLE_THREAT_3), depth, maxDepth, failedTries);
		
		if (retry != null) {
			return retry;
		}
		
		retry = retrySecondary(dataWrapper, playingColor, threatContextService.findCombinedThreats(threatContext, ThreatType.DOUBLE_THREAT_3, ThreatType.THREAT_3), depth, maxDepth, failedTries);
		
		if (retry != null) {
			return retry;
		}
		
		retry = retrySecondary(dataWrapper, playingColor, threatContextService.findCombinedThreats(threatContext, ThreatType.DOUBLE_THREAT_3, ThreatType.DOUBLE_THREAT_2), depth, maxDepth, failedTries);
		
		if (retry != null) {
			return retry;
		}
		
		
		Set<Cell> cells = new HashSet<>();
		
		threatContext.getThreatTypeToThreatMap().get(ThreatType.DOUBLE_THREAT_3).stream().forEach(t -> cells.addAll(t.getEmptyCells()));
		
		retry = retrySecondary(dataWrapper, playingColor, cells, depth, maxDepth, failedTries);
		
		if (retry != null) {
			return retry;
		}
		
		return null;
	}
	
	private Cell retrySecondary(DataWrapper dataWrapper, int playingColor, Set<Cell> cellsToTry, int depth, int maxDepth, Map<Integer, Set<DataWrapper>> failedTries) throws InterruptedException {
		
		for (Cell threat : cellsToTry) {
			dataWrapper.addMove(threat, playingColor);
			
			if (customProperties.isDisplayAnalysis()) {
				analysisService.sendAnalysisCell(threat, playingColor);
			}
			
			List<Cell> opponentDefendFromStrikes = counterDirectStrikeMoves(dataWrapper, -playingColor, failedTries);
			
			boolean hasDefense = false;
			
			for (Cell opponentDefendFromStrike : opponentDefendFromStrikes) {
				dataWrapper.addMove(opponentDefendFromStrike, -playingColor);
				
				if (customProperties.isDisplayAnalysis()) {
					analysisService.sendAnalysisCell(opponentDefendFromStrike, -playingColor);
				}
				
				Cell newAttempt = secondaryStrike(dataWrapper, playingColor, depth + 1, maxDepth, failedTries);
				
				dataWrapper.removeMove(opponentDefendFromStrike);
				
				if (customProperties.isDisplayAnalysis()) {
					analysisService.sendAnalysisCell(opponentDefendFromStrike, EngineConstants.NONE_COLOR);
				}
				
				if (newAttempt == null) {
					hasDefense = true;
					break;
				}
			}
			
			dataWrapper.removeMove(threat);
			
			if (customProperties.isDisplayAnalysis()) {
				analysisService.sendAnalysisCell(threat, EngineConstants.NONE_COLOR);
			}
			
			if (!hasDefense) {
				return threat;
			}
		}
		
		return null;
		
	}

}
