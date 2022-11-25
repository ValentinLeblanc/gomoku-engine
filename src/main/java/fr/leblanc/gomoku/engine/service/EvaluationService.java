package fr.leblanc.gomoku.engine.service;

import fr.leblanc.gomoku.engine.model.DataWrapper;
import fr.leblanc.gomoku.engine.model.EngineSettings;

public interface EvaluationService {
	double computeEvaluation(DataWrapper dataWrapper, int playingColor, EngineSettings engineSettings);
}
