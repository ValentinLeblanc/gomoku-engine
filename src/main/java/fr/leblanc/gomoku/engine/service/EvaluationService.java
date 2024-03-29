package fr.leblanc.gomoku.engine.service;

import fr.leblanc.gomoku.engine.model.EvaluationContext;
import fr.leblanc.gomoku.engine.model.EvaluationResult;

public interface EvaluationService {
	
	public static final int WIN_EVALUATION = 100000;
	public static final int STRIKE_EVALUATION = 22222;
	public static final int THREAT_5_POTENTIAL = 2000;
	public static final int DOUBLE_THREAT_4_POTENTIAL = 500;
	public static final int THREAT_4_DOUBLE_THREAT_3_POTENTIAL = 250;
	public static final int DOUBLE_THREAT_3_DOUBLE_THREAT_3_POTENTIAL = 100;
	public static final int THREAT_4_POTENTIAL = 20;
	public static final int THREAT_4_DOUBLE_THREAT_2_POTENTIAL = 15;
	public static final int DOUBLE_THREAT_3_DOUBLE_THREAT_2_POTENTIAL = 10;
	public static final int DOUBLE_THREAT_3_THREAT_3_POTENTIAL = 10;
	public static final int DOUBLE_THREAT_3_POTENTIAL = 4;
	public static final int DOUBLE_THREAT_2_DOUBLE_THREAT_2_POTENTIAL = 4;
	public static final int THREAT_3_POTENTIAL = 2;
	public static final int DOUBLE_THREAT_2_POTENTIAL = 2;
	public static final int THREAT_2_POTENTIAL = 1;

	EvaluationResult computeEvaluation(Long gameId, EvaluationContext context) throws InterruptedException;
}
