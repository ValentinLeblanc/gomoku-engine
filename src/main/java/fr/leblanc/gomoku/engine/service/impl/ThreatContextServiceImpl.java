package fr.leblanc.gomoku.engine.service.impl;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.stereotype.Service;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.CompoThreatType;
import fr.leblanc.gomoku.engine.model.DataWrapper;
import fr.leblanc.gomoku.engine.model.DoubleThreat;
import fr.leblanc.gomoku.engine.model.EngineConstants;
import fr.leblanc.gomoku.engine.model.Threat;
import fr.leblanc.gomoku.engine.model.ThreatContext;
import fr.leblanc.gomoku.engine.model.ThreatType;
import fr.leblanc.gomoku.engine.service.ThreatContextService;
import fr.leblanc.gomoku.engine.util.Pair;

@Service
public class ThreatContextServiceImpl implements ThreatContextService {
	
	@Override
	public ThreatContext computeThreatContext(DataWrapper dataWrapper, int playingColor) {
		
		// this cache consumes too much memory
		
//		if (L2CacheSupport.isCacheEnabled() && L2CacheSupport.getThreatContextCache().containsKey(playingColor) && L2CacheSupport.getThreatContextCache().get(playingColor).containsKey(dataWrapper)) {
//			return L2CacheSupport.getThreatContextCache().get(playingColor).get(dataWrapper);
//		}
		
		ThreatContext threatContext = new ThreatContext(dataWrapper.getData(), playingColor);
		
		threatContext.getThreatTypeToThreatMap().put(ThreatType.THREAT_5, new ArrayList<>());
		threatContext.getThreatTypeToThreatMap().put(ThreatType.THREAT_4, new ArrayList<>());
		threatContext.getThreatTypeToThreatMap().put(ThreatType.THREAT_3, new ArrayList<>());
		threatContext.getThreatTypeToThreatMap().put(ThreatType.THREAT_2, new ArrayList<>());
		
		threatContext.getDoubleThreatTypeToThreatMap().put(ThreatType.DOUBLE_THREAT_4, new HashSet<>());
		threatContext.getDoubleThreatTypeToThreatMap().put(ThreatType.DOUBLE_THREAT_3, new HashSet<>());
		threatContext.getDoubleThreatTypeToThreatMap().put(ThreatType.DOUBLE_THREAT_2, new HashSet<>());
		
		internalComputeThreatContext(threatContext);
		
//		if (L2CacheSupport.isCacheEnabled()) {
//			if (!L2CacheSupport.getThreatContextCache().containsKey(playingColor)) {
//				L2CacheSupport.getThreatContextCache().put(playingColor, new HashMap<>());
//			}
//			
//			L2CacheSupport.getThreatContextCache().get(playingColor).put(new DataWrapper(dataWrapper), threatContext);
//		}
		
		return threatContext;
	}
	
	@Override
	public Set<Cell> findCombinedThreats(ThreatContext threatContext, ThreatType threatType1, ThreatType threatType2) {
		Set<Cell> combinedThreats = new HashSet<>();
		
		for (Entry<Cell, Map<ThreatType, List<Threat>>> entry : threatContext.getCellToThreatMap().entrySet()) {
			
			if (entry.getValue().get(threatType1) != null && entry.getValue().get(threatType2) != null) {
				
				for (int i = 0; i < entry.getValue().get(threatType1).size(); i++) {
					for (int j = 0; j < entry.getValue().get(threatType2).size(); j++) {
						if (!entry.getValue().get(threatType1).get(i).getPlainCells().containsAll(entry.getValue().get(threatType2).get(j).getPlainCells())) {
							combinedThreats.add(entry.getKey());
						}
					}
				}
			}
		}
		
		return combinedThreats;
	}
	
