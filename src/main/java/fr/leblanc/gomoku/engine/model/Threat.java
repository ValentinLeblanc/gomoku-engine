package fr.leblanc.gomoku.engine.model;

import java.util.List;

import lombok.Data;

@Data
public class Threat {

	private List<Cell> plainCells;
	private List<Cell> emptyCells;
	
	public Threat(List<Cell> plainCells, List<Cell> emptyCells) {
		this.plainCells = plainCells;
		this.emptyCells = emptyCells;
	}

}
