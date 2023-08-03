package fr.leblanc.gomoku.engine.service;

import fr.leblanc.gomoku.engine.model.GameData;
import fr.leblanc.gomoku.engine.model.EvaluationContext;
import fr.leblanc.gomoku.engine.model.EvaluationResult;

public interface EvaluationService {
	
	EvaluationResult computeEvaluation(Long gameId, GameData dataWrapper) throws InterruptedException;

	EvaluationResult computeEvaluation(Long gameId, EvaluationContext context) throws InterruptedException;
}
