package fr.leblanc.gomoku.engine.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
public class EngineException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public EngineException(String message) {
		super(message);
	}
	
	public EngineException(Exception e) {
		super(e);
	}
}
