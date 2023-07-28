package fr.leblanc.gomoku.engine.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MinMaxResult {
	
	private double evaluation;
	private Double finalEvaluation;
	
	public static final MinMaxResult EMPTY_RESULT = new MinMaxResult();
	
	private Map<Integer, Cell> optimalMoves = new HashMap<>();
	
	public Cell getResultCell() {
		return optimalMoves.get(0);
	}

	public Double getFinalEvaluation() {
		return finalEvaluation;
	}

	public void setFinalEvaluation(Double finalEvaluation) {
		this.finalEvaluation = finalEvaluation;
	}

	public double getEvaluation() {
		return evaluation;
	}

	public void setEvaluation(double evaluation) {
		this.evaluation = evaluation;
	}

	public Map<Integer, Cell> getOptimalMoves() {
		return optimalMoves;
	}

	public void setOptimalMoves(Map<Integer, Cell> optimalMoves) {
		this.optimalMoves = optimalMoves;
	}

	@Override
	public int hashCode() {
		return Objects.hash(evaluation, finalEvaluation, optimalMoves);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MinMaxResult other = (MinMaxResult) obj;
		return Double.doubleToLongBits(evaluation) == Double.doubleToLongBits(other.evaluation)
				&& Objects.equals(finalEvaluation, other.finalEvaluation)
				&& Objects.equals(optimalMoves, other.optimalMoves);
	}

	@Override
	public String toString() {
		return "MinMaxResult [evaluation=" + evaluation + ", finalEvaluation=" + finalEvaluation + ", optimalMoves="
				+ optimalMoves + "]";
	}


}
