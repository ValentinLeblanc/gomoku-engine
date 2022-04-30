package fr.leblanc.gomoku.engine.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import fr.leblanc.gomoku.engine.model.CheckWinResultDto;
import fr.leblanc.gomoku.engine.model.GameDto;
import fr.leblanc.gomoku.engine.service.EngineService;

@RestController
public class EngineController {

	@Autowired
	private EngineService engineService;

	@PostMapping("/checkWin")
	public CheckWinResultDto checkWin(@RequestBody GameDto game) {

		return engineService.checkWin(game);
		
	}

}
