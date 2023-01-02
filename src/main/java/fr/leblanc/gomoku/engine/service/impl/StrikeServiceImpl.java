package fr.leblanc.gomoku.engine.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.DataWrapper;
import fr.leblanc.gomoku.engine.model.DoubleThreat;
import fr.leblanc.gomoku.engine.model.EngineConstants;
import fr.leblanc.gomoku.engine.model.Threat;
import fr.leblanc.gomoku.engine.model.ThreatContext;
import fr.leblanc.gomoku.engine.model.ThreatType;
import fr.leblanc.gomoku.engine.model.messaging.EngineSettingsDto;
import fr.leblanc.gomoku.engine.service.MessagingService;
import fr.leblanc.gomoku.engine.service.MinMaxService;
import fr.leblanc.gomoku.engine.service.StrikeService;
import fr.leblanc.gomoku.engine.service.ThreatContextService;
import lombok.extern.apachecommons.CommonsLog;

@Service
@CommonsLog
public class StrikeServiceImpl implements StrikeService {

	@Autowired
	private ThreatContextService threatContextService;
	
	@Autowired
	private MinMaxService minMaxService;
	
	@Autowired
	private MessagingService messagingService;
	
	private Boolean isComputing = false;
	
	private Boolean stopComputation = false;
	
	private ExecutorService executor = Executors.newSingleThreadExecutor();