	@Override
	public List<Cell> buildAnalyzedMoves(DataWrapper dataWrapper, int color) {

		List<Cell> analysedMoves = new ArrayList<>();

		ThreatContext threatContext = computeThreatContext(dataWrapper, color);
		ThreatContext opponentThreatContext = computeThreatContext(dataWrapper, -color);
		
		Map<ThreatType, List<Threat>> threatMap = threatContext.getThreatTypeToThreatMap();
		Map<ThreatType, Set<DoubleThreat>> doubleThreatMap = threatContext.getDoubleThreatTypeToThreatMap();
		
		Map<ThreatType, List<Threat>> opponentThreatMap = opponentThreatContext.getThreatTypeToThreatMap();
		Map<ThreatType, Set<DoubleThreat>> opponentDoubleThreatMap = opponentThreatContext.getDoubleThreatTypeToThreatMap();
		
		threatMap.get(ThreatType.THREAT_5).stream().forEach(t -> t.getEmptyCells().stream().filter(c -> !analysedMoves.contains(c)).forEach(analysedMoves::add));
		opponentThreatMap.get(ThreatType.THREAT_5).stream().forEach(t -> t.getEmptyCells().stream().filter(c -> !analysedMoves.contains(c)).forEach(analysedMoves::add));
		
		doubleThreatMap.get(ThreatType.DOUBLE_THREAT_4).stream().filter(t -> !analysedMoves.contains(t.getTargetCell())).forEach(t -> analysedMoves.add(t.getTargetCell()));
		opponentDoubleThreatMap.get(ThreatType.DOUBLE_THREAT_4).stream().filter(t -> !analysedMoves.contains(t.getTargetCell())).forEach(t -> analysedMoves.add(t.getTargetCell()));
		opponentDoubleThreatMap.get(ThreatType.DOUBLE_THREAT_4).stream().forEach(t -> t.getBlockingCells().stream().filter(c -> !analysedMoves.contains(c)).forEach(analysedMoves::add));
		
		threatMap.get(ThreatType.THREAT_4).stream().forEach(t -> t.getEmptyCells().stream().filter(c -> !analysedMoves.contains(c)).forEach(analysedMoves::add));
		opponentThreatMap.get(ThreatType.THREAT_4).stream().forEach(t -> t.getEmptyCells().stream().filter(c -> !analysedMoves.contains(c)).forEach(analysedMoves::add));
		
		List<Cell> doubleThreat3Targets = doubleThreatMap.get(ThreatType.DOUBLE_THREAT_3).stream().map(DoubleThreat::getTargetCell).toList();
		doubleThreat3Targets.stream().filter(c -> doubleThreatMap.get(ThreatType.DOUBLE_THREAT_3).stream().filter(t -> t.getTargetCell().equals(c)).count() > 1).filter(c -> !analysedMoves.contains(c)).forEach(analysedMoves::add);
		
		doubleThreatMap.get(ThreatType.DOUBLE_THREAT_3).stream().filter(t -> !analysedMoves.contains(t.getTargetCell())).forEach(t -> analysedMoves.add(t.getTargetCell()));
		opponentDoubleThreatMap.get(ThreatType.DOUBLE_THREAT_3).stream().filter(t -> !analysedMoves.contains(t.getTargetCell())).forEach(t -> analysedMoves.add(t.getTargetCell()));
		opponentDoubleThreatMap.get(ThreatType.DOUBLE_THREAT_3).stream().forEach(t -> t.getBlockingCells().stream().filter(c -> !analysedMoves.contains(c)).forEach(analysedMoves::add));
		
		threatMap.get(ThreatType.THREAT_3).stream().forEach(t -> t.getEmptyCells().stream().filter(c -> !analysedMoves.contains(c)).forEach(analysedMoves::add));
		doubleThreatMap.get(ThreatType.DOUBLE_THREAT_2).stream().filter(t -> !analysedMoves.contains(t.getTargetCell())).forEach(t -> analysedMoves.add(t.getTargetCell()));
		threatMap.get(ThreatType.THREAT_2).stream().forEach(t -> t.getEmptyCells().stream().filter(c -> !analysedMoves.contains(c)).forEach(analysedMoves::add));

		List<Cell> notPlayedMoves = new ArrayList<>();
		
		for (int i = 0; i < dataWrapper.getData().length; i++) {
			for (int j = 0; j < dataWrapper.getData().length; j++) {
				if (dataWrapper.getValue(i, j) == EngineConstants.NONE_COLOR) {
					Cell cell = new Cell(i, j);
					if (!analysedMoves.contains(cell)) {
						notPlayedMoves.add(cell);
					}
				}
			}
		}
		
		notPlayedMoves.sort((c1, c2) -> {
			int radius1 = Math.abs(c1.getColumn() - dataWrapper.getData().length / 2) + Math.abs(c1.getRow() - dataWrapper.getData().length / 2);
			int radius2 = Math.abs(c2.getColumn() - dataWrapper.getData().length / 2) + Math.abs(c2.getRow() - dataWrapper.getData().length / 2);
			return Integer.compare(radius1, radius2);
		});
		
		analysedMoves.addAll(notPlayedMoves);
		
		return analysedMoves;
	}
	
