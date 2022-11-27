package fr.leblanc.gomoku.engine.service;

import fr.leblanc.gomoku.engine.model.DataWrapper;
import fr.leblanc.gomoku.engine.model.messaging.EngineSettingsDto;

public interface EvaluationService {
	double computeEvaluation(DataWrapper dataWrapper, int playingColor, EngineSettingsDto engineSettings);
}
