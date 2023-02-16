package fr.leblanc.gomoku.engine.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MinMaxResult {
	
	private double evaluation;
	
	private Map<Integer, Cell> optimalMoves = new HashMap<>();

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
		return Objects.hash(evaluation, optimalMoves);
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
				&& Objects.equals(optimalMoves, other.optimalMoves);
	}

	@Override
	public String toString() {
		return "MinMaxResult [evaluation=" + evaluation + ", optimalMoves=" + optimalMoves + "]";
	}

}
