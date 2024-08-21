package fr.leblanc.gomoku.engine.model;

import java.util.HashMap;
import java.util.Map;

public class EvaluationResult {

	private double evaluation;
	
	private Map<CompoThreatType, Double> evaluationMap = new HashMap<>();
	
	private Map<Cell, Double> cellEvaluationMap = new HashMap<>();

	public double getEvaluation() {
		return evaluation;
	}

	public void setEvaluation(double evaluation) {
		this.evaluation = evaluation;
	}
	
	public Map<CompoThreatType, Double> getEvaluationMap() {
		return evaluationMap;
	}
	
	public Map<Cell, Double> getCellEvaluationMap() {
		return cellEvaluationMap;
	}

	@Override
	public String toString() {
		return "EvaluationResult [evaluation=" + evaluation + "]";
	}
	
}