	@Override
	public Map<Threat, Integer> getEffectiveThreats(ThreatContext playingThreatContext, ThreatContext opponentThreatContext, ThreatType threatType, ThreatType secondThreatType) {
		Map<Threat, Integer> map = new HashMap<>();
		
		if (threatType == ThreatType.THREAT_5) {
			
			if (!playingThreatContext.getThreatTypeToThreatMap().get(ThreatType.THREAT_5).isEmpty()) {
				map.put(playingThreatContext.getThreatTypeToThreatMap().get(ThreatType.THREAT_5).get(0), playingThreatContext.getThreatTypeToThreatMap().get(ThreatType.THREAT_5).size());
			}
			
		} else if (threatType == ThreatType.DOUBLE_THREAT_4) {
			for (DoubleThreat threat : playingThreatContext.getDoubleThreatTypeToThreatMap().get(ThreatType.DOUBLE_THREAT_4)) {
				Cell playingCell = threat.getTargetCell();
				if (!hasT5Counter(playingCell, opponentThreatContext)) {
					map.put(threat, map.computeIfAbsent(threat, k -> 0) + 1);
				}
			}
		} else if (threatType == ThreatType.THREAT_4) {
			
			Set<Threat> visitedThreats = new HashSet<>();
			
			for (Threat threat : playingThreatContext.getThreatTypeToThreatMap().get(ThreatType.THREAT_4)) {
				visitedThreats.add(threat);
				for (Cell playingCell : threat.getEmptyCells()) {
					
					if (!hasT5Counter(playingCell, opponentThreatContext)) {
						
						if (ThreatType.THREAT_4.equals(secondThreatType)) {
							
							// find threat 4
							if (playingThreatContext.getCellToThreatMap().get(playingCell).get(ThreatType.THREAT_4) != null) {
								
								if (playingThreatContext.getCellToThreatMap().get(playingCell).get(ThreatType.THREAT_4).stream().anyMatch(t -> !visitedThreats.contains(t)  && !threat.getPlainCells().containsAll(t.getPlainCells()) && !threat.getEmptyCells().containsAll(t.getEmptyCells()))) {
									map.put(threat, map.computeIfAbsent(threat, k -> 0) + 1);
								}
								
							}
						} else if (ThreatType.DOUBLE_THREAT_3.equals(secondThreatType)) {
							Cell blockingCell = threat.getEmptyCells().stream().filter(c -> !c.equals(playingCell)).findFirst().orElseThrow();
							
							if (!hasT4Counter(blockingCell, opponentThreatContext)) {
								
								long count = playingThreatContext.getDoubleThreatTypeToThreatMap().get(ThreatType.DOUBLE_THREAT_3).stream().filter(t -> !threat.getPlainCells().containsAll(t.getPlainCells()) && t.getTargetCell().equals(playingCell)).count();

								if (count > 0) {
									map.put(threat, map.computeIfAbsent(threat, k -> 0) + (int) count);
								}
							}
						}
					}
				}
			}
		} else if (threatType == ThreatType.DOUBLE_THREAT_3) {
			Set<Threat> visitedThreats = new HashSet<>();
			
			for (DoubleThreat threat : playingThreatContext.getDoubleThreatTypeToThreatMap().get(ThreatType.DOUBLE_THREAT_3)) {
				visitedThreats.add(threat);
				Cell playingCell = threat.getTargetCell();
				if (!hasT5Counter(playingCell, opponentThreatContext)) {
					
					if (threat.getBlockingCells().stream().allMatch(blockingCell -> !hasT4Counter(blockingCell, opponentThreatContext))) {
						
						if (ThreatType.DOUBLE_THREAT_3.equals(secondThreatType)) {
							long count = playingThreatContext.getDoubleThreatTypeToThreatMap().get(ThreatType.DOUBLE_THREAT_3).stream().filter(t -> !threat.getPlainCells().containsAll(t.getPlainCells()) && t.getTargetCell().equals(threat.getTargetCell())).count();
							
							if (count > 0) {
								map.put(threat, map.computeIfAbsent(threat, k -> 0) + (int) count);
							}
						} else if (ThreatType.DOUBLE_THREAT_2.equals(secondThreatType)) {
								
							if (threat.getBlockingCells().stream().allMatch(blockingCell -> !hasDT3Counter(blockingCell, opponentThreatContext))) {
								
								long count = playingThreatContext.getDoubleThreatTypeToThreatMap().get(ThreatType.DOUBLE_THREAT_2).stream().filter(t -> !threat.getPlainCells().containsAll(t.getPlainCells()) && t.getTargetCell().equals(playingCell)).count();
								
								if (count > 0) {
									map.put(threat, map.computeIfAbsent(threat, k -> 0) + (int) count);
								}
							}
							
						}
						
					}
					
				}
			}
		} else if (threatType == ThreatType.DOUBLE_THREAT_2) {
			Set<Threat> visitedThreats = new HashSet<>();
			
			for (DoubleThreat threat : playingThreatContext.getDoubleThreatTypeToThreatMap().get(ThreatType.DOUBLE_THREAT_2)) {
				visitedThreats.add(threat);
				Cell playingCell = threat.getTargetCell();
				if (!hasT5Counter(playingCell, opponentThreatContext)) {
					
					if (threat.getBlockingCells().stream().allMatch(blockingCell -> !hasT4Counter(blockingCell, opponentThreatContext))) {
						
						if (ThreatType.DOUBLE_THREAT_2.equals(secondThreatType)) {
								
							if (threat.getBlockingCells().stream().allMatch(blockingCell -> !hasDT3Counter(blockingCell, opponentThreatContext))) {
								
								long count = playingThreatContext.getDoubleThreatTypeToThreatMap().get(ThreatType.DOUBLE_THREAT_2).stream().filter(t -> !threat.getPlainCells().containsAll(t.getPlainCells()) && t.getTargetCell().equals(playingCell)).count();
								
								if (count > 0) {
									map.put(threat, map.computeIfAbsent(threat, k -> 0) + 1);
								}
							}
							
						}
						
					}
					
				}
			}
		}
		
		return map;
	}
	
