package fr.leblanc.gomoku.engine.model;

import java.util.HashMap;
import java.util.Map;

public class EvaluationResult {

	private double evaluation;
	
	private Map<CompoThreatType, Double> evaluationMap = new HashMap<>();

	public double getEvaluation() {
		return evaluation;
	}

	public void setEvaluation(double evaluation) {
		this.evaluation = evaluation;
	}

	public Map<CompoThreatType, Double> getEvaluationMap() {
		return evaluationMap;
	}

	public void setEvaluationMap(Map<CompoThreatType, Double> evaluationMap) {
		this.evaluationMap = evaluationMap;
	}
	
}
