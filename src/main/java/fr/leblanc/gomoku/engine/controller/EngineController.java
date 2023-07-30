package fr.leblanc.gomoku.engine.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import fr.leblanc.gomoku.engine.exception.EngineException;
import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.CheckWinResult;
import fr.leblanc.gomoku.engine.model.EngineConstants;
import fr.leblanc.gomoku.engine.model.GameData;
import fr.leblanc.gomoku.engine.model.messaging.EngineMessageType;
import fr.leblanc.gomoku.engine.model.messaging.GameDTO;
import fr.leblanc.gomoku.engine.model.messaging.GameSettings;
import fr.leblanc.gomoku.engine.model.messaging.MoveDTO;
import fr.leblanc.gomoku.engine.service.CheckWinService;
import fr.leblanc.gomoku.engine.service.ComputationService;
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
	
	@Autowired
	private ComputationService computationService;
	
	@GetMapping("isComputing/{gameId}")
	public Boolean isComputing(@PathVariable Long gameId) {
		return computationService.isComputing(gameId);
	}

	@PostMapping("/checkWin")
	public CheckWinResult checkWin(@RequestBody GameDTO gameDTO) {
		GameData gameData = GameData.of(gameDTO);
		return checkWinService.checkWin(gameData);
	}
	
	@PostMapping("/computeMove")
	public MoveDTO computeMove(@RequestBody GameDTO gameDTO) {
		
		try {
			webSocketService.sendMessage(EngineMessageType.IS_COMPUTING, gameDTO.getId(), true);
			
			GameData gameData = GameData.of(gameDTO);
			GameSettings gameSettings = gameDTO.getSettings();
			Cell computedMove = computationService.startComputation(gameDTO.getId(), () -> engineService.computeMove(gameData, gameSettings));
			
			MoveDTO returnedMove = new MoveDTO(computedMove.getColumn(), computedMove.getRow(), GameData.extractPlayingColor(gameData));
			
			webSocketService.sendMessage(EngineMessageType.REFRESH_MOVE, gameDTO.getId(), returnedMove);
			return returnedMove;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new EngineException(e);
		} finally {
			webSocketService.sendMessage(EngineMessageType.IS_COMPUTING, gameDTO.getId(), false);
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
		computationService.stopComputation(gameId);
	}
	
}
