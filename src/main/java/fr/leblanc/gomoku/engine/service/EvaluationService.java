package fr.leblanc.gomoku.engine.service;

import fr.leblanc.gomoku.engine.model.DataWrapper;
import fr.leblanc.gomoku.engine.model.EvaluationResult;

public interface EvaluationService {
	
	EvaluationResult computeEvaluation(DataWrapper dataWrapper);
	
	EvaluationResult computeEvaluation(DataWrapper dataWrapper, boolean external);
}
