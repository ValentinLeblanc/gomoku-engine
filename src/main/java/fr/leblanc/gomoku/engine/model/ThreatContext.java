package fr.leblanc.gomoku.engine.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ThreatContext {

	private Map<Cell, Map<ThreatType, List<Threat>>> cellToThreatMap = new HashMap<>();
	private Map<ThreatType, List<Threat>> threatTypeToThreatMap = new EnumMap<>(ThreatType.class);
	private Map<Cell, List<Threat>> blockingCellsToThreatMap = new HashMap<>();
	
	private int[][] data;
	private int playingColor;

	public ThreatContext(int[][] data, int playingColor) {
		this.data = data;
		this.playingColor = playingColor;
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
	
	public int[][] getData() {
		return data;
	}

	public void setData(int[][] data) {
		this.data = data;
	}

	public int getPlayingColor() {
		return playingColor;
	}

	public void setPlayingColor(int playingColor) {
		this.playingColor = playingColor;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.deepHashCode(data);
		result = prime * result
				+ Objects.hash(cellToThreatMap, playingColor, threatTypeToThreatMap);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ThreatContext other = (ThreatContext) obj;
		return Objects.equals(cellToThreatMap, other.cellToThreatMap) && Arrays.deepEquals(data, other.data)
				&& playingColor == other.playingColor
				&& Objects.equals(threatTypeToThreatMap, other.threatTypeToThreatMap);
	}

	@Override
	public String toString() {
		return "ThreatContext [cellToThreatMap=" + cellToThreatMap + ", threatTypeToThreatMap=" + threatTypeToThreatMap
				+ ", data=" + Arrays.toString(data)
				+ ", playingColor=" + playingColor + "]";
	}

	public void addNewThreat(Threat threat) {
		getThreatsOfCell(threat.getTargetCell()).get(threat.getThreatType()).add(threat);
		getThreatsOfType(threat.getThreatType()).add(threat);
		for (Cell blockingCell : threat.getBlockingCells()) {
			blockingCellsToThreatMap.computeIfAbsent(blockingCell, k -> new ArrayList<>()).add(threat);
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
	
	private void computeStripeThreats(int[][] stripe) {
		int[][] data = getData();
		int playingColor = getPlayingColor();
		int anchorIndex = 0;
		while (anchorIndex < stripe.length - 4) {
			anchorIndex = computeThreat(stripe, data, playingColor, anchorIndex);
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

	public void update(Cell cell, int color) {
		
	}
	
}