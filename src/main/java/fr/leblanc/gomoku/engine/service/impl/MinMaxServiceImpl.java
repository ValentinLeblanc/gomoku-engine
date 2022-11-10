package fr.leblanc.gomoku.engine.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.CustomProperties;
import fr.leblanc.gomoku.engine.model.DataWrapper;
import fr.leblanc.gomoku.engine.model.DoubleThreat;
import fr.leblanc.gomoku.engine.model.EngineConstants;
import fr.leblanc.gomoku.engine.model.Threat;
import fr.leblanc.gomoku.engine.model.ThreatContext;
import fr.leblanc.gomoku.engine.model.ThreatType;
import fr.leblanc.gomoku.engine.service.AnalysisService;
import fr.leblanc.gomoku.engine.service.EvaluationService;
import fr.leblanc.gomoku.engine.service.MinMaxService;
import fr.leblanc.gomoku.engine.service.ThreatContextService;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class MinMaxServiceImpl implements MinMaxService {
	
	@Autowired
	private ThreatContextService threatContextService;
	
	@Autowired
	private EvaluationService evaluationService;

	@Autowired
	private CustomProperties customProperties;
	
	@Autowired
	private AnalysisService analysisService;
	
	private Boolean stopComputation = false;
	
	@Override
	public Cell computeMinMax(DataWrapper dataWrapper, int playingColor, List<Cell> analyzedMoves) {
		
		if (analyzedMoves == null) {
			analyzedMoves = buildAnalyzedMoves(dataWrapper, playingColor);
		}

		try {
			List<Cell> miniMaxResult = internalComputeMinMax(dataWrapper, playingColor, analyzedMoves);
			if (!miniMaxResult.isEmpty()) {
				return new Cell(miniMaxResult.get(0).getColumnIndex(), miniMaxResult.get(0).getRowIndex());
			}
		} catch (Exception e) {
			log.error("Error while computing min/max : " + e.getMessage(), e);
		}

		return null;
	}
	
	@Override
	public void stopComputation() {
		stopComputation = true;
	}

	private List<Cell> internalComputeMinMax(DataWrapper dataWrapper, int playingColor, List<Cell> analysedMoves) throws Exception {

		if (analysedMoves.size() == 1) {
			return analysedMoves;
		}
		
		List<Cell> optimalMoves = new ArrayList<>();

		double maxEvaluation = Double.NEGATIVE_INFINITY;

		int advancement = 0;
		
		Cell bestMove = null;
		Cell bestOpponentMove = null;

		for (Cell analysedMove : analysedMoves) {

			if (stopComputation) {
				stopComputation = false;
				throw new InterruptedException();
			}
			
			dataWrapper.addMove(analysedMove, playingColor);

			if (customProperties.isDisplayAnalysis()) {
				analysisService.sendAnalysisCell(analysedMove, playingColor);
			}
			
			List<Cell> subAnalysedMoves = buildAnalyzedMoves(dataWrapper, -playingColor);
			
			double minEvaluation = Double.POSITIVE_INFINITY;
			
			Cell opponentMove = null;
			
			for (Cell subAnalysedMove : subAnalysedMoves) {
				
				if (stopComputation) {
					stopComputation = false;
					throw new InterruptedException();
				}
				
				dataWrapper.addMove(subAnalysedMove, -playingColor);

				double evaluation = evaluationService.computeEvaluation(dataWrapper, playingColor);
				
				dataWrapper.removeMove(subAnalysedMove);
				
				if (evaluation < minEvaluation) {
					minEvaluation = evaluation;
					opponentMove = subAnalysedMove;
					if (minEvaluation <= maxEvaluation) {
						break;
					}
				}
				
			}
			
			dataWrapper.removeMove(analysedMove);
			
			if (customProperties.isDisplayAnalysis()) {
				analysisService.sendAnalysisCell(analysedMove, EngineConstants.NONE_COLOR);
			}
			
			if (minEvaluation > maxEvaluation) {
				maxEvaluation = minEvaluation;
				bestMove = analysedMove;
				bestOpponentMove = opponentMove;
			}
			
			advancement++;

			Integer percentCompleted = advancement * 100 / analysedMoves.size();
			
			if (customProperties.isDisplayAnalysis()) {
				analysisService.sendPercentCompleted(percentCompleted);
			}

		}
		
		optimalMoves.add(bestMove);
		optimalMoves.add(bestOpponentMove);
		
		if (log.isDebugEnabled()) {
			log.debug("bestMove : " + bestMove);
			log.debug("opponentMove : " + bestOpponentMove);
		}

		return optimalMoves;
	}
	
	private List<Cell> buildAnalyzedMoves(DataWrapper dataWrapper, int color) {

		List<Cell> analysedMoves = new ArrayList<>();

		ThreatContext threatContext = threatContextService.computeThreatContext(dataWrapper.getData(), color);
		ThreatContext opponentThreatContext = threatContextService.computeThreatContext(dataWrapper.getData(), -color);
		
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

		for (int i = 0; i < dataWrapper.getData().length; i++) {
			for (int j = 0; j < dataWrapper.getData().length; j++) {
				if (dataWrapper.getValue(i, j) == EngineConstants.NONE_COLOR) {
					Cell cell = new Cell(i, j);
					if (!analysedMoves.contains(cell)) {
						analysedMoves.add(cell);
					}
				}
			}
		}

		return analysedMoves;
	}
}
