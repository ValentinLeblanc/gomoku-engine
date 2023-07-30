package fr.leblanc.gomoku.engine.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.CheckWinResult;
import fr.leblanc.gomoku.engine.model.GameData;
import fr.leblanc.gomoku.engine.model.EngineConstants;
import fr.leblanc.gomoku.engine.model.messaging.GameDTO;
import fr.leblanc.gomoku.engine.model.messaging.GameSettings;
import fr.leblanc.gomoku.engine.model.messaging.MoveDTO;
import fr.leblanc.gomoku.engine.service.CheckWinService;
import fr.leblanc.gomoku.engine.service.EngineService;
import fr.leblanc.gomoku.engine.service.EvaluationService;
import fr.leblanc.gomoku.engine.service.WebSocketService;

@RestController
public class EngineController {

	@Autowired
	private EngineService engineService;
	
	@Autowired
	private CheckWinService checkWinService;
	
	@Autowired
	private EvaluationService evaluationService;

	@Autowired
	private WebSocketService webSocketService;
	
	@GetMapping("isComputing/{gameId}")
	public Boolean isComputing(@PathVariable Long gameId) {
		return engineService.isComputing(gameId);
	}

	@PostMapping("/checkWin")
	public CheckWinResult checkWin(@RequestBody GameDTO gameDTO) {
		GameData gameData = GameData.of(gameDTO);
		return checkWinService.checkWin(gameData);
	}
	
	@PostMapping("/computeMove")
	public MoveDTO computeMove(@RequestBody GameDTO gameDTO) {
		
		GameData gameData = GameData.of(gameDTO);
		GameSettings gameSettings = gameDTO.getSettings();
		int playingColor = GameData.extractPlayingColor(gameData);
		
		try {
			webSocketService.sendIsComputing(gameDTO.getId(), true);
			webSocketService.sendComputingProgress(gameDTO.getId(), 0);
			Cell computedMove = engineService.computeMove(gameDTO.getId(), gameData, gameSettings);
			MoveDTO returnedMove = new MoveDTO(computedMove.getColumn(), computedMove.getRow(), playingColor);
			webSocketService.sendRefreshMove(gameDTO.getId(), returnedMove);
			return returnedMove;
		} finally {
			webSocketService.sendIsComputing(gameDTO.getId(), false);
		}
	}
	
	@PostMapping("/computeEvaluation")
	public Double computeEvaluation(@RequestBody GameDTO gameDTO) {
		GameData gameData = GameData.of(gameDTO);
		int playingColor = GameData.extractPlayingColor(gameData);
		
		if (playingColor == EngineConstants.BLACK_COLOR) {
			return evaluationService.computeEvaluation(gameData, true).getEvaluation();
		} else if (playingColor == EngineConstants.WHITE_COLOR) {
			return -evaluationService.computeEvaluation(gameData, true).getEvaluation();
		}
		
		throw new IllegalArgumentException("Game has no valid playing color");
	}
	
	@PostMapping("/stop/{gameId}")
	public void stopComputation(@PathVariable Long gameId) {
		engineService.stopComputation(gameId);
		webSocketService.sendComputingProgress(gameId, 0);
	}
	
}
