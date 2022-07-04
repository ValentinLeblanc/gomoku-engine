package fr.leblanc.gomoku.engine.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.springframework.stereotype.Service;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.EngineConstants;
import fr.leblanc.gomoku.engine.model.Threat;
import fr.leblanc.gomoku.engine.model.ThreatContext;
import fr.leblanc.gomoku.engine.model.ThreatType;

@Service
public class ThreatContextService {
	
	public ThreatContext computeThreatContext(int[][] data, int playingColor) {
		ThreatContext threatContext = new ThreatContext(data, playingColor);
		
		internalComputeThreatContext(threatContext);
		
		return threatContext;
	}
	
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
		
		Map<Cell, Map<ThreatType, List<Threat>>> cellToThreatMap = threatContext.getCellToThreatMap();
		
		Map<ThreatType, Set<Cell>> threatToCellMap = threatContext.getThreatToCellMap();
		
		int anchorIndex = 0;
		
		while (anchorIndex < stripe.length - 4) {
			anchorIndex = computeThreat(stripe, data, playingColor, cellToThreatMap, threatToCellMap, anchorIndex);
		}
	}

	private int computeThreat(int[][] stripe, int[][] data, int playingColor,
			Map<Cell, Map<ThreatType, List<Threat>>> cellToThreatMap, Map<ThreatType, Set<Cell>> threatToCellMap,
			int anchorIndex) {
		List<Cell> plainCells = new ArrayList<>();
		List<Cell> emptyCells = new ArrayList<>();
		
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
			computeThreat(cellToThreatMap, threatToCellMap, plainCells, emptyCells);
		}
		
		anchorIndex++;
		return anchorIndex;
	}

	private void computeThreat(Map<Cell, Map<ThreatType, List<Threat>>> cellToThreatMap,
			Map<ThreatType, Set<Cell>> threatToCellMap, List<Cell> plainCells, List<Cell> emptyCells) {
		Threat newThreat = new Threat(plainCells, emptyCells);
		
		for (Cell emptyCell : emptyCells) {
			Map<ThreatType, List<Threat>> cellThreatMap = cellToThreatMap.computeIfAbsent(emptyCell, key -> new HashMap<>());
			
			ThreatType threatType = ThreatType.valueOf(plainCells.size() + 1);
			
			List<Threat> threatList = cellThreatMap.computeIfAbsent(threatType, key -> new ArrayList<>());
			
			for (Threat threat : threatList) {
				if (threat.getPlainCells().containsAll(newThreat.getPlainCells())) {
					ThreatType doubleThreatType = ThreatType.valueDoubleOf(plainCells.size() + 1);
					cellThreatMap.computeIfAbsent(doubleThreatType, key -> new ArrayList<>()).add(threat);
					cellThreatMap.computeIfAbsent(doubleThreatType, key -> new ArrayList<>()).add(newThreat);
					threatToCellMap.get(doubleThreatType).add(emptyCell);
				}
			}
			
			threatList.add(newThreat);
			
			threatToCellMap.get(threatType).add(emptyCell);
		}
	}
}
