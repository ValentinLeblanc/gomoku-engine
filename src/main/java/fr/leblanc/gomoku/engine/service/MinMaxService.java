package fr.leblanc.gomoku.engine.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.DataWrapper;
import fr.leblanc.gomoku.engine.model.EngineConstants;
import fr.leblanc.gomoku.engine.model.ThreatType;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class MinMaxService {
	
	@Autowired
	private ThreatContextService threatContextService;
	
	@Autowired
	private EvaluationService evaluationService;

	public Cell computeMinMax(DataWrapper dataWrapper, int playingColor, Set<Cell> analysedMoves) {
		
		if (analysedMoves == null) {
			analysedMoves = sortEmptyCells(dataWrapper, playingColor);
		}

		try {
			List<Cell> miniMaxResult = internalComputeMinMax(dataWrapper, playingColor, analysedMoves);
			if (!miniMaxResult.isEmpty()) {
				return new Cell(miniMaxResult.get(0).getColumnIndex(), miniMaxResult.get(0).getRowIndex());
			}
		} catch (Exception e) {
			log.error("Error while computing min/max : " + e.getMessage(), e);
		}

		return null;
	}
	
	private List<Cell> internalComputeMinMax(DataWrapper dataWrapper, int playingColor, Set<Cell> analysedMoves) throws Exception {

		List<Cell> optimalMoves = new ArrayList<>();

		double maxEvaluation = Double.NEGATIVE_INFINITY;

		int percentCompleted = 0;
		
		Cell bestMove = null;
		Cell bestOpponentMove = null;

		for (Cell analysedMove : analysedMoves) {

			dataWrapper.addMove(analysedMove, playingColor);

			Set<Cell> subAnalysedMoves = sortEmptyCells(dataWrapper, -playingColor);
			
			double minEvaluation = Double.POSITIVE_INFINITY;
			
			Cell opponentMove = null;
			
			for (Cell subAnalysedMove : subAnalysedMoves) {
				dataWrapper.addMove(subAnalysedMove, -playingColor);

				double evaluation = evaluationService.computeEvaluation(dataWrapper, playingColor);
				
				dataWrapper.removeMove(subAnalysedMove);
				
				if (evaluation < minEvaluation) {
					minEvaluation = evaluation;
					opponentMove = subAnalysedMove;
					
					if (minEvaluation < maxEvaluation) {
						break;
					}
				}
				
			}
			
			dataWrapper.removeMove(analysedMove);
			
			if (minEvaluation > maxEvaluation) {
				maxEvaluation = minEvaluation;
				bestMove = analysedMove;
				bestOpponentMove = opponentMove;
			}

			percentCompleted++;

			if (log.isDebugEnabled()) {
				log.debug("analysis : " + percentCompleted * 100 / analysedMoves.size() + " %");
			}

		}
		
		optimalMoves.add(bestMove);
		optimalMoves.add(bestOpponentMove);

		return optimalMoves;
	}
	
	private Set<Cell> sortEmptyCells(DataWrapper dataWrapper, int color) {

		Set<Cell> analysedMoves = new LinkedHashSet<>();

		Map<ThreatType, Set<Cell>> threatMap = threatContextService.computeThreatContext(dataWrapper.getData(), color).getThreatToCellMap();

		analysedMoves.addAll(threatMap.get(ThreatType.THREAT_5));
		analysedMoves.addAll(threatMap.get(ThreatType.DOUBLE_THREAT_4));
		analysedMoves.addAll(threatMap.get(ThreatType.THREAT_4));
		analysedMoves.addAll(threatMap.get(ThreatType.DOUBLE_THREAT_3));
		analysedMoves.addAll(threatMap.get(ThreatType.THREAT_3));
		analysedMoves.addAll(threatMap.get(ThreatType.DOUBLE_THREAT_2));
		analysedMoves.addAll(threatMap.get(ThreatType.THREAT_2));

		for (int i = 0; i < dataWrapper.getData().length; i++) {
			for (int j = 0; j < dataWrapper.getData().length; j++) {
				if (dataWrapper.getValue(i, j) == EngineConstants.NONE_COLOR) {
					analysedMoves.add(new Cell(i, j));
				}
			}
		}

		return analysedMoves;
	}
}
