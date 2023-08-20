package fr.leblanc.gomoku.engine.model;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

public class ThreatContext {

	private Map<Cell, Map<ThreatType, List<Threat>>> cellToThreatMap = new HashMap<>();
	private Map<ThreatType, List<Threat>> threatTypeToThreatMap = new EnumMap<>(ThreatType.class);
	private Map<Cell, List<Threat>> blockingCellsToThreatMap = new HashMap<>();
	private Map<Cell, List<Threat>> plainCellsToThreatMap = new HashMap<>();
	
	private int[][] data;
	private int color;

	public ThreatContext(int[][] data, int color) {
		this.data = data;
		this.color = color;
		for (ThreatType threatType : ThreatType.values()) {
			threatTypeToThreatMap.put(threatType, new ArrayList<>());
		}
		threatTypeToThreatMap.put(ThreatType.THREAT_5, new ArrayList<>());
		threatTypeToThreatMap.put(ThreatType.THREAT_4, new ArrayList<>());
		threatTypeToThreatMap.put(ThreatType.THREAT_3, new ArrayList<>());
		threatTypeToThreatMap.put(ThreatType.THREAT_2, new ArrayList<>());
		threatTypeToThreatMap.put(ThreatType.DOUBLE_THREAT_4, new ArrayList<>());
		threatTypeToThreatMap.put(ThreatType.DOUBLE_THREAT_3, new ArrayList<>());
		threatTypeToThreatMap.put(ThreatType.DOUBLE_THREAT_2, new ArrayList<>());
		
		compute();
	}
	
	public Map<ThreatType, List<Threat>> getThreatsOfCell(Cell cell) {
		return cellToThreatMap.computeIfAbsent(cell, k -> {
			EnumMap<ThreatType, List<Threat>> enumMap = new EnumMap<>(ThreatType.class);
			for (ThreatType threatType : ThreatType.values()) {
				enumMap.put(threatType, new ArrayList<>());
			}
			return enumMap;
		});
	}
	
	public List<Threat> getThreatsOfType(ThreatType threatType) {
		return threatTypeToThreatMap.get(threatType);
	}

	public void addMoveUpdate(Cell cell, int color) {
		
		data[cell.getColumn()][cell.getRow()] = color;
		
		clearEmptyCellMapping(cell);
		
		computeHorizontalThreats(cell.getColumn(), cell.getRow());
		computeVerticalThreats(cell.getColumn(), cell.getRow());
		computeDiagonal1Threats(cell.getColumn(), cell.getRow());
		computeDiagonal2Threats(cell.getColumn(), cell.getRow());
		computeDoubleThreats(ThreatType.THREAT_4);
		computeDoubleThreats(ThreatType.THREAT_3);
		computeDoubleThreats(ThreatType.THREAT_2);
	}
	
	public void removeMoveUpdate(Cell cell) {
		
		data[cell.getColumn()][cell.getRow()] = GomokuColor.NONE_COLOR;
		
		clearPlainCellMapping(cell);
		
		computeHorizontalThreats(cell.getColumn(), cell.getRow());
		computeVerticalThreats(cell.getColumn(), cell.getRow());
		computeDiagonal1Threats(cell.getColumn(), cell.getRow());
		computeDiagonal2Threats(cell.getColumn(), cell.getRow());
		computeDoubleThreats(ThreatType.THREAT_4);
		computeDoubleThreats(ThreatType.THREAT_3);
		computeDoubleThreats(ThreatType.THREAT_2);
	}

	private void clearPlainCellMapping(Cell cell) {
		List<Threat> toRemove = new ArrayList<>();
		
		for (Threat threat : getPlainCellThreats(cell)) {
			threatTypeToThreatMap.get(threat.getThreatType()).remove(threat);
			toRemove.add(threat);
		}
		
		for (Threat threat : toRemove) {
			cellToThreatMap.get(threat.getTargetCell()).get(threat.getThreatType()).remove(threat);
		}
		
		getBlockingCellThreats(cell).clear();
		
	}
	
	private void clearEmptyCellMapping(Cell cell) {
		List<Threat> toRemove = new ArrayList<>();
		
		for (Threat threat : getBlockingCellThreats(cell)) {
			threatTypeToThreatMap.get(threat.getThreatType()).remove(threat);
			toRemove.add(threat);
		}
		
		for (Entry<ThreatType, List<Threat>> entry : getThreatsOfCell(cell).entrySet()) {
			for (Threat threat : entry.getValue()) {
				threatTypeToThreatMap.get(threat.getThreatType()).remove(threat);
				toRemove.add(threat);
			}
		}
		
		for (Threat threat : toRemove) {
			cellToThreatMap.get(threat.getTargetCell()).get(threat.getThreatType()).remove(threat);
		}
		
		getBlockingCellThreats(cell).clear();
		
	}