	@Override
	public Map<Cell, Pair<Threat, List<Threat>>> findEfficientThreats(ThreatContext playingThreatContext, ThreatContext opponentThreatContext, CompoThreatType threatTryContext) {
		
		Map<Cell, Pair<Threat, List<Threat>>> efficientThreats = new HashMap<>();

		boolean isPlaying = threatTryContext.isPlaying();

		if (isPlaying) {
			Map<Cell, Pair<Threat, List<Threat>>> candidateMap = findCandidates(playingThreatContext, threatTryContext);
			for (Entry<Cell, Pair<Threat, List<Threat>>> entry : candidateMap.entrySet()) {

				Cell targetCell = entry.getKey();
				
				Pair<Threat, List<Threat>> threats = entry.getValue();

				Threat firstThreat = threats.getFirst();
				
				if (checkPlayingThreat(targetCell, firstThreat, opponentThreatContext)) {
					for (Threat otherThreat : threats.getSecond()) {
						if (otherThreat == null
							|| otherThreat.getThreatType() != firstThreat.getThreatType()
							|| checkPlayingThreat(targetCell, otherThreat, opponentThreatContext)) {
								efficientThreats.computeIfAbsent(targetCell, k -> new Pair<>(firstThreat, new ArrayList<>())).getSecond().add(otherThreat);
						}
					}
				}
			}
		} else {
			Map<Cell, Pair<Threat, List<Threat>>> candidateMap = findCandidates(opponentThreatContext, threatTryContext);

//			Map<Cell, List<Pair<Threat>>> separatedCandidates = filterOnSeparateThreats(candidateMap);
			
//			for (Entry<Cell, List<Pair<Threat>>> entry : candidateMap.entrySet()) {
//
//				Cell targetCell = entry.getKey();
//				List<Pair<Threat>> threatPairs = entry.getValue();
//
//				for (Pair<Threat> threatPair : threatPairs) {
//					
//					if (checkThreatPair(threatPair, playingThreatContext, opponentThreatContext)) {
//						
//					}
//					
//					if (checkOpponentThreat(targetCell, threatPair.getFirst(), playingThreatContext, opponentThreatContext)
//							&& (threatPair.getSecond() == null
//									|| threatPair.getSecond().getThreatType() != threatPair.getFirst().getThreatType()
//									|| checkOpponentThreat(targetCell, threatPair.getSecond(), playingThreatContext, opponentThreatContext))) {
//						efficientThreats.computeIfAbsent(targetCell, k -> new ArrayList<>()).add(threatPair);
//					}
//				}
//			}
		}

		return efficientThreats;
	}

