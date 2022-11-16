package fr.leblanc.gomoku.engine.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.DataWrapper;
import fr.leblanc.gomoku.engine.model.DoubleThreat;
import fr.leblanc.gomoku.engine.model.EngineConstants;
import fr.leblanc.gomoku.engine.model.Threat;
import fr.leblanc.gomoku.engine.model.ThreatContext;
import fr.leblanc.gomoku.engine.model.ThreatType;
import fr.leblanc.gomoku.engine.service.AnalysisService;
import fr.leblanc.gomoku.engine.service.MinMaxService;
import fr.leblanc.gomoku.engine.service.StrikeService;
import fr.leblanc.gomoku.engine.service.ThreatContextService;
import lombok.extern.log4j.Log4j2;


@Service
@Log4j2
public class StrikeServiceImpl implements StrikeService {

	@Value("${engine.strike.depth}")
	private int strikeDepth;
	
	@Value("${engine.display.analysis}")
	private boolean displayAnalysis;

	@Autowired
	private ThreatContextService threatContextService;
	
	@Autowired
	private MinMaxService minMaxService;
	
	@Autowired
	private AnalysisService analysisService;
	
	private Boolean isComputing = false;
	
	private Boolean stopComputation = false;
	
	@Override
	public Cell findOrCounterStrike(DataWrapper dataWrapper, int playingColor) throws InterruptedException {
		
		isComputing = true;
		
		StopWatch stopWatch = new StopWatch("findOrCounterStrike");
		stopWatch.start();
		
		Map<Integer, Set<DataWrapper>> failedTries = new HashMap<>();
		
		Cell directThreat = directStrike(dataWrapper, playingColor, failedTries);

		if (directThreat != null) {
			
			stopWatch.stop();
			
			if (log.isDebugEnabled()) {
				log.debug("direct strike found in " + stopWatch.getTotalTimeMillis() + " ms");
			}
			
			isComputing = false;
			
			return directThreat;
		}

		Cell opponentDirectThreat = directStrike(dataWrapper, -playingColor, failedTries);

		if (opponentDirectThreat != null) {
			List<Cell> counterOpponentThreats = counterDirectStrikeMoves(dataWrapper, playingColor, failedTries);

			if (!counterOpponentThreats.isEmpty()) {
				isComputing = false;
				return minMaxService.computeMinMax(dataWrapper, playingColor, counterOpponentThreats);
			}
		}
		
		for (int maxDepth = 1; maxDepth <= strikeDepth; maxDepth++) {
			
			long timeElapsed = System.currentTimeMillis();	
			
			Map<Integer, Set<DataWrapper>> secondaryFailedTries = new HashMap<>();
			
			Cell secondaryStrike = secondaryStrike(dataWrapper, playingColor, 0, maxDepth, failedTries, secondaryFailedTries);
			
			if (secondaryStrike != null) {
				
				stopWatch.stop();
				
				if (log.isDebugEnabled()) {
					log.debug("secondary strike found in " + stopWatch.getTotalTimeMillis() + " ms for maxDepth = " + maxDepth);
				}
				
				isComputing = false;
				
				return secondaryStrike;
			}
			
			if (log.isDebugEnabled()) {
				timeElapsed = System.currentTimeMillis() - timeElapsed;
				log.debug("secondary strike failed for maxDepth = " + maxDepth + " (" + timeElapsed +" ms)");
			}
			
		}
		
		isComputing = false;
		
		return null;
	}
	
	@Override
	public boolean isComputing() {
		return isComputing;
	}

