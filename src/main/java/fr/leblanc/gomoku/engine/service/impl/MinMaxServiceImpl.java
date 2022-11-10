package fr.leblanc.gomoku.engine.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.CustomProperties;
import fr.leblanc.gomoku.engine.model.DataWrapper;
import fr.leblanc.gomoku.engine.model.EngineConstants;
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
			analyzedMoves = threatContextService.buildAnalyzedMoves(dataWrapper, playingColor);
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
			
			List<Cell> subAnalysedMoves = threatContextService.buildAnalyzedMoves(dataWrapper, -playingColor);
			
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
	
}
