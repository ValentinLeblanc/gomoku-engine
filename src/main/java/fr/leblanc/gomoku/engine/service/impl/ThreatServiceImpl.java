package fr.leblanc.gomoku.engine.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
	public Set<Cell> findCombinedThreats(ThreatContext threatContext, ThreatType threatType1, ThreatType threatType2) {
		Set<Cell> combinedThreats = new HashSet<>();
		for (Threat threat1 : threatContext.getThreatsOfType(threatType1)) {
			for (Threat threat2 : threatContext.getThreatsOfCell(threat1.getTargetCell()).get(threatType2)) {
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

		ThreatContext playingThreatContext = gameData.computeThreatContext(color);
		ThreatContext opponentThreatContext = gameData.computeThreatContext(-color);
		
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