	@Override
	public void stopComputation() {
		stopComputation = true;
		isComputing = false;
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
			
			Set<Cell> opponentThreats = new HashSet<>();
			
			opponentThreatMap.get(ThreatType.THREAT_5).stream().forEach(t -> t.getEmptyCells().stream().forEach(opponentThreats::add));
			
			if (opponentThreats.size() == 1) {
				
				Cell opponentThreat = opponentThreats.iterator().next();
				
				// defend
				dataWrapper.addMove(opponentThreat, playingColor);
				
				if (displayAnalysis) {
					analysisService.sendAnalysisCell(opponentThreat, playingColor);
				}
				
				// check for another threat5
				threatMap = threatContextService.computeThreatContext(dataWrapper.getData(), playingColor).getThreatTypeToThreatMap();
				if (!threatMap.get(ThreatType.THREAT_5).isEmpty()) {
					
					Cell newThreat = threatMap.get(ThreatType.THREAT_5).iterator().next().getEmptyCells().iterator().next();
					
					// opponent defends
					dataWrapper.addMove(newThreat, -playingColor);
					
					if (displayAnalysis) {
						analysisService.sendAnalysisCell(newThreat, -playingColor);
					}
					
					// check for another strike
					Cell nextThreat = directStrike(dataWrapper, playingColor, failedTries);
					
					dataWrapper.removeMove(newThreat);
					
					if (displayAnalysis) {
						analysisService.sendAnalysisCell(newThreat, EngineConstants.NONE_COLOR);
					}
					
					if (nextThreat != null) {
						dataWrapper.removeMove(opponentThreat);
						if (displayAnalysis) {
							analysisService.sendAnalysisCell(opponentThreat, EngineConstants.NONE_COLOR);
						}
						return opponentThreat;
					}
				}
				
				dataWrapper.removeMove(opponentThreat);
				
				if (displayAnalysis) {
					analysisService.sendAnalysisCell(opponentThreat, EngineConstants.NONE_COLOR);
				}
			}
			
			if (!failedTries.containsKey(playingColor)) {
				failedTries.put(playingColor, new HashSet<>());
			}
			
			failedTries.get(playingColor).add(new DataWrapper(dataWrapper));
			
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
			
			if (displayAnalysis) {
				analysisService.sendAnalysisCell(threat4Move, playingColor);
			}

			// opponent defends
			opponentThreatMap = threatContextService.computeThreatContext(dataWrapper.getData(), playingColor).getThreatTypeToThreatMap();

			Cell counterMove = opponentThreatMap.get(ThreatType.THREAT_5).iterator().next().getEmptyCells().iterator().next();

			dataWrapper.addMove(counterMove, -playingColor);
			
			if (displayAnalysis) {
				analysisService.sendAnalysisCell(counterMove, -playingColor);
			}

			Cell nextAttempt = directStrike(dataWrapper, playingColor, failedTries);

			dataWrapper.removeMove(counterMove);
			dataWrapper.removeMove(threat4Move);
			
			if (displayAnalysis) {
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
		
		if (!opponentThreatMap.get(ThreatType.THREAT_5).isEmpty()) {
			defendingMoves.add(opponentThreatMap.get(ThreatType.THREAT_5).iterator().next().getEmptyCells().iterator().next());
			return defendingMoves;
		}
		
		List<Cell> analysedMoves =  threatContextService.buildAnalyzedMoves(dataWrapper, playingColor);
		
		for (Cell analysedMove : analysedMoves) {
			dataWrapper.addMove(analysedMove, playingColor);
			if (directStrike(dataWrapper, -playingColor, failedTries) == null) {
				defendingMoves.add(analysedMove);
			}
			dataWrapper.removeMove(analysedMove);
		}
		
		return defendingMoves;
	}

	private Cell secondaryStrike(DataWrapper dataWrapper, int playingColor, int depth, final int maxDepth, Map<Integer, Set<DataWrapper>> failedTries, Map<Integer, Set<DataWrapper>> secondaryFailedTries) throws InterruptedException {

		if (depth == maxDepth) {
			return null;
		}
		
		if (secondaryFailedTries.containsKey(playingColor) && secondaryFailedTries.get(playingColor).contains(dataWrapper)) {
			return null;
		}
		
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
				
				if (displayAnalysis) {
					analysisService.sendAnalysisCell(defendFromStrike, playingColor);
				}
				
				// check for a new strike
				Cell newStrike = directStrike(dataWrapper, playingColor, failedTries);

				if (newStrike != null) {
					List<Cell> opponentDefendFromStrikes = counterDirectStrikeMoves(dataWrapper, -playingColor, failedTries);

					boolean hasDefense = false;
					
					for (Cell opponentDefendFromStrike : opponentDefendFromStrikes) {
						
						dataWrapper.addMove(opponentDefendFromStrike, -playingColor);
						
						if (displayAnalysis) {
							analysisService.sendAnalysisCell(opponentDefendFromStrike, -playingColor);
						}
						
						Cell newAttempt = secondaryStrike(dataWrapper, playingColor, depth + 1, maxDepth, failedTries, secondaryFailedTries);
						
						dataWrapper.removeMove(opponentDefendFromStrike);
						
						if (displayAnalysis) {
							analysisService.sendAnalysisCell(opponentDefendFromStrike, EngineConstants.NONE_COLOR);
						}
						
						if (newAttempt == null) {
							hasDefense = true;
							break;
						}
					}
					
					if (!hasDefense) {
						dataWrapper.removeMove(defendFromStrike);
						if (displayAnalysis) {
							analysisService.sendAnalysisCell(defendFromStrike, EngineConstants.NONE_COLOR);
						}
						return defendFromStrike;
					}
					
				}
				
				dataWrapper.removeMove(defendFromStrike);
				if (displayAnalysis) {
					analysisService.sendAnalysisCell(defendFromStrike, EngineConstants.NONE_COLOR);
				}
			}

			if (!secondaryFailedTries.containsKey(playingColor)) {
				secondaryFailedTries.put(playingColor, new HashSet<>());
			}
			
			if (depth + 1 != maxDepth) {
				secondaryFailedTries.get(playingColor).add(new DataWrapper(dataWrapper));
			}
			
			return null;
		}
		
