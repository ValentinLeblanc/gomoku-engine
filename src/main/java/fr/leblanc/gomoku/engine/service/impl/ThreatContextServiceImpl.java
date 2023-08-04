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
import fr.leblanc.gomoku.engine.model.GameData;
import fr.leblanc.gomoku.engine.model.DoubleThreat;
import fr.leblanc.gomoku.engine.model.GomokuColor;
import fr.leblanc.gomoku.engine.model.Threat;
import fr.leblanc.gomoku.engine.model.ThreatContext;
import fr.leblanc.gomoku.engine.model.ThreatType;
import fr.leblanc.gomoku.engine.service.ThreatContextService;
import fr.leblanc.gomoku.engine.util.Pair;

@Service
public class ThreatContextServiceImpl implements ThreatContextService {
	
	@Override
	public ThreatContext computeThreatContext(GameData dataWrapper, int playingColor) {
		
		ThreatContext threatContext = new ThreatContext(dataWrapper.getData(), playingColor);
		
		threatContext.getThreatTypeToThreatMap().put(ThreatType.THREAT_5, new ArrayList<>());
		threatContext.getThreatTypeToThreatMap().put(ThreatType.THREAT_4, new ArrayList<>());
		threatContext.getThreatTypeToThreatMap().put(ThreatType.THREAT_3, new ArrayList<>());
		threatContext.getThreatTypeToThreatMap().put(ThreatType.THREAT_2, new ArrayList<>());
		
		threatContext.getDoubleThreatTypeToThreatMap().put(ThreatType.DOUBLE_THREAT_4, new HashSet<>());
		threatContext.getDoubleThreatTypeToThreatMap().put(ThreatType.DOUBLE_THREAT_3, new HashSet<>());
		threatContext.getDoubleThreatTypeToThreatMap().put(ThreatType.DOUBLE_THREAT_2, new HashSet<>());
		
		internalComputeThreatContext(threatContext);
		
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
	public List<Cell> buildAnalyzedCells(GameData dataWrapper, int color) {

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
		opponentDoubleThreatMap.get(ThreatType.DOUBLE_THREAT_4).stream().forEach(t -> t.getKillingCells().stream().filter(c -> !analysedMoves.contains(c)).forEach(analysedMoves::add));
		threatMap.get(ThreatType.THREAT_4).stream().forEach(t -> t.getEmptyCells().stream().filter(c -> !analysedMoves.contains(c)).forEach(analysedMoves::add));
		opponentThreatMap.get(ThreatType.THREAT_4).stream().forEach(t -> t.getEmptyCells().stream().filter(c -> !analysedMoves.contains(c)).forEach(analysedMoves::add));
		
		List<Cell> doubleThreat3Targets = doubleThreatMap.get(ThreatType.DOUBLE_THREAT_3).stream().map(DoubleThreat::getTargetCell).toList();
		doubleThreat3Targets.stream().filter(c -> doubleThreatMap.get(ThreatType.DOUBLE_THREAT_3).stream().filter(t -> t.getTargetCell().equals(c)).count() > 1).filter(c -> !analysedMoves.contains(c)).forEach(analysedMoves::add);
		
		doubleThreatMap.get(ThreatType.DOUBLE_THREAT_3).stream().filter(t -> !analysedMoves.contains(t.getTargetCell())).forEach(t -> analysedMoves.add(t.getTargetCell()));
		opponentDoubleThreatMap.get(ThreatType.DOUBLE_THREAT_3).stream().filter(t -> !analysedMoves.contains(t.getTargetCell())).forEach(t -> analysedMoves.add(t.getTargetCell()));
		opponentDoubleThreatMap.get(ThreatType.DOUBLE_THREAT_3).stream().forEach(t -> t.getKillingCells().stream().filter(c -> !analysedMoves.contains(c)).forEach(analysedMoves::add));
		
		threatMap.get(ThreatType.THREAT_3).stream().forEach(t -> t.getEmptyCells().stream().filter(c -> !analysedMoves.contains(c)).forEach(analysedMoves::add));
		doubleThreatMap.get(ThreatType.DOUBLE_THREAT_2).stream().filter(t -> !analysedMoves.contains(t.getTargetCell())).forEach(t -> analysedMoves.add(t.getTargetCell()));
		threatMap.get(ThreatType.THREAT_2).stream().forEach(t -> t.getEmptyCells().stream().filter(c -> !analysedMoves.contains(c)).forEach(analysedMoves::add));
		
		List<Cell> notPlayedMoves = new ArrayList<>();
		
		for (int i = 0; i < dataWrapper.getData().length; i++) {
			for (int j = 0; j < dataWrapper.getData().length; j++) {
				if (dataWrapper.getValue(i, j) == GomokuColor.NONE_COLOR) {
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
	public List<Pair<Threat, Threat>> findCompositeThreats(ThreatContext context, CompoThreatType threatTryContext) {
		
		List<Pair<Threat, Threat>> candidateMap = new ArrayList<>();
		
		Set<Threat> visitedThreats = new HashSet<>();
		
		if (threatTryContext.getThreatType2() == null) {
			if (threatTryContext.getThreatType1().isDoubleType()) {
				for (DoubleThreat threat : context.getDoubleThreatTypeToThreatMap().get(threatTryContext.getThreatType1())) {
					candidateMap.add(new Pair<>(threat, null));
				}
			} else {
				for (Threat threat : context.getThreatTypeToThreatMap().get(threatTryContext.getThreatType1())) {
					candidateMap.add(new Pair<>(threat, null));
				}
			}
		} else {
			if (threatTryContext.getThreatType1().isDoubleType()) {
				for (DoubleThreat threat1 : context.getDoubleThreatTypeToThreatMap().get(threatTryContext.getThreatType1())) {
					visitedThreats.add(threat1);
					if (threatTryContext.getThreatType2().isDoubleType()) {
						for (DoubleThreat threat2 : context.getDoubleThreatTypeToThreatMap().get(threatTryContext.getThreatType2())) {
							if (!visitedThreats.contains(threat2) && threat1.getTargetCell().equals(threat2.getTargetCell()) && !areAligned(threat1, threat2)) {
								candidateMap.add(new Pair<>(threat1, threat2));
							}
						}
					} else {
						for (Threat threat2 : context.getThreatTypeToThreatMap().get(threatTryContext.getThreatType2())) {
							if (!visitedThreats.contains(threat2) && !areAligned(threat1, threat2)) {
								for (Cell emptyCell : threat2.getEmptyCells()) {
									if (threat1.getEmptyCells().contains(emptyCell)) {
										candidateMap.add(new Pair<>(threat1, threat2));
									}
								}
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
								candidateMap.add(new Pair<>(threat1, threat2));
							}
						}
					} else {
						for (Threat threat2 : context.getThreatTypeToThreatMap().get(threatTryContext.getThreatType2())) {
							if (!visitedThreats.contains(threat2) && !areAligned(threat1, threat2)) {
								for (Cell emptyCell : threat2.getEmptyCells()) {
									if (threat1.getEmptyCells().contains(emptyCell)) {
										candidateMap.add(new Pair<>(threat1, threat2));
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
	
	private Cell retrieveThreatCell(Pair<Threat, Threat> threatPair) {
		
		if (threatPair.getFirst().getThreatType().isDoubleType()) {
			return ((DoubleThreat) threatPair.getFirst()).getTargetCell();
		}
		
		if (threatPair.getSecond() == null) {
			return threatPair.getFirst().getEmptyCells().iterator().next();
		}
		
		if (threatPair.getSecond().getThreatType().isDoubleType()) {
			return ((DoubleThreat) threatPair.getSecond()).getTargetCell();
		}
		
		return threatPair.getFirst().getEmptyCells().stream().filter(c -> threatPair.getSecond().getEmptyCells().contains(c)).findFirst().orElse(null);
		
	}
	
	private boolean areAligned(Threat threat1, Threat threat2) {
		
		if (threat1.getPlainCells().stream().filter(threat2.getPlainCells()::contains).count() > 0) {
			return true;
		}
		
		Cell commonCell = retrieveThreatCell(new Pair<>(threat1, threat2));
		
		return commonCell != null && threat1.getBlockingCells(commonCell).stream().filter(threat2.getBlockingCells(commonCell)::contains).count() > 0;
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
			} else if (value == GomokuColor.NONE_COLOR) {
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
