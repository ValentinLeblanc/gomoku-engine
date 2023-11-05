package fr.leblanc.gomoku.engine.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

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
	
	@Override
	public ThreatContext getOrUpdateThreatContext(GameData gameData, int color) {
		ThreatContext threatContext = new ThreatContext(gameData.getData(), color);
		compute(threatContext);
		gameData.putThreatContext(color, threatContext);
		return gameData.getThreatContext(color);
	}
	
	private void compute(ThreatContext threatContext) {
		computeHorizontalThreats(threatContext);
		computeVerticalThreats(threatContext);
		computeDiagonal1Threats(threatContext);
		computeDiagonal2Threats(threatContext);
		computeDoubleThreats(threatContext, ThreatType.THREAT_4);
		computeDoubleThreats(threatContext, ThreatType.THREAT_3);
		computeDoubleThreats(threatContext, ThreatType.THREAT_2);
	}

	private void computeHorizontalThreats(ThreatContext threatContext) {
		for (int row = 0; row < threatContext.getData().length; row++) {
			int[][] horizontalStripe = new int[threatContext.getData().length][2];
			for (int col = 0; col < threatContext.getData().length; col++) {
				horizontalStripe[col][0] = col;
				horizontalStripe[col][1] = row;
			}
			computeStripeThreats(threatContext, horizontalStripe);
		}
	}

	private void computeVerticalThreats(ThreatContext threatContext) {
		for (int col = 0; col < threatContext.getData().length; col++) {
			int[][] verticalStripe = new int[threatContext.getData().length][2];
			for (int row = 0; row < threatContext.getData().length; row++) {
				verticalStripe[row][0] = col;
				verticalStripe[row][1] = row;
			}
			computeStripeThreats(threatContext, verticalStripe);
		}
	}

	private void computeDiagonal1Threats(ThreatContext threatContext) {
		for (int row = 0; row < threatContext.getData().length; row++) {
			int[][] diagonal1Stripe = new int[threatContext.getData().length - row][2];
			for (int col = 0; col < threatContext.getData().length - row; col++) {
				diagonal1Stripe[col][0] = col;
				diagonal1Stripe[col][1] = row + col;
			}
			computeStripeThreats(threatContext, diagonal1Stripe);
		}
	
		for (int col = 1; col < threatContext.getData().length; col++) {
			int[][] diagonal1Stripe = new int[threatContext.getData().length - col][2];
			for (int row = 0; row < threatContext.getData().length - col; row++) {
				diagonal1Stripe[row][0] = col + row;
				diagonal1Stripe[row][1] = row;
			}
			computeStripeThreats(threatContext, diagonal1Stripe);
		}
	}

	private void computeDiagonal2Threats(ThreatContext threatContext) {
		for (int col = 0; col < threatContext.getData().length; col++) {
			int[][] diagonal2Stripe = new int[threatContext.getData().length - col][2];
			for (int row = threatContext.getData().length - 1; row >= col; row--) {
				diagonal2Stripe[threatContext.getData().length - 1 - row][0] = col - row + threatContext.getData().length - 1;
				diagonal2Stripe[threatContext.getData().length - 1 - row][1] = row;
			}
			computeStripeThreats(threatContext, diagonal2Stripe);
		}
	
		for (int row = threatContext.getData().length - 2; row >= 0; row--) {
			int[][] diagonal2Stripe = new int[row + 1][2];
			for (int col = 0; col <= row; col++) {
				diagonal2Stripe[col][0] = col;
				diagonal2Stripe[col][1] = row - col;
			}
			computeStripeThreats(threatContext, diagonal2Stripe);
		}
	}

	private void computeStripeThreats(ThreatContext threatContext, int[][] stripe) {
		int anchorIndex = 0;
		while (anchorIndex < stripe.length - 4) {
			anchorIndex = computeThreat(threatContext, stripe, anchorIndex);
		}
	}

	private int computeThreat(ThreatContext threatContext, int[][] stripe, int anchorIndex) {
		Set<Cell> plainCells = new HashSet<>();
		Set<Cell> emptyCells = new HashSet<>();
		
		for (int h = 0; h < 5; h++) {
			
			int columnIndex = stripe[anchorIndex + h][0];
			int rowIndex = stripe[anchorIndex + h][1];
			int value = threatContext.getData()[columnIndex][rowIndex];
			
			if (value == threatContext.getColor()) {
				plainCells.add(new Cell(columnIndex, rowIndex));
			} else if (value == GomokuColor.NONE_COLOR) {
				emptyCells.add(new Cell(columnIndex, rowIndex));
			} else if (value == -threatContext.getColor()) {
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
			addNewThreat(threatContext, newThreat);
		}
	}

	private void computeDoubleThreats(ThreatContext threatContext, ThreatType threatType) {
		Set<Threat> visitedThreats = new HashSet<>();
		Set<Threat> toRemove = new HashSet<>();
		for (Threat threat : threatContext.getThreatsOfType(threatType)) {
			if (!visitedThreats.contains(threat)) {
				List<Threat> sameTargetCellThreats = threatContext.getThreatsOfTargetCell(threat.getTargetCell()).get(threatType).stream().filter(t -> t.getPlainCells().containsAll(threat.getPlainCells())).toList();
				if (sameTargetCellThreats.size() > 1) {
					Set<Threat> doubleThreats = createDoubleThreats(threatContext, sameTargetCellThreats);
					doubleThreats.stream().forEach(t -> addNewThreat(threatContext, t));
					if (!doubleThreats.isEmpty()) {
						visitedThreats.addAll(sameTargetCellThreats);
						toRemove.addAll(sameTargetCellThreats);
					}
				}
			}
		}
	}

	private Set<Threat> createDoubleThreats(ThreatContext threatContext, List<Threat> threats) {
		Set<Threat> doubleThreats = new HashSet<>();
		Cell targetCell = threats.get(0).getTargetCell();
		List<Threat> threatsContainingTargetCell = threats.stream().filter(t -> t.getKillingCells().contains(targetCell)).toList();
		if (threatsContainingTargetCell.size() >= 2) {
			doubleThreats.add(createDoubleThreat(threatContext, targetCell, threatsContainingTargetCell));
		}
		return doubleThreats;
	}

	private Threat createDoubleThreat(ThreatContext threatContext, Cell targetCell, List<Threat> threatsContaining) {
		Set<Cell> blockingCells = new HashSet<>();
		threatsContaining.forEach(t -> t.getBlockingCells().stream().filter(c -> threatsContaining.stream().filter(t2 -> t2.getKillingCells().contains(c)).count() >= threatsContaining.size() - 1).forEach(blockingCells::add));
		return new Threat(targetCell, threatsContaining.get(0).getPlainCells(), blockingCells, threatsContaining.get(0).getThreatType().getDoubleThreatType());
	}

	private void addNewThreat(ThreatContext threatContext, Threat threat) {
		if (!threatContext.getThreatsOfTargetCell(threat.getTargetCell()).get(threat.getThreatType()).contains(threat)) {
			threatContext.getThreatsOfTargetCell(threat.getTargetCell()).get(threat.getThreatType()).add(threat);
		}
		if (!threatContext.getThreatsOfType(threat.getThreatType()).contains(threat)) {
			threatContext.getThreatsOfType(threat.getThreatType()).add(threat);
		}
		
		for (Cell blockingCell : threat.getBlockingCells()) {
			if (!threatContext.getBlockingCellThreats(blockingCell).contains(threat)) {
				threatContext.getBlockingCellThreats(blockingCell).add(threat);
			}
		}
		for (Cell plainCell : threat.getPlainCells()) {
			if (!threatContext.getPlainCellThreats(plainCell).contains(threat)) {
				threatContext.getPlainCellThreats(plainCell).add(threat);
			}
		}
	}
		
	@Override
	public Set<Cell> findCombinedThreats(ThreatContext threatContext, ThreatType threatType1, ThreatType threatType2) {
		Set<Cell> combinedThreats = new HashSet<>();
		for (Threat threat1 : threatContext.getThreatsOfType(threatType1)) {
			for (Threat threat2 : threatContext.getThreatsOfTargetCell(threat1.getTargetCell()).get(threatType2)) {
				if (!areAligned(threat1, threat2)) {
					combinedThreats.add(threat1.getTargetCell());
				}
			}
		}
		return combinedThreats;
	}
	
	@Override
	public List<Cell> buildAnalyzedCells(GameData gameData, int color) {

		List<Cell> analysedMoves = new ArrayList<>();

		ThreatContext playingThreatContext = getOrUpdateThreatContext(gameData, color);
		ThreatContext opponentThreatContext = getOrUpdateThreatContext(gameData, -color);
		
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
		
		for (int i = 0; i < gameData.getData().length; i++) {
			for (int j = 0; j < gameData.getData().length; j++) {
				if (gameData.getValue(i, j) == GomokuColor.NONE_COLOR) {
					Cell cell = new Cell(i, j);
					if (!analysedMoves.contains(cell)) {
						notPlayedMoves.add(cell);
					}
				}
			}
		}
		
		notPlayedMoves.sort((c1, c2) -> {
			int radius1 = Math.abs(c1.getColumn() - gameData.getData().length / 2) + Math.abs(c1.getRow() - gameData.getData().length / 2);
			int radius2 = Math.abs(c2.getColumn() - gameData.getData().length / 2) + Math.abs(c2.getRow() - gameData.getData().length / 2);
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
	
}
