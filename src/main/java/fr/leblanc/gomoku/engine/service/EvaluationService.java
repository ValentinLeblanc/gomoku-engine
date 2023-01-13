package fr.leblanc.gomoku.engine.service;

import fr.leblanc.gomoku.engine.model.DataWrapper;

public interface EvaluationService {
	double computeEvaluation(DataWrapper dataWrapper);
}