	@Override
	public Map<Cell, Pair<Threat, List<Threat>>> findCompositeThreats(ThreatContext context, CompoThreatType threatTryContext) {
		return findCandidates(context, threatTryContext);
	}

//	private boolean checkThreatPair(Pair<Threat> threatPair, ThreatContext playingThreatContext, ThreatContext opponentThreatContext) {
//		return false;
//	}

//	private Map<Cell, List<Pair<Threat>>> filterOnSeparateThreats(Map<Cell, List<Pair<Threat>>> candidateMap) {
//		
//		 Map<Cell, List<Pair<Threat>>> filteredCandidates = new HashMap<>();
//		
//		for (Entry<Cell, List<Pair<Threat>>> entry1 : candidateMap.entrySet()) {
//			
//			Cell firstTargetCell = entry1.getKey();
//			List<Pair<Threat>> firstThreatPairs = entry1.getValue();
//			
//			for (Pair<Threat> firstThreatPair : firstThreatPairs) {
//				
//				Set<Cell> firstKillingMoves = new HashSet<>();
//				
//				firstKillingMoves.addAll(firstThreatPair.getFirst().getKillingCells());
//				if (firstThreatPair.getSecond() != null) {
//					firstKillingMoves.addAll(firstThreatPair.getSecond().getKillingCells());
//				}
//
//				for (Entry<Cell, List<Pair<Threat>>> entry2 : candidateMap.entrySet()) {
//					
//					Cell secondTargetCell = entry2.getKey();
//					List<Pair<Threat>> secondThreatPairs = entry2.getValue();
//					
//					for (Pair<Threat> secondThreatPair : secondThreatPairs) {
//						
//						Set<Cell> secondKillingMoves = new HashSet<>();
//						secondKillingMoves.addAll(secondThreatPair.getFirst().getKillingCells());
//						if (firstThreatPair.getSecond() != null) {
//							secondKillingMoves.addAll(secondThreatPair.getSecond().getKillingCells());
//						}
//
//						if (firstKillingMoves.stream().noneMatch(secondKillingMoves::contains)) {
//							filteredCandidates.computeIfAbsent(firstTargetCell, k -> new ArrayList<>())
//									.addAll(firstThreatPairs);
//							filteredCandidates.computeIfAbsent(secondTargetCell, k -> new ArrayList<>())
//									.addAll(secondThreatPairs);
//						}
//						
//					}
//				}
//			}
//		}
//		
//		return filteredCandidates;
//	}

	private boolean checkOpponentThreat(Cell targetCell, Threat threat, ThreatContext playingThreatContext, ThreatContext opponentThreatContext) {
		
		if (!playingThreatContext.getThreatTypeToThreatMap().get(ThreatType.THREAT_5).isEmpty()) {
			return false;
		}

		if (opponentThreatContext.getThreatTypeToThreatMap().get(ThreatType.THREAT_5).size() > 1) {
			return true;
		}
		
		if (!opponentThreatContext.getThreatTypeToThreatMap().get(ThreatType.THREAT_5).isEmpty()
				&& !opponentThreatContext.getThreatTypeToThreatMap().get(ThreatType.THREAT_5).contains(threat)) {
			return true;
		}
		
		if (!ThreatType.THREAT_5.equals(threat.getThreatType())) {
			
			if (!playingThreatContext.getDoubleThreatTypeToThreatMap().get(ThreatType.DOUBLE_THREAT_4).isEmpty()) {
				return false;
			}
			
			if (!findEfficientThreats(playingThreatContext, opponentThreatContext, new CompoThreatType(ThreatType.THREAT_4, ThreatType.THREAT_4, true)).isEmpty()) {
				return false;
			}
			
			if (!findEfficientThreats(playingThreatContext, opponentThreatContext, new CompoThreatType(ThreatType.THREAT_4, ThreatType.DOUBLE_THREAT_3, true)).isEmpty()) {
				return false;
			}
			
			if (threat.getThreatType().equals(ThreatType.THREAT_4) || threat.getThreatType().equals(ThreatType.DOUBLE_THREAT_4)) {
				return true;
			}
			
			if (!opponentThreatContext.getDoubleThreatTypeToThreatMap().get(ThreatType.DOUBLE_THREAT_4).isEmpty()
					&& !opponentThreatContext.getDoubleThreatTypeToThreatMap().get(ThreatType.DOUBLE_THREAT_4).contains(threat)) {
				return true;
			}
			
			
			if (threat.getThreatType() == ThreatType.DOUBLE_THREAT_3) {
				if (!findEfficientThreats(playingThreatContext, opponentThreatContext, new CompoThreatType(ThreatType.DOUBLE_THREAT_3, ThreatType.DOUBLE_THREAT_3, true)).isEmpty()) {
					return false;
				}
			}
		}

		return false;
	}