	private void computeHorizontalThreats(int centerX, int centerY) {
	    int minX = Math.max(0, centerX - 4);
	    int maxX = Math.min(data.length - 1, centerX + 4);
	    for (int row = centerY; row >= 0 && row < data.length; row++) {
	        int[][] horizontalStripe = new int[maxX - minX + 1][2];
	        for (int col = minX; col <= maxX; col++) {
	            horizontalStripe[col - minX][0] = col;
	            horizontalStripe[col - minX][1] = row;
	        }
	        computeStripeThreats(horizontalStripe);
	    }
	}

	private void computeVerticalThreats(int centerX, int centerY) {
	    int minY = Math.max(0, centerY - 4);
	    int maxY = Math.min(data.length - 1, centerY + 4);
	    for (int col = centerX; col >= 0 && col < data.length; col++) {
	        int[][] verticalStripe = new int[maxY - minY + 1][2];
	        for (int row = minY; row <= maxY; row++) {
	            verticalStripe[row - minY][0] = col;
	            verticalStripe[row - minY][1] = row;
	        }
	        computeStripeThreats(verticalStripe);
	    }
	}

	private void computeDiagonal1Threats(int centerX, int centerY) {
	    int minX = Math.max(0, centerX - 4);
	    int maxX = Math.min(data.length - 1, centerX + 4);
	    int minY = Math.max(0, centerY - 4);
	    int maxY = Math.min(data.length - 1, centerY + 4);
	    
	    for (int row = centerY, col = centerX; row >= minY && col >= minX; row--, col--) {
	        int[][] diagonal1Stripe = new int[Math.min(maxX - col, maxY - row) + 1][2];
	        for (int i = 0; i < diagonal1Stripe.length; i++) {
	            diagonal1Stripe[i][0] = col + i;
	            diagonal1Stripe[i][1] = row + i;
	        }
	        computeStripeThreats(diagonal1Stripe);
	    }
	}

	private void computeDiagonal2Threats(int centerX, int centerY) {
	    int minX = Math.max(0, centerX - 4);
	    int maxX = Math.min(data.length - 1, centerX + 4);
	    int minY = Math.max(0, centerY - 4);
	    int maxY = Math.min(data.length - 1, centerY + 4);

	    for (int row = centerY, col = centerX; row >= minY && col <= maxX; row--, col++) {
	        int[][] diagonal2Stripe = new int[Math.min(col - minX, maxY - row) + 1][2];
	        for (int i = 0; i < diagonal2Stripe.length; i++) {
	            diagonal2Stripe[i][0] = col - i;
	            diagonal2Stripe[i][1] = row + i;
	        }
	        computeStripeThreats(diagonal2Stripe);
	    }
	}

	
	private void compute() {
		computeHorizontalThreats();
		computeVerticalThreats();
		computeDiagonal1Threats();
		computeDiagonal2Threats();
		computeDoubleThreats(ThreatType.THREAT_4);
		computeDoubleThreats(ThreatType.THREAT_3);
		computeDoubleThreats(ThreatType.THREAT_2);
	}

	private void computeHorizontalThreats() {
		for (int row = 0; row < data.length; row++) {
			int[][] horizontalStripe = new int[data.length][2];
			for (int col = 0; col < data.length; col++) {
				horizontalStripe[col][0] = col;
				horizontalStripe[col][1] = row;
			}
			computeStripeThreats(horizontalStripe);
		}
	}

	private void computeVerticalThreats() {
		for (int col = 0; col < data.length; col++) {
			int[][] verticalStripe = new int[data.length][2];
			for (int row = 0; row < data.length; row++) {
				verticalStripe[row][0] = col;
				verticalStripe[row][1] = row;
			}
			computeStripeThreats(verticalStripe);
		}
	}

	private void computeDiagonal1Threats() {
		for (int row = 0; row < data.length; row++) {
			int[][] diagonal1Stripe = new int[data.length - row][2];
			for (int col = 0; col < data.length - row; col++) {
				diagonal1Stripe[col][0] = col;
				diagonal1Stripe[col][1] = row + col;
			}
			computeStripeThreats(diagonal1Stripe);
		}
	
		for (int col = 1; col < data.length; col++) {
			int[][] diagonal1Stripe = new int[data.length - col][2];
			for (int row = 0; row < data.length - col; row++) {
				diagonal1Stripe[row][0] = col + row;
				diagonal1Stripe[row][1] = row;
			}
			computeStripeThreats(diagonal1Stripe);
		}
	}

