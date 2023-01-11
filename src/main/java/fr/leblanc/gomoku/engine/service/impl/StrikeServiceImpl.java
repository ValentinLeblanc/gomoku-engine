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
import fr.leblanc.gomoku.engine.model.StrikeContext;
import fr.leblanc.gomoku.engine.model.StrikeResult;
import fr.leblanc.gomoku.engine.model.StrikeResult.StrikeType;
import fr.leblanc.gomoku.engine.model.Threat;
import fr.leblanc.gomoku.engine.model.ThreatContext;
import fr.leblanc.gomoku.engine.model.ThreatType;
import fr.leblanc.gomoku.engine.service.MessageService;
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
	private MessageService messagingService;
	
	private Boolean isComputing = false;
	
	private Boolean stopComputation = false;
	
	private ExecutorService executor = Executors.newSingleThreadExecutor();

	@Override
	public StrikeResult processStrike(DataWrapper dataWrapper, int playingColor, int strikeDepth, int minMaxDepth, int strikeTimeout) throws InterruptedException {
		
		isComputing = true;
		
		try {
			StopWatch stopWatch = new StopWatch("processStrike");
			stopWatch.start();
			
			StrikeContext strikeContext = new StrikeContext();
			
			strikeContext.setStrikeDepth(strikeDepth);
			strikeContext.setMinMaxDepth(minMaxDepth);
			strikeContext.setStrikeTimeout(strikeTimeout);
			
			if (log.isDebugEnabled()) {
				log.debug("find direct strike...");
			}
			
			Cell directThreat = directStrike(dataWrapper, playingColor, strikeContext);
			
			if (directThreat != null) {
				
				stopWatch.stop();
				
				if (log.isDebugEnabled()) {
					log.debug("direct strike found in " + stopWatch.getTotalTimeMillis() + " ms");
				}
				
				return new StrikeResult(directThreat, StrikeType.DIRECT_STRIKE);
			}
			
			Cell opponentDirectThreat = directStrike(dataWrapper, -playingColor, strikeContext);
			
			if (opponentDirectThreat != null) {
				if (log.isDebugEnabled()) {
					log.debug("defend from opponent direct strike...");
				}
				List<Cell> counterOpponentThreats = counterDirectStrikeMoves(dataWrapper, playingColor, strikeContext, false);
				
				if (!counterOpponentThreats.isEmpty()) {
					
					Cell defense = minMaxService.computeMinMax(dataWrapper, playingColor, counterOpponentThreats, strikeContext.getMinMaxDepth()).getOptimalMoves().get(0);
					
					stopWatch.stop();
					
					if (log.isDebugEnabled()) {
						log.debug("best defense found in " + stopWatch.getTotalTimeMillis() + " ms");
					}
					
					return new StrikeResult(defense, StrikeType.DEFEND_STRIKE);
				}
				
				return new StrikeResult(null, StrikeType.EMPTY_STRIKE);
			}
			
			if (log.isDebugEnabled()) {
				log.debug("find secondary strike...");
			}
			
			Cell secondaryStrike = executeSecondaryStrikeCommand(dataWrapper, playingColor, strikeContext);
			
			if (secondaryStrike != null) {
				return new StrikeResult(secondaryStrike, StrikeType.SECONDARY_STRIKE);
			}
		} finally {
			isComputing = false;
		}
		
		return new StrikeResult(null, StrikeType.EMPTY_STRIKE);
	}

	private Cell executeSecondaryStrikeCommand(DataWrapper dataWrapper, int playingColor, StrikeContext strikeContext) throws InterruptedException {
		SecondaryStrikeCommand command = new SecondaryStrikeCommand(dataWrapper, playingColor, strikeContext);
		
		Cell secondaryStrike = null;
		
		try {

			if (strikeContext.getStrikeTimeout() == -1) {
				secondaryStrike = command.call();
			} else {
				secondaryStrike = executor.invokeAny(List.of(command), strikeContext.getStrikeTimeout(), TimeUnit.SECONDS);
			}
			
		} catch (ExecutionException e) {
			
			if (e.getCause() instanceof InterruptedException interruptedException) {
				throw interruptedException;
			}
			
			log.error("Error while SecondaryStrikeCommand : " + e.getMessage(), e);
		} catch (TimeoutException e) {
			log.info("SecondaryStrikeCommand timeout (" + strikeContext.getStrikeTimeout() + "s)");
		}
		return secondaryStrike;
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
	
	private class SecondaryStrikeCommand implements Callable<Cell> {

		private DataWrapper dataWrapper;
		private int playingColor;
		private StrikeContext strikeContext;
		
		private SecondaryStrikeCommand(DataWrapper dataWrapper, int playingColor, StrikeContext strikeContext) {
			this.dataWrapper = dataWrapper;
			this.playingColor = playingColor;
			this.strikeContext = strikeContext;
		}
		
		@Override
		public Cell call() throws InterruptedException {

			StopWatch stopWatch = new StopWatch("findOrCounterStrike");
			stopWatch.start();
			
			for (int maxDepth = 1; maxDepth <= strikeContext.getStrikeDepth(); maxDepth++) {
				
				long timeElapsed = System.currentTimeMillis();	
				
				Cell secondaryStrike = secondaryStrike(dataWrapper, playingColor, 0, strikeContext);
				
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
		
	}

	private Cell directStrike(DataWrapper dataWrapper, int playingColor, StrikeContext strikeContext) throws InterruptedException {

		if (Boolean.TRUE.equals(stopComputation)) {
			stopComputation = false;
			throw new InterruptedException();
		}
		
		Map<Integer, Set<DataWrapper>> strikeAttempts = strikeContext.getDirectStrikeAttempts();
		
		if (strikeAttempts.containsKey(playingColor) && strikeAttempts.get(playingColor).contains(dataWrapper)) {
			return null;
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
					Cell nextThreat = directStrike(dataWrapper, playingColor, strikeContext);
					
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
			
			if (!strikeAttempts.containsKey(playingColor)) {
				strikeAttempts.put(playingColor, new HashSet<>());
			}
			
			strikeAttempts.get(playingColor).add(new DataWrapper(dataWrapper));
			
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

			Cell nextAttempt = directStrike(dataWrapper, playingColor, strikeContext);

			dataWrapper.removeMove(counterMove);
			dataWrapper.removeMove(threat4Move);
			
			messagingService.sendAnalysisCell(counterMove, EngineConstants.NONE_COLOR);
			messagingService.sendAnalysisCell(threat4Move, EngineConstants.NONE_COLOR);

			if (nextAttempt != null) {
				return threat4Move;
			}
		}
		
		if (!strikeAttempts.containsKey(playingColor)) {
			strikeAttempts.put(playingColor, new HashSet<>());
		}
		
		strikeAttempts.get(playingColor).add(new DataWrapper(dataWrapper));

		return null;
	}
	
	private List<Cell> counterDirectStrikeMoves(DataWrapper dataWrapper, int playingColor, StrikeContext strikeContext, boolean onlyOne) throws InterruptedException {
	
		if (strikeContext.getRecordedCounterMoves().containsKey(playingColor) && strikeContext.getRecordedCounterMoves().get(playingColor).containsKey(dataWrapper)) {
			return strikeContext.getRecordedCounterMoves().get(playingColor).get(dataWrapper);
		}
		
		List<Cell> defendingMoves = new ArrayList<>();
		
		ThreatContext opponentThreatContext = threatContextService.computeThreatContext(dataWrapper.getData(), -playingColor);
		
		Map<ThreatType, List<Threat>> opponentThreatMap = opponentThreatContext.getThreatTypeToThreatMap();
		
		if (!opponentThreatMap.get(ThreatType.THREAT_5).isEmpty()) {
			defendingMoves.add(opponentThreatMap.get(ThreatType.THREAT_5).iterator().next().getEmptyCells().iterator().next());
			return defendingMoves;
		}
		
		List<Cell> analysedMoves =  threatContextService.buildAnalyzedMoves(dataWrapper, playingColor);
		
		for (Cell analysedMove : analysedMoves) {
			messagingService.sendAnalysisCell(analysedMove, playingColor);
			
			dataWrapper.addMove(analysedMove, playingColor);
			if (directStrike(dataWrapper, -playingColor, strikeContext) == null) {
				
				Map<ThreatType, List<Threat>> newThreatContext = threatContextService.computeThreatContext(dataWrapper.getData(), playingColor).getThreatTypeToThreatMap();
				
				if (!newThreatContext.get(ThreatType.THREAT_5).isEmpty()) {
					
					Cell counter = newThreatContext.get(ThreatType.THREAT_5).iterator().next().getEmptyCells().iterator().next();
					
					dataWrapper.addMove(counter, -playingColor);
					messagingService.sendAnalysisCell(counter, -playingColor);
					
					List<Cell> nextCounters = counterDirectStrikeMoves(dataWrapper, playingColor, strikeContext, true);
					
					if (!nextCounters.isEmpty()) {
						defendingMoves.add(analysedMove);
						if (onlyOne) {
							dataWrapper.removeMove(counter);
							messagingService.sendAnalysisCell(counter, EngineConstants.NONE_COLOR);
							dataWrapper.removeMove(analysedMove);
							messagingService.sendAnalysisCell(analysedMove, EngineConstants.NONE_COLOR);
							return defendingMoves;
						}
					}
					
					dataWrapper.removeMove(counter);
					messagingService.sendAnalysisCell(counter, EngineConstants.NONE_COLOR);
					
				} else {
					defendingMoves.add(analysedMove);
					if (onlyOne) {
						dataWrapper.removeMove(analysedMove);
						messagingService.sendAnalysisCell(analysedMove, EngineConstants.NONE_COLOR);
						return defendingMoves;
					}
				}
				
			}
			
			messagingService.sendAnalysisCell(analysedMove, EngineConstants.NONE_COLOR);
			dataWrapper.removeMove(analysedMove);
		}
		
		if (!strikeContext.getRecordedCounterMoves().containsKey(playingColor)) {
			strikeContext.getRecordedCounterMoves().put(playingColor, new HashMap<>());
		}
		
		strikeContext.getRecordedCounterMoves().get(playingColor).put(dataWrapper, defendingMoves);
		
		return defendingMoves;
	}

	private Cell secondaryStrike(DataWrapper dataWrapper, int playingColor, int depth, StrikeContext strikeContext) throws InterruptedException {

		if (Thread.currentThread().isInterrupted()) {
			throw new InterruptedException();
		}
		
		if (depth == strikeContext.getStrikeDepth()) {
			return null;
		}
		
		if (strikeContext.getSecondaryStrikeAttempts().containsKey(playingColor) && strikeContext.getSecondaryStrikeAttempts().get(playingColor).contains(dataWrapper)) {
			return null;
		}
		
		// check for a strike
		Cell directStrike = directStrike(dataWrapper, playingColor, strikeContext);

		if (directStrike != null) {
			return directStrike;
		}
		
		// check for an opponent strike
		Cell opponentDirectStrike = directStrike(dataWrapper, -playingColor, strikeContext);

		if (opponentDirectStrike != null) {
			return defendFromStrike(dataWrapper, playingColor, depth, strikeContext);
		}
		
		ThreatContext threatContext = threatContextService.computeThreatContext(dataWrapper.getData(), playingColor);

		Cell retry = retrySecondary(dataWrapper, playingColor, threatContextService.findCombinedThreats(threatContext, ThreatType.DOUBLE_THREAT_3, ThreatType.DOUBLE_THREAT_3), depth, strikeContext);
		
		if (retry != null) {
			return retry;
		}
		
		retry = retrySecondary(dataWrapper, playingColor, threatContextService.findCombinedThreats(threatContext, ThreatType.DOUBLE_THREAT_3, ThreatType.DOUBLE_THREAT_2), depth, strikeContext);
		
		if (retry != null) {
			return retry;
		}
		
		retry = retrySecondary(dataWrapper, playingColor, threatContextService.findCombinedThreats(threatContext, ThreatType.DOUBLE_THREAT_3, ThreatType.THREAT_3), depth, strikeContext);
		
		if (retry != null) {
			return retry;
		}
		
		retry = retrySecondary(dataWrapper, playingColor, threatContextService.findCombinedThreats(threatContext, ThreatType.THREAT_4, ThreatType.DOUBLE_THREAT_3), depth, strikeContext);
		
		if (retry != null) {
			return retry;
		}
		
		retry = retrySecondary(dataWrapper, playingColor, threatContextService.findCombinedThreats(threatContext, ThreatType.THREAT_4, ThreatType.DOUBLE_THREAT_2), depth, strikeContext);
		
		if (retry != null) {
			return retry;
		}
		
		retry = retrySecondary(dataWrapper, playingColor, threatContext.getDoubleThreatTypeToThreatMap().get(ThreatType.DOUBLE_THREAT_3).stream().map(DoubleThreat::getTargetCell).collect(Collectors.toSet()), depth, strikeContext);
		
		if (retry != null) {
			return retry;
		}
		
		if (!strikeContext.getSecondaryStrikeAttempts().containsKey(playingColor)) {
			strikeContext.getSecondaryStrikeAttempts().put(playingColor, new HashSet<>());
		}
		
		if (depth + 1 != strikeContext.getStrikeDepth()) {
			strikeContext.getSecondaryStrikeAttempts().get(playingColor).add(new DataWrapper(dataWrapper));
		}
		
		return null;
	}

	private Cell defendFromStrike(DataWrapper dataWrapper, int playingColor, int depth, StrikeContext strikeContext)
			throws InterruptedException {
		List<Cell> defendFromStrikes = counterDirectStrikeMoves(dataWrapper, playingColor, strikeContext, false);

		// defend
		for (Cell defendFromStrike : defendFromStrikes) {
			
			dataWrapper.addMove(defendFromStrike, playingColor);
			
			messagingService.sendAnalysisCell(defendFromStrike, playingColor);
			
			// check for a new strike
			Cell newStrike = directStrike(dataWrapper, playingColor, strikeContext);

			if (newStrike != null) {
				List<Cell> opponentDefendFromStrikes = counterDirectStrikeMoves(dataWrapper, -playingColor, strikeContext, false);

				boolean hasDefense = false;
				
				for (Cell opponentDefendFromStrike : opponentDefendFromStrikes) {
					
					dataWrapper.addMove(opponentDefendFromStrike, -playingColor);
					
					messagingService.sendAnalysisCell(opponentDefendFromStrike, -playingColor);
					
					Cell newAttempt = secondaryStrike(dataWrapper, playingColor, depth + 1, strikeContext);
					
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

		if (!strikeContext.getSecondaryStrikeAttempts().containsKey(playingColor)) {
			strikeContext.getSecondaryStrikeAttempts().put(playingColor, new HashSet<>());
		}
		
		if (depth + 1 != strikeContext.getStrikeDepth()) {
			strikeContext.getSecondaryStrikeAttempts().get(playingColor).add(new DataWrapper(dataWrapper));
		}
		
		return null;
	}
	
	private Cell retrySecondary(DataWrapper dataWrapper, int playingColor, Set<Cell> cellsToTry, int depth, StrikeContext strikeContext) throws InterruptedException {
		
		if (Thread.currentThread().isInterrupted()) {
			throw new InterruptedException();
		}
		
		for (Cell threat : cellsToTry) {
			dataWrapper.addMove(threat, playingColor);
			
			messagingService.sendAnalysisCell(threat, playingColor);
			
			List<Cell> opponentDefendFromStrikes = counterDirectStrikeMoves(dataWrapper, -playingColor, strikeContext, false);
			
			boolean hasDefense = false;
			
			for (Cell opponentDefendFromStrike : opponentDefendFromStrikes) {
				dataWrapper.addMove(opponentDefendFromStrike, -playingColor);
				
				messagingService.sendAnalysisCell(opponentDefendFromStrike, -playingColor);
				
				Cell newAttempt = secondaryStrike(dataWrapper, playingColor, depth + 1, strikeContext);
				
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
