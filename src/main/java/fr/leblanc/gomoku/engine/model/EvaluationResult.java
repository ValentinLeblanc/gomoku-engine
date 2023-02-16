package fr.leblanc.gomoku.engine.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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

	@Override
	public int hashCode() {
		return Objects.hash(evaluation, evaluationMap);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EvaluationResult other = (EvaluationResult) obj;
		return Double.doubleToLongBits(evaluation) == Double.doubleToLongBits(other.evaluation)
				&& Objects.equals(evaluationMap, other.evaluationMap);
	}

	@Override
	public String toString() {
		return "EvaluationResult [evaluation=" + evaluation + ", evaluationMap=" + evaluationMap + "]";
	}
	
}
