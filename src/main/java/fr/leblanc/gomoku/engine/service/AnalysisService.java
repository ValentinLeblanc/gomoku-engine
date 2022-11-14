package fr.leblanc.gomoku.engine.service;

import fr.leblanc.gomoku.engine.model.Cell;

public interface AnalysisService {

	public void sendAnalysisCell(Cell analysisCell, int color);
	
	void sendPercentCompleted(int index, int percent);

}