	private boolean checkPlayingThreat(Cell targetCell, Threat threat, ThreatContext opponentThreatContext) {

		if (threat.getThreatType() == ThreatType.THREAT_5) {
			return true;
		}
		
		if (!opponentThreatContext.getThreatTypeToThreatMap().get(ThreatType.THREAT_5).isEmpty()
				&& numberOfCellThreats(opponentThreatContext, targetCell, ThreatType.THREAT_5) == 0) {
			return false;
		}
		
		if (threat.getThreatType() == ThreatType.THREAT_4) {
			
			Cell counterCell = threat.getEmptyCells().stream().filter(c -> !c.equals(targetCell)).findFirst().orElseThrow();
			
			if (numberOfCellThreats(opponentThreatContext, counterCell, ThreatType.THREAT_4) > 0) {
				return false;
			}
		} else if (threat.getThreatType() == ThreatType.DOUBLE_THREAT_3) {
			
			Set<Cell> counterCells = ((DoubleThreat) threat).getBlockingCells();
			
			for (Cell counterCell : counterCells) {
				if (numberOfCellThreats(opponentThreatContext, counterCell, ThreatType.THREAT_4) > 0) {
					return false;
				}
				if (numberOfCellThreats(opponentThreatContext, counterCell, ThreatType.DOUBLE_THREAT_3) > 0) {
					return false;
				}
			}
		}

		return true;
	}

	private int numberOfCellThreats(ThreatContext threatContext, Cell cell, ThreatType type) {
		
		if (threatContext.getCellToThreatMap().get(cell) != null
				&& threatContext.getCellToThreatMap().get(cell).get(type) != null) {
			return threatContext.getCellToThreatMap().get(cell).get(type).size();
		}
		
		return 0;
		
	}

	private Map<Cell, Pair<Threat, List<Threat>>> findCandidates(ThreatContext context, CompoThreatType threatTryContext) {
		
		Map<Cell, Pair<Threat, List<Threat>>> candidateMap = new HashMap<>();
		
		Set<Threat> visitedThreats = new HashSet<>();
		
		if (threatTryContext.getThreatType2() == null) {
			if (threatTryContext.getThreatType1().isDoubleType()) {
				for (DoubleThreat threat : context.getDoubleThreatTypeToThreatMap().get(threatTryContext.getThreatType1())) {
					candidateMap.computeIfAbsent(threat.getTargetCell(), key -> new Pair<>(threat, new ArrayList<>()));
				}
			} else {
				for (Threat threat : context.getThreatTypeToThreatMap().get(threatTryContext.getThreatType1())) {
					
					for (Cell emptyCell : threat.getEmptyCells()) {
						candidateMap.computeIfAbsent(emptyCell, key -> new Pair<>(threat, new ArrayList<>()));
					}
				}
			}
		} else {
			if (threatTryContext.getThreatType1().isDoubleType()) {
				for (DoubleThreat threat1 : context.getDoubleThreatTypeToThreatMap().get(threatTryContext.getThreatType1())) {
					visitedThreats.add(threat1);
					if (threatTryContext.getThreatType2().isDoubleType()) {
						for (DoubleThreat threat2 : context.getDoubleThreatTypeToThreatMap().get(threatTryContext.getThreatType2())) {
							if (!visitedThreats.contains(threat2) && threat1.getTargetCell().equals(threat2.getTargetCell()) && !areAligned(threat1, threat2)) {
								candidateMap.computeIfAbsent(threat1.getTargetCell(), key -> new Pair<>(threat1, new ArrayList<>())).getSecond().add(threat2);
							}
						}
					}
				}
			} else {
				for (Threat threat1 : context.getThreatTypeToThreatMap().get(threatTryContext.getThreatType1())) {
					visitedThreats.add(threat1);
					if (threatTryContext.getThreatType2().isDoubleType()) {
						for (DoubleThreat threat2 : context.getDoubleThreatTypeToThreatMap().get(threatTryContext.getThreatType2())) {
							if (!visitedThreats.contains(threat2) && threat1.getEmptyCells().contains(threat2.getTargetCell()) && !areAligned(threat1, threat2)) {
								candidateMap.computeIfAbsent(threat2.getTargetCell(), key -> new Pair<>(threat1, new ArrayList<>())).getSecond().add(threat2);
							}
						}
					} else {
						for (Threat threat2 : context.getThreatTypeToThreatMap().get(threatTryContext.getThreatType2())) {
							if (!visitedThreats.contains(threat2) && !areAligned(threat1, threat2)) {
								for (Cell emptyCell : threat2.getEmptyCells()) {
									if (threat1.getEmptyCells().contains(emptyCell)) {
										candidateMap.computeIfAbsent(emptyCell, key -> new Pair<>(threat1, new ArrayList<>())).getSecond().add(threat2);
									}
								}
							}
						}
					}
				}
			}
		}
		
		return candidateMap;
	}
	
	private boolean areAligned(Threat threat1, Threat threat2) {
		return threat1.getPlainCells().stream().filter(threat2.getPlainCells()::contains).count() > 0;
	}