		ThreatContext threatContext = threatContextService.computeThreatContext(dataWrapper.getData(), playingColor);

		Cell retry = retrySecondary(dataWrapper, playingColor, threatContextService.findCombinedThreats(threatContext, ThreatType.DOUBLE_THREAT_3, ThreatType.DOUBLE_THREAT_3), depth, maxDepth, failedTries, secondaryFailedTries);
		
		if (retry != null) {
			return retry;
		}
		
		retry = retrySecondary(dataWrapper, playingColor, threatContextService.findCombinedThreats(threatContext, ThreatType.DOUBLE_THREAT_3, ThreatType.DOUBLE_THREAT_2), depth, maxDepth, failedTries, secondaryFailedTries);
		
		if (retry != null) {
			return retry;
		}
		
		retry = retrySecondary(dataWrapper, playingColor, threatContextService.findCombinedThreats(threatContext, ThreatType.DOUBLE_THREAT_3, ThreatType.THREAT_3), depth, maxDepth, failedTries, secondaryFailedTries);
		
		if (retry != null) {
			return retry;
		}
		
		retry = retrySecondary(dataWrapper, playingColor, threatContextService.findCombinedThreats(threatContext, ThreatType.THREAT_4, ThreatType.DOUBLE_THREAT_3), depth, maxDepth, failedTries, secondaryFailedTries);
		
		if (retry != null) {
			return retry;
		}
		
		retry = retrySecondary(dataWrapper, playingColor, threatContextService.findCombinedThreats(threatContext, ThreatType.THREAT_4, ThreatType.DOUBLE_THREAT_2), depth, maxDepth, failedTries, secondaryFailedTries);
		
		if (retry != null) {
			return retry;
		}
		
		retry = retrySecondary(dataWrapper, playingColor, threatContext.getDoubleThreatTypeToThreatMap().get(ThreatType.DOUBLE_THREAT_3).stream().map(DoubleThreat::getTargetCell).collect(Collectors.toSet()), depth, maxDepth, failedTries, secondaryFailedTries);
		
		if (retry != null) {
			return retry;
		}
		
		if (!secondaryFailedTries.containsKey(playingColor)) {
			secondaryFailedTries.put(playingColor, new HashSet<>());
		}
		
		if (depth + 1 != maxDepth) {
			secondaryFailedTries.get(playingColor).add(new DataWrapper(dataWrapper));
		}
		
		return null;
	}
	
	private Cell retrySecondary(DataWrapper dataWrapper, int playingColor, Set<Cell> cellsToTry, int depth, int maxDepth, Map<Integer, Set<DataWrapper>> failedTries, Map<Integer, Set<DataWrapper>> secondaryFailedTries) throws InterruptedException {
		
		for (Cell threat : cellsToTry) {
			dataWrapper.addMove(threat, playingColor);
			
			if (displayAnalysis) {
				analysisService.sendAnalysisCell(threat, playingColor);
			}
			
			List<Cell> opponentDefendFromStrikes = counterDirectStrikeMoves(dataWrapper, -playingColor, failedTries);
			
			boolean hasDefense = false;
			
			for (Cell opponentDefendFromStrike : opponentDefendFromStrikes) {
				dataWrapper.addMove(opponentDefendFromStrike, -playingColor);
				
				if (displayAnalysis) {
					analysisService.sendAnalysisCell(opponentDefendFromStrike, -playingColor);
				}
				
				Cell newAttempt = secondaryStrike(dataWrapper, playingColor, depth + 1, maxDepth, failedTries, secondaryFailedTries);
				
				dataWrapper.removeMove(opponentDefendFromStrike);
				
				if (displayAnalysis) {
					analysisService.sendAnalysisCell(opponentDefendFromStrike, EngineConstants.NONE_COLOR);
				}
				
				if (newAttempt == null) {
					hasDefense = true;
					break;
				}
			}
			
			dataWrapper.removeMove(threat);
			
			if (displayAnalysis) {
				analysisService.sendAnalysisCell(threat, EngineConstants.NONE_COLOR);
			}
			
			if (!hasDefense) {
				return threat;
			}
		}
		
		return null;
		
	}

}
