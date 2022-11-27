package fr.leblanc.gomoku.engine.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import fr.leblanc.gomoku.engine.model.CheckWinResult;
import fr.leblanc.gomoku.engine.model.messaging.GameDto;
import fr.leblanc.gomoku.engine.model.messaging.MoveDto;
import fr.leblanc.gomoku.engine.service.EngineService;

@RestController
public class EngineController {

	@Autowired
	private EngineService engineService;
	
	@PostMapping("/checkWin")
	public CheckWinResult checkWin(@RequestBody GameDto game) {
		return engineService.checkWin(game);
	}
	
	@PostMapping("/computeMove")
	public MoveDto computeMove(@RequestBody GameDto game) {
		return engineService.computeMove(game);
	}
	
	@PostMapping("/computeEvaluation")
	public Double computeEvaluation(@RequestBody GameDto game) {
		return engineService.computeEvaluation(game);
	}
	
	@PostMapping("/stop")
	public void stopComputation() {
		engineService.stopComputation();
	}
	
}
