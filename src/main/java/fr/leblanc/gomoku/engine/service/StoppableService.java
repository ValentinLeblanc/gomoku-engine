package fr.leblanc.gomoku.engine.service;

public interface StoppableService {

	boolean isComputing(Long id);
	
	void stopComputation(Long id);
	
}