	@Override
	public Cell findOrCounterStrike(DataWrapper dataWrapper, int playingColor, EngineSettingsDto engineSettings) throws InterruptedException {
		
		StrikeCommand command = new StrikeCommand(dataWrapper, playingColor, engineSettings);
		
		Cell result = null;
		
		try {
			
			if (engineSettings.getStrikeTimeout() == -1) {
				result = command.call();
			} else {
				result = executor.invokeAny(List.of(command), engineSettings.getStrikeTimeout(), TimeUnit.SECONDS);
			}
			
		} catch (ExecutionException e) {
			log.error("Error while executing strike command : " + e.getMessage(), e);
		} catch (TimeoutException e) {
			log.info("Strike command timeout (" + engineSettings.getStrikeTimeout() + "s)");
		}
		
		return result;
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
	
	private class StrikeCommand implements Callable<Cell> {

		private DataWrapper dataWrapper;
		private int playingColor;
		private EngineSettingsDto engineSettings;
		
		private StrikeCommand(DataWrapper dataWrapper, int playingColor, EngineSettingsDto engineSettings) {
			this.dataWrapper = dataWrapper;
			this.playingColor = playingColor;
			this.engineSettings = engineSettings;
		}
		
		@Override
		public Cell call() throws InterruptedException {
			
			if (!engineSettings.isStrikeEnabled()) {
				return null;
			}
			
			isComputing = true;
			
			StopWatch stopWatch = new StopWatch("findOrCounterStrike");
			stopWatch.start();
			
			Map<Integer, Set<DataWrapper>> failedTries = new HashMap<>();
			
			if (log.isDebugEnabled()) {
				log.debug("find direct strike...");
			}
			
			Cell directThreat = directStrike(dataWrapper, playingColor, failedTries);

			if (directThreat != null) {
				
				stopWatch.stop();
				
				if (log.isDebugEnabled()) {
					log.debug("direct strike found in " + stopWatch.getTotalTimeMillis() + " ms");
				}
				
				isComputing = false;
				
				return directThreat;
			}
			
			if (log.isDebugEnabled()) {
				log.debug("strike not found");
			}

			Cell opponentDirectThreat = directStrike(dataWrapper, -playingColor, failedTries);

			if (opponentDirectThreat != null) {
				if (log.isDebugEnabled()) {
					log.debug("defend from opponent direct strike...");
				}
				List<Cell> counterOpponentThreats = counterDirectStrikeMoves(dataWrapper, playingColor, failedTries);

				if (!counterOpponentThreats.isEmpty()) {
					isComputing = false;
					
					Cell defense = minMaxService.computeMinMax(dataWrapper, playingColor, counterOpponentThreats, engineSettings);
					
					stopWatch.stop();
					
					if (log.isDebugEnabled()) {
						log.debug("best defense found in " + stopWatch.getTotalTimeMillis() + " ms");
					}
					
					return defense;
				}
			}
			
			for (int maxDepth = 1; maxDepth <= engineSettings.getStrikeDepth(); maxDepth++) {
				
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
		
	}

	private Cell directStrike(DataWrapper dataWrapper, int playingColor, Map<Integer, Set<DataWrapper>> failedTries) throws InterruptedException {

		if (failedTries.containsKey(playingColor) && failedTries.get(playingColor).contains(dataWrapper)) {
			return null;
		}
		
		if (Boolean.TRUE.equals(stopComputation)) {
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
				
				messagingService.sendAnalysisCell(opponentThreat, playingColor);
				
				// check for another threat5
				threatMap = threatContextService.computeThreatContext(dataWrapper.getData(), playingColor).getThreatTypeToThreatMap();
				if (!threatMap.get(ThreatType.THREAT_5).isEmpty()) {
					
					Cell newThreat = threatMap.get(ThreatType.THREAT_5).iterator().next().getEmptyCells().iterator().next();
					
					// opponent defends
					dataWrapper.addMove(newThreat, -playingColor);
					
					messagingService.sendAnalysisCell(newThreat, -playingColor);
					
					// check for another strike
					Cell nextThreat = directStrike(dataWrapper, playingColor, failedTries);
					
					dataWrapper.removeMove(newThreat);
					
					messagingService.sendAnalysisCell(newThreat, EngineConstants.NONE_COLOR);
					
					if (nextThreat != null) {
						dataWrapper.removeMove(opponentThreat);
						messagingService.sendAnalysisCell(opponentThreat, EngineConstants.NONE_COLOR);
						return opponentThreat;
					}
				}
				
				dataWrapper.removeMove(opponentThreat);
				
				messagingService.sendAnalysisCell(opponentThreat, EngineConstants.NONE_COLOR);
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
			
			messagingService.sendAnalysisCell(threat4Move, playingColor);

			// opponent defends
			opponentThreatMap = threatContextService.computeThreatContext(dataWrapper.getData(), playingColor).getThreatTypeToThreatMap();

			Cell counterMove = opponentThreatMap.get(ThreatType.THREAT_5).iterator().next().getEmptyCells().iterator().next();

			dataWrapper.addMove(counterMove, -playingColor);
			
			messagingService.sendAnalysisCell(counterMove, -playingColor);

			Cell nextAttempt = directStrike(dataWrapper, playingColor, failedTries);

			dataWrapper.removeMove(counterMove);
			dataWrapper.removeMove(threat4Move);
			
			messagingService.sendAnalysisCell(counterMove, EngineConstants.NONE_COLOR);
			messagingService.sendAnalysisCell(threat4Move, EngineConstants.NONE_COLOR);

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
				
				messagingService.sendAnalysisCell(defendFromStrike, playingColor);
				
				// check for a new strike
				Cell newStrike = directStrike(dataWrapper, playingColor, failedTries);

				if (newStrike != null) {
					List<Cell> opponentDefendFromStrikes = counterDirectStrikeMoves(dataWrapper, -playingColor, failedTries);

					boolean hasDefense = false;
					
					for (Cell opponentDefendFromStrike : opponentDefendFromStrikes) {
						
						dataWrapper.addMove(opponentDefendFromStrike, -playingColor);
						
						messagingService.sendAnalysisCell(opponentDefendFromStrike, -playingColor);
						
						Cell newAttempt = secondaryStrike(dataWrapper, playingColor, depth + 1, maxDepth, failedTries, secondaryFailedTries);
						
						dataWrapper.removeMove(opponentDefendFromStrike);
						
						messagingService.sendAnalysisCell(opponentDefendFromStrike, EngineConstants.NONE_COLOR);
							
						if (newAttempt == null) {
							hasDefense = true;
							break;
						}
					}
					
					if (!hasDefense) {
						dataWrapper.removeMove(defendFromStrike);
						messagingService.sendAnalysisCell(defendFromStrike, EngineConstants.NONE_COLOR);
						return defendFromStrike;
					}
					
				}
				
				dataWrapper.removeMove(defendFromStrike);
				messagingService.sendAnalysisCell(defendFromStrike, EngineConstants.NONE_COLOR);
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
			
			messagingService.sendAnalysisCell(threat, playingColor);
			
			List<Cell> opponentDefendFromStrikes = counterDirectStrikeMoves(dataWrapper, -playingColor, failedTries);
			
			boolean hasDefense = false;
			
			for (Cell opponentDefendFromStrike : opponentDefendFromStrikes) {
				dataWrapper.addMove(opponentDefendFromStrike, -playingColor);
				
				messagingService.sendAnalysisCell(opponentDefendFromStrike, -playingColor);
				
				Cell newAttempt = secondaryStrike(dataWrapper, playingColor, depth + 1, maxDepth, failedTries, secondaryFailedTries);
				
				dataWrapper.removeMove(opponentDefendFromStrike);
				
				messagingService.sendAnalysisCell(opponentDefendFromStrike, EngineConstants.NONE_COLOR);
				
				if (newAttempt == null) {
					hasDefense = true;
					break;
				}
			}
			
			dataWrapper.removeMove(threat);
			
			messagingService.sendAnalysisCell(threat, EngineConstants.NONE_COLOR);
			
			if (!hasDefense) {
				return threat;
			}
		}
		
		return null;
		
	}

}
