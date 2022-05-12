package fr.leblanc.gomoku.engine.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import fr.leblanc.gomoku.engine.model.CheckWinResult;
import fr.leblanc.gomoku.engine.model.GameDto;
import fr.leblanc.gomoku.engine.model.MoveDto;
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
	
}
