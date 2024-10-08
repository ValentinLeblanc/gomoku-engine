package fr.leblanc.gomoku.engine.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.CheckWinResult;
import fr.leblanc.gomoku.engine.model.EvaluationContext;
import fr.leblanc.gomoku.engine.model.EvaluationDTO;
import fr.leblanc.gomoku.engine.model.EvaluationResult;
import fr.leblanc.gomoku.engine.model.GameData;
import fr.leblanc.gomoku.engine.model.GameSettings;
import fr.leblanc.gomoku.engine.model.GomokuColor;
import fr.leblanc.gomoku.engine.model.messaging.GameDTO;
import fr.leblanc.gomoku.engine.model.messaging.MoveDTO;
import fr.leblanc.gomoku.engine.service.CacheService;
import fr.leblanc.gomoku.engine.service.CheckWinService;
import fr.leblanc.gomoku.engine.service.EngineService;
import fr.leblanc.gomoku.engine.service.EvaluationService;
import fr.leblanc.gomoku.engine.service.GameComputationService;

@RestController
@RequestMapping("/engine")
public class EngineController {

	private static final Logger logger = LoggerFactory.getLogger(EngineController.class);

	private EngineService engineService;

	private CheckWinService checkWinService;

	private EvaluationService evaluationService;

	private GameComputationService computationService;

	private CacheService cacheService;

	public EngineController(EngineService engineService, CheckWinService checkWinService,
			EvaluationService evaluationService, GameComputationService computationService, CacheService cacheService) {
		super();
		this.engineService = engineService;
		this.checkWinService = checkWinService;
		this.evaluationService = evaluationService;
		this.computationService = computationService;
		this.cacheService = cacheService;
	}

	@GetMapping("/isComputing/{gameId}")
	public Boolean isComputing(@PathVariable Long gameId) {
		return computationService.isGameComputing(gameId);
	}

	@PostMapping("/checkWin")
	public CheckWinResult checkWin(@RequestBody GameDTO gameDTO) {
		GameData gameData = GameData.of(gameDTO);
		return checkWinService.checkWin(gameData);
	}

	@PostMapping("/computeMove")
	public MoveDTO computeMove(@RequestBody GameDTO gameDTO) {
		try {
			GameData gameData = GameData.of(gameDTO);
			GameSettings settingsData = new GameSettings(gameDTO.strikeEnabled(), gameDTO.minMaxEnabled(),
					gameDTO.displayAnalysis(), gameDTO.minMaxExtent(), gameDTO.minMaxDepth(), gameDTO.strikeDepth(),
					gameDTO.strikeTimeout());
			Cell computedMove = computationService.startGameComputation(gameDTO.id(), gameDTO.displayAnalysis(),
					() -> engineService.computeMove(gameDTO.id(), gameData, settingsData));
			return new MoveDTO(computedMove.getColumn(), computedMove.getRow(), GameData.extractPlayingColor(gameData));
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			logger.warn("Computation was interrupted");
		}
		return null;
	}

	@PostMapping("/computeEvaluation")
	public EvaluationDTO computeEvaluation(@RequestBody GameDTO gameDTO) throws InterruptedException {
		GameData gameData = GameData.of(gameDTO);
		int playingColor = GameData.extractPlayingColor(gameData);

		EvaluationResult eval = evaluationService.computeEvaluation(gameDTO.id(), new EvaluationContext(gameData).useStrikeService());
		if (playingColor == GomokuColor.BLACK_COLOR) {
			return new EvaluationDTO(eval.getEvaluation(), eval.getCellEvaluationMap());
		} else if (playingColor == GomokuColor.WHITE_COLOR) {
			return new EvaluationDTO(-eval.getEvaluation(), eval.getCellEvaluationMap());
		}

		throw new IllegalArgumentException("Game has no valid playing color");
	}

	@PostMapping("/stop/{gameId}")
	public void stopComputation(@PathVariable Long gameId) {
		computationService.stopGameComputation(gameId);
	}

	@DeleteMapping("/clearGame")
	public void clearGame(@RequestBody Long gameId) {
		cacheService.clearCache(gameId);
	}

}