	private boolean hasDT3Counter(Cell blockingCell, ThreatContext opponentThreatContext) {
		return opponentThreatContext.getDoubleThreatTypeToThreatMap().get(ThreatType.DOUBLE_THREAT_3).stream().anyMatch(t -> t.getTargetCell().equals(blockingCell));
	}

	private boolean hasT4Counter(Cell blockingCell, ThreatContext opponentThreatContext) {
		return opponentThreatContext.getThreatTypeToThreatMap().get(ThreatType.THREAT_4).stream().anyMatch(t -> t.getEmptyCells().contains(blockingCell));
	}

	private boolean hasT5Counter(Cell playingCell, ThreatContext opponentThreatContext) {
		return opponentThreatContext.getThreatTypeToThreatMap().get(ThreatType.THREAT_5).stream().anyMatch(t -> !t.getEmptyCells().contains(playingCell));
	}

	private void internalComputeThreatContext(ThreatContext threatContext) {
		
		int dataLength = threatContext.getData().length;
		
		computeHorizontalThreats(threatContext, dataLength);

		computeVerticalThreats(threatContext, dataLength);

		computeDiagonal1Threats(threatContext, dataLength);

		computeDiagonal2Threats(threatContext, dataLength);
		
		computeDoubleThreats(threatContext, ThreatType.THREAT_4);
		
		computeDoubleThreats(threatContext, ThreatType.THREAT_3);
		
		computeDoubleThreats(threatContext, ThreatType.THREAT_2);
	}

	private void computeDoubleThreats(ThreatContext threatContext, ThreatType threatType) {
		Set<Threat> visitedThreats = new HashSet<>();
		
		for (Threat threat : threatContext.getThreatTypeToThreatMap().get(threatType)) {
			
			if (!visitedThreats.contains(threat)) {
				List<Threat> similarThreats = threatContext.getThreatTypeToThreatMap().get(threatType).stream().filter(t -> !t.equals(threat) && t.getPlainCells().containsAll(threat.getPlainCells())).toList();
				
				similarThreats = new ArrayList<>(similarThreats);
				
				similarThreats.add(threat);
				
				visitedThreats.addAll(similarThreats);
				
				Set<DoubleThreat> doubleThreats = createDoubleThreats(similarThreats);
				
				doubleThreats.stream().forEach(t -> t.setThreatType(threatType.getDoubleThreatType()));
				
				threatContext.getDoubleThreatTypeToThreatMap().get(threatType.getDoubleThreatType()).addAll(doubleThreats);
				
				doubleThreats.stream().forEach(t -> threatContext.getCellToThreatMap().computeIfAbsent(t.getTargetCell(), key -> new EnumMap<>(ThreatType.class)).computeIfAbsent(threatType.getDoubleThreatType(), k -> new ArrayList<>()).add(t));
			}
		}
	}

	private Set<DoubleThreat> createDoubleThreats(List<Threat> threats) {
		
		Set<DoubleThreat> doubleThreats = new HashSet<>();
		
		Set<Cell> emptyCells = new HashSet<>();
		
		threats.stream().forEach(t -> emptyCells.addAll(t.getEmptyCells()));
		
		for (Cell emptyCell : emptyCells) {
			List<Threat> threatsContaining = threats.stream().filter(t -> t.getEmptyCells().contains(emptyCell)).toList();
			
			if (threatsContaining.size() >= 2) {
				doubleThreats.add(createDoubleThreat(emptyCell, threatsContaining));
			}
		}
		
		return doubleThreats;
	}

	private DoubleThreat createDoubleThreat(Cell emptyCell, List<Threat> threatsContaining) {
		DoubleThreat doubleThreat = new DoubleThreat();
		
		doubleThreat.setTargetCell(emptyCell);
		
		doubleThreat.setPlainCells(threatsContaining.get(0).getPlainCells());
		
		Set<Cell> blockingCells = new HashSet<>();
		
		threatsContaining.forEach(t -> t.getEmptyCells().stream().filter(c -> !c.equals(emptyCell) && threatsContaining.stream().filter(t2 -> t2.getEmptyCells().contains(c)).count() >= threatsContaining.size() - 1).forEach(blockingCells::add));
		
		doubleThreat.setBlockingCells(blockingCells);
		
		doubleThreat.getKillingCells().add(emptyCell);
		doubleThreat.getKillingCells().addAll(blockingCells);
		
		return doubleThreat;
	}

