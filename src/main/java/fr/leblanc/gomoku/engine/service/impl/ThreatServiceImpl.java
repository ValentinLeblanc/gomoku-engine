package fr.leblanc.gomoku.engine.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.CompoThreatType;
import fr.leblanc.gomoku.engine.model.GameData;
import fr.leblanc.gomoku.engine.model.GomokuColor;
import fr.leblanc.gomoku.engine.model.Threat;
import fr.leblanc.gomoku.engine.model.ThreatContext;
import fr.leblanc.gomoku.engine.model.ThreatType;
import fr.leblanc.gomoku.engine.service.ThreatService;
import fr.leblanc.gomoku.engine.util.Pair;

@Service
public class ThreatServiceImpl implements ThreatService {
	
	private static final Logger logger = LoggerFactory.getLogger(ThreatServiceImpl.class);
	
	@Override
	public ThreatContext computeThreatContext(GameData dataWrapper, int playingColor) {
		
		StopWatch stopWatch = null;
		
		if (logger.isDebugEnabled()) {
			stopWatch = new StopWatch("computeThreatContext");
			stopWatch.start();
		}
		
		ThreatContext threatContext = new ThreatContext(dataWrapper.getData(), playingColor);
		internalComputeThreatContext(threatContext);
		
		if (logger.isDebugEnabled() && stopWatch != null) {
			stopWatch.stop();
			logger.debug("computeThreatContext elapsed time: {} ns", stopWatch.getLastTaskTimeNanos());
		}
		
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

		ThreatContext playingThreatContext = computeThreatContext(dataWrapper, color);
		ThreatContext opponentThreatContext = computeThreatContext(dataWrapper, -color);
		
		playingThreatContext.getThreatsOfType(ThreatType.THREAT_5).stream().map(Threat::getTargetCell).filter(c -> !analysedMoves.contains(c)).forEach(analysedMoves::add);
		opponentThreatContext.getThreatsOfType(ThreatType.THREAT_5).stream().map(Threat::getTargetCell).filter(c -> !analysedMoves.contains(c)).forEach(analysedMoves::add);
		
		playingThreatContext.getThreatsOfType(ThreatType.DOUBLE_THREAT_4).stream().map(Threat::getTargetCell).filter(c -> !analysedMoves.contains(c)).forEach(analysedMoves::add);
		opponentThreatContext.getThreatsOfType(ThreatType.DOUBLE_THREAT_4).stream().map(Threat::getTargetCell).filter(c -> !analysedMoves.contains(c)).forEach(analysedMoves::add);
		opponentThreatContext.getThreatsOfType(ThreatType.DOUBLE_THREAT_4).stream().forEach(t -> t.getBlockingCells().stream().filter(c -> !analysedMoves.contains(c)).forEach(analysedMoves::add));
		
		playingThreatContext.getThreatsOfType(ThreatType.THREAT_4).stream().map(Threat::getTargetCell).filter(c -> !analysedMoves.contains(c)).forEach(analysedMoves::add);
		opponentThreatContext.getThreatsOfType(ThreatType.THREAT_4).stream().map(Threat::getTargetCell).filter(c -> !analysedMoves.contains(c)).forEach(analysedMoves::add);
		opponentThreatContext.getThreatsOfType(ThreatType.THREAT_4).stream().forEach(t -> t.getBlockingCells().stream().filter(c -> !analysedMoves.contains(c)).forEach(analysedMoves::add));
		
		List<Cell> doubleThreat3Targets = playingThreatContext.getThreatsOfType(ThreatType.DOUBLE_THREAT_3).stream().map(Threat::getTargetCell).toList();
		doubleThreat3Targets.stream().filter(c -> playingThreatContext.getThreatsOfType(ThreatType.DOUBLE_THREAT_3).stream().filter(t -> t.getTargetCell().equals(c)).count() > 1).filter(c -> !analysedMoves.contains(c)).forEach(analysedMoves::add);
		
		playingThreatContext.getThreatsOfType(ThreatType.DOUBLE_THREAT_3).stream().map(Threat::getTargetCell).filter(c -> !analysedMoves.contains(c)).forEach(analysedMoves::add);
		opponentThreatContext.getThreatsOfType(ThreatType.DOUBLE_THREAT_3).stream().map(Threat::getTargetCell).filter(c -> !analysedMoves.contains(c)).forEach(analysedMoves::add);
		opponentThreatContext.getThreatsOfType(ThreatType.DOUBLE_THREAT_3).stream().forEach(t -> t.getBlockingCells().stream().filter(c -> !analysedMoves.contains(c)).forEach(analysedMoves::add));
		
		playingThreatContext.getThreatsOfType(ThreatType.THREAT_3).stream().map(Threat::getTargetCell).filter(c -> !analysedMoves.contains(c)).forEach(analysedMoves::add);
		playingThreatContext.getThreatsOfType(ThreatType.DOUBLE_THREAT_2).stream().map(Threat::getTargetCell).filter(c -> !analysedMoves.contains(c)).forEach(analysedMoves::add);
		playingThreatContext.getThreatsOfType(ThreatType.THREAT_2).stream().map(Threat::getTargetCell).filter(c -> !analysedMoves.contains(c)).forEach(analysedMoves::add);
		
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
	public List<Pair<Threat, Threat>> findCompositeThreats(ThreatContext threatContext, CompoThreatType threatTryContext) {
		
		List<Pair<Threat, Threat>> candidateMap = new ArrayList<>();
		
		Set<Threat> visitedThreats = new HashSet<>();
		
		for (Threat threat1 : threatContext.getThreatsOfType(threatTryContext.getThreatType1())) {
			if (threatTryContext.getThreatType2() == null) {
				candidateMap.add(new Pair<>(threat1, null));
			} else {
				visitedThreats.add(threat1);
				for (Threat threat2 : threatContext.getThreatsOfType(threatTryContext.getThreatType2())) {
					if (!visitedThreats.contains(threat2) && !areAligned(threat1, threat2) && threat1.getTargetCell().equals(threat2.getTargetCell())) {
						candidateMap.add(new Pair<>(threat1, threat2));
					}
				}
			}
		}
		
		return candidateMap;
	}
	
	private boolean areAligned(Threat threat1, Threat threat2) {
		if (threat1.getPlainCells().stream().filter(threat2.getPlainCells()::contains).count() > 0) {
			return true;
		}
		return threat1.getBlockingCells().stream().filter(threat2.getBlockingCells()::contains).count() > 0;
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
		for (Threat threat : threatContext.getThreatsOfType(threatType)) {
			if (!visitedThreats.contains(threat)) {
				List<Threat> sameTargetCellThreats = threatContext.getThreatsOfCell(threat.getTargetCell()).get(threatType).stream().filter(t -> t.getPlainCells().containsAll(threat.getPlainCells())).toList();
				visitedThreats.addAll(sameTargetCellThreats);
				
				if (sameTargetCellThreats.size() > 1) {
					Set<Threat> doubleThreats = createDoubleThreats(sameTargetCellThreats);
					threatContext.getThreatsOfType(threatType.getDoubleThreatType()).addAll(doubleThreats);
					doubleThreats.stream().forEach(t -> threatContext.getThreatsOfCell(t.getTargetCell()).get(threatType.getDoubleThreatType()).add(t));
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
		
		for (Cell emptyCell : emptyCells) {
			Set<Cell> blockingCells = emptyCells.stream().filter(c -> !c.equals(emptyCell)).collect(Collectors.toSet());
			Threat newThreat = new Threat(emptyCell, plainCells, blockingCells, threatType);
			threatContext.getThreatsOfCell(emptyCell).get(threatType).add(newThreat);
			threatContext.getThreatsOfType(threatType).add(newThreat);
		}
	}
}
