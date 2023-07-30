package fr.leblanc.gomoku.engine.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
public class ComputationStoppedException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ComputationStoppedException() {
		super();
	}
	
	public ComputationStoppedException(String message) {
		super(message);
	}
	
	public ComputationStoppedException(Exception e) {
		super(e);
	}
}