	private void computeDiagonal2Threats(ThreatContext threatContext, int dataLength) {
		for (int col = 0; col < dataLength; col++) {
			int[][] diagonal2Stripe = new int[dataLength - col][2];
			for (int row = dataLength - 1; row >= col; row--) {
				diagonal2Stripe[dataLength - 1 - row][0] = col - row + dataLength - 1;
				diagonal2Stripe[dataLength - 1 - row][1] = row;
			}
			computeStripeThreats(threatContext, diagonal2Stripe);
		}

		for (int row = dataLength - 2; row >= 0; row--) {
			int[][] diagonal2Stripe = new int[row + 1][2];
			for (int col = 0; col <= row; col++) {
				diagonal2Stripe[col][0] = col;
				diagonal2Stripe[col][1] = row - col;
			}
			computeStripeThreats(threatContext, diagonal2Stripe);
		}
	}

	private void computeDiagonal1Threats(ThreatContext threatContext, int dataLength) {
		for (int row = 0; row < dataLength; row++) {
			int[][] diagonal1Stripe = new int[dataLength - row][2];
			for (int col = 0; col < dataLength - row; col++) {
				diagonal1Stripe[col][0] = col;
				diagonal1Stripe[col][1] = row + col;
			}
			computeStripeThreats(threatContext, diagonal1Stripe);
		}

		for (int col = 1; col < dataLength; col++) {
			int[][] diagonal1Stripe = new int[dataLength - col][2];
			for (int row = 0; row < dataLength - col; row++) {
				diagonal1Stripe[row][0] = col + row;
				diagonal1Stripe[row][1] = row;
			}
			computeStripeThreats(threatContext, diagonal1Stripe);
		}
	}

	private void computeVerticalThreats(ThreatContext threatContext, int dataLength) {
		for (int col = 0; col < dataLength; col++) {
			int[][] verticalStripe = new int[dataLength][2];
			for (int row = 0; row < dataLength; row++) {
				verticalStripe[row][0] = col;
				verticalStripe[row][1] = row;
			}
			computeStripeThreats(threatContext, verticalStripe);
		}
	}

	private void computeHorizontalThreats(ThreatContext threatContext, int dataLength) {
		for (int row = 0; row < dataLength; row++) {
			int[][] horizontalStripe = new int[dataLength][2];
			for (int col = 0; col < dataLength; col++) {
				horizontalStripe[col][0] = col;
				horizontalStripe[col][1] = row;
			}
			computeStripeThreats(threatContext, horizontalStripe);
		}
	}
	
	private void computeStripeThreats(ThreatContext threatContext, int[][] stripe) {
		
		int[][] data = threatContext.getData();
		
		int playingColor = threatContext.getPlayingColor();
		
		int anchorIndex = 0;
		
		while (anchorIndex < stripe.length - 4) {
			anchorIndex = computeThreat(stripe, data, playingColor, threatContext, anchorIndex);
		}
	}

	private int computeThreat(int[][] stripe, int[][] data, int playingColor, ThreatContext threatContext, int anchorIndex) {
		Set<Cell> plainCells = new HashSet<>();
		Set<Cell> emptyCells = new HashSet<>();
		
		for (int h = 0; h < 5; h++) {
			
			int columnIndex = stripe[anchorIndex + h][0];
			int rowIndex = stripe[anchorIndex + h][1];
			int value = data[columnIndex][rowIndex];
			
			if (value == playingColor) {
				plainCells.add(new Cell(columnIndex, rowIndex));
			} else if (value == EngineConstants.NONE_COLOR) {
				emptyCells.add(new Cell(columnIndex, rowIndex));
			} else if (value == -playingColor) {
				break;
			}
		}
		
		if (!plainCells.isEmpty() && !emptyCells.isEmpty() && plainCells.size() + emptyCells.size() == 5) {
			computeThreat(threatContext, plainCells, emptyCells);
		}
		
		anchorIndex++;
		return anchorIndex;
	}

	private void computeThreat(ThreatContext threatContext, Set<Cell> plainCells, Set<Cell> emptyCells) {
		ThreatType threatType = ThreatType.valueOf(plainCells.size() + 1);
		
		Threat newThreat = new Threat(plainCells, emptyCells, threatType);
			
		for (Cell emptyCell : emptyCells) {
			Map<ThreatType, List<Threat>> cellThreatMap = threatContext.getCellToThreatMap().computeIfAbsent(emptyCell, key -> new HashMap<>());
			
			List<Threat> threatList = cellThreatMap.computeIfAbsent(threatType, key -> new ArrayList<>());
			
			threatList.add(newThreat);
			
		}
		
		threatContext.getThreatTypeToThreatMap().get(threatType).add(newThreat);
	}
}
