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
import fr.leblanc.gomoku.engine.model.DoubleThreat;
import fr.leblanc.gomoku.engine.model.EngineConstants;
import fr.leblanc.gomoku.engine.model.Threat;
import fr.leblanc.gomoku.engine.model.ThreatContext;
import fr.leblanc.gomoku.engine.model.ThreatType;
import fr.leblanc.gomoku.engine.service.ThreatContextService;

@Service
public class ThreatContextServiceImpl implements ThreatContextService {
	
	@Override
	public ThreatContext computeThreatContext(int[][] data, int playingColor) {
		ThreatContext threatContext = new ThreatContext(data, playingColor);
		
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

	private void internalComputeThreatContext(ThreatContext threatContext) {
		
		int dataLength = threatContext.getData().length;
		
		computeHorizontalThreats(threatContext, dataLength);

		computeVerticalThreats(threatContext, dataLength);

		computeDiagonal1Threats(threatContext, dataLength);

		computeDiagonal2Threats(threatContext, dataLength);
		
		computeDoubleThreats(threatContext, ThreatType.THREAT_4);
		
		computeDoubleThreats(threatContext, ThreatType.THREAT_3);
		
//		computeDoubleThreats(threatContext, ThreatType.THREAT_2);
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
			
			if (threatsContaining.size() == 2) {
				DoubleThreat doubleThreat = new DoubleThreat();
				
				doubleThreat.setTargetCell(emptyCell);
				
				doubleThreat.setPlainCells(threatsContaining.get(0).getPlainCells());
				
				Set<Cell> blockingCells = new HashSet<>();
				
				threatsContaining.forEach(t -> t.getEmptyCells().stream().filter(c -> !c.equals(emptyCell) && threatsContaining.stream().filter(t2 -> t2.getEmptyCells().contains(c)).count() > 0).forEach(blockingCells::add));
				
				doubleThreat.setBlockingCells(blockingCells);
				
				doubleThreats.add(doubleThreat);
			}
			if (threatsContaining.size() == 3) {
				DoubleThreat doubleThreat = new DoubleThreat();
				
				doubleThreat.setTargetCell(emptyCell);
				
				doubleThreat.setPlainCells(threatsContaining.get(0).getPlainCells());
				
				Set<Cell> blockingCells = new HashSet<>();
				
				threatsContaining.forEach(t -> t.getEmptyCells().stream().filter(c -> !c.equals(emptyCell) && threatsContaining.stream().filter(t2 -> t2.getEmptyCells().contains(c)).count() > 1).forEach(blockingCells::add));
				
				doubleThreat.setBlockingCells(blockingCells);
				
				doubleThreats.add(doubleThreat);
			}
		}
		
		return doubleThreats;
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
		Threat newThreat = new Threat(plainCells, emptyCells);
		
		ThreatType threatType = ThreatType.valueOf(plainCells.size() + 1);
			
		for (Cell emptyCell : emptyCells) {
			Map<ThreatType, List<Threat>> cellThreatMap = threatContext.getCellToThreatMap().computeIfAbsent(emptyCell, key -> new HashMap<>());
			
			List<Threat> threatList = cellThreatMap.computeIfAbsent(threatType, key -> new ArrayList<>());
			
			threatList.add(newThreat);
			
		}
		
		threatContext.getThreatTypeToThreatMap().get(threatType).add(newThreat);
	}
}