	private void computeDiagonal2Threats() {
		for (int col = 0; col < data.length; col++) {
			int[][] diagonal2Stripe = new int[data.length - col][2];
			for (int row = data.length - 1; row >= col; row--) {
				diagonal2Stripe[data.length - 1 - row][0] = col - row + data.length - 1;
				diagonal2Stripe[data.length - 1 - row][1] = row;
			}
			computeStripeThreats(diagonal2Stripe);
		}
	
		for (int row = data.length - 2; row >= 0; row--) {
			int[][] diagonal2Stripe = new int[row + 1][2];
			for (int col = 0; col <= row; col++) {
				diagonal2Stripe[col][0] = col;
				diagonal2Stripe[col][1] = row - col;
			}
			computeStripeThreats(diagonal2Stripe);
		}
	}

	private void computeStripeThreats(int[][] stripe) {
		int anchorIndex = 0;
		while (anchorIndex < stripe.length - 4) {
			anchorIndex = computeThreat(stripe, data, color, anchorIndex);
		}
	}

	private int computeThreat(int[][] stripe, int[][] data, int playingColor, int anchorIndex) {
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
			computeThreat(plainCells, emptyCells);
		}
		
		anchorIndex++;
		return anchorIndex;
	}

	private void computeThreat(Set<Cell> plainCells, Set<Cell> emptyCells) {
		ThreatType threatType = ThreatType.valueOf(plainCells.size() + 1);
		
		for (Cell emptyCell : emptyCells) {
			Set<Cell> blockingCells = emptyCells.stream().filter(c -> !c.equals(emptyCell)).collect(Collectors.toSet());
			Threat newThreat = new Threat(emptyCell, plainCells, blockingCells, threatType);
			addNewThreat(newThreat);
		}
	}

	private void computeDoubleThreats(ThreatType threatType) {
		Set<Threat> visitedThreats = new HashSet<>();
		for (Threat threat : getThreatsOfType(threatType)) {
			if (!visitedThreats.contains(threat)) {
				List<Threat> sameTargetCellThreats = getThreatsOfCell(threat.getTargetCell()).get(threatType).stream().filter(t -> t.getPlainCells().containsAll(threat.getPlainCells())).toList();
				visitedThreats.addAll(sameTargetCellThreats);
				if (sameTargetCellThreats.size() > 1) {
					Set<Threat> doubleThreats = createDoubleThreats(sameTargetCellThreats);
					doubleThreats.stream().forEach(this::addNewThreat);
				}
			}
		}
	}

	private Set<Threat> createDoubleThreats(List<Threat> threats) {
		Set<Threat> doubleThreats = new HashSet<>();
		Set<Cell> targetCells = new HashSet<>();
		threats.stream().map(Threat::getTargetCell).forEach(targetCells::add);
		for (Cell targetCell : targetCells) {
			List<Threat> threatsContainingTargetCell = threats.stream().filter(t -> t.getKillingCells().contains(targetCell)).toList();
			if (threatsContainingTargetCell.size() >= 2) {
				doubleThreats.add(createDoubleThreat(targetCell, threatsContainingTargetCell));
			}
		}
		return doubleThreats;
	}

	private Threat createDoubleThreat(Cell targetCell, List<Threat> threatsContaining) {
		Set<Cell> blockingCells = new HashSet<>();
		threatsContaining.forEach(t -> t.getBlockingCells().stream().filter(c -> threatsContaining.stream().filter(t2 -> t2.getKillingCells().contains(c)).count() >= threatsContaining.size() - 1).forEach(blockingCells::add));
		return new Threat(targetCell, threatsContaining.get(0).getPlainCells(), blockingCells, threatsContaining.get(0).getThreatType().getDoubleThreatType());
	}

	private void addNewThreat(Threat threat) {
		if (!getThreatsOfCell(threat.getTargetCell()).get(threat.getThreatType()).contains(threat)) {
			getThreatsOfCell(threat.getTargetCell()).get(threat.getThreatType()).add(threat);
		}
		if (!getThreatsOfType(threat.getThreatType()).contains(threat)) {
			getThreatsOfType(threat.getThreatType()).add(threat);
		}
		
		for (Cell blockingCell : threat.getBlockingCells()) {
			if (!getBlockingCellThreats(blockingCell).contains(threat)) {
				getBlockingCellThreats(blockingCell).add(threat);
			}
		}
		for (Cell plainCell : threat.getPlainCells()) {
			if (!getPlainCellThreats(plainCell).contains(threat)) {
				getPlainCellThreats(plainCell).add(threat);
			}
		}
	}

	private List<Threat> getBlockingCellThreats(Cell blockingCell) {
		return blockingCellsToThreatMap.computeIfAbsent(blockingCell, k -> new ArrayList<>());
	}
	
	private List<Threat> getPlainCellThreats(Cell blockingCell) {
		return plainCellsToThreatMap.computeIfAbsent(blockingCell, k -> new ArrayList<>());
	}
	
}