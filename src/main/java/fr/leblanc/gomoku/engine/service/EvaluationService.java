package fr.leblanc.gomoku.engine.service;

import fr.leblanc.gomoku.engine.model.GameData;
import fr.leblanc.gomoku.engine.model.EvaluationResult;

public interface EvaluationService {
	
	EvaluationResult computeEvaluation(GameData dataWrapper);
	
	EvaluationResult computeEvaluation(GameData dataWrapper, boolean external);
}
