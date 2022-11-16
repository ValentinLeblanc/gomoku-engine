package fr.leblanc.gomoku.engine.service;

public interface StoppableService {

	boolean isComputing();
	
	void stopComputation();
	
}
