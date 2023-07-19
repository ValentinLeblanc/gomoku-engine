package fr.leblanc.gomoku.engine.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import fr.leblanc.gomoku.engine.model.CheckWinResult;
import fr.leblanc.gomoku.engine.model.DataWrapper;
import fr.leblanc.gomoku.engine.model.EngineConstants;
import fr.leblanc.gomoku.engine.model.messaging.GameDto;
import fr.leblanc.gomoku.engine.model.messaging.MoveDto;
import fr.leblanc.gomoku.engine.service.CheckWinService;
import fr.leblanc.gomoku.engine.service.EngineService;
import fr.leblanc.gomoku.engine.service.EvaluationService;
import fr.leblanc.gomoku.engine.util.GameHelper;

@RestController
public class EngineController {

	@Autowired
	private EngineService engineService;
	
	@Autowired
	private CheckWinService checkWinService;
	
	@Autowired
	private EvaluationService evaluationService;

	@PostMapping("/checkWin")
	public CheckWinResult checkWin(@RequestBody GameDto game) {
		return checkWinService.checkWin(DataWrapper.of(game));
	}
	
	@PostMapping("/computeMove")
	public MoveDto computeMove(@RequestBody GameDto game) {
		return engineService.computeMove(game);
	}
	
	@PostMapping("/computeEvaluation")
	public Double computeEvaluation(@RequestBody GameDto game) {
		int playingColor = GameHelper.extractPlayingColor(game);
		
		DataWrapper dataWrapper = DataWrapper.of(game);
		
		if (playingColor == EngineConstants.BLACK_COLOR) {
			return evaluationService.computeEvaluation(dataWrapper, true).getEvaluation();
		} else if (playingColor == EngineConstants.WHITE_COLOR) {
			return -evaluationService.computeEvaluation(dataWrapper, true).getEvaluation();
		}
		
		throw new IllegalArgumentException("Game has no valid playing color");
	}
	
	@PostMapping("/stop")
	public void stopComputation() {
		engineService.stopComputation();
	}
	
}
