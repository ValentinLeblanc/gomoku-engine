package fr.leblanc.gomoku.engine.service.impl;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.CheckWinResult;
import fr.leblanc.gomoku.engine.model.DataWrapper;
import fr.leblanc.gomoku.engine.model.EngineConstants;
import fr.leblanc.gomoku.engine.model.messaging.GameDto;
import fr.leblanc.gomoku.engine.model.messaging.MoveDto;
import fr.leblanc.gomoku.engine.service.CheckWinService;
import fr.leblanc.gomoku.engine.service.EngineService;
import fr.leblanc.gomoku.engine.service.EvaluationService;
import fr.leblanc.gomoku.engine.service.MessagingService;
import fr.leblanc.gomoku.engine.service.MinMaxService;
import fr.leblanc.gomoku.engine.service.StrikeService;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class EngineServiceImpl implements EngineService {

	@Autowired
	private EvaluationService evaluationService;
	
	@Autowired
	private StrikeService strikeService;
	
	@Autowired
	private MinMaxService minMaxService;
	
	@Autowired
	private CheckWinService checkWinService;
	
	@Autowired
	private MessagingService messagingService;

	@Override
	public CheckWinResult checkWin(GameDto game) {

		DataWrapper dataWrapper = DataWrapper.of(game);

		for (int color : EngineConstants.COLORS) {
			int[][] result = new int[5][2];
			if (checkWinService.checkWin(dataWrapper, color, result)) {
				return buildResult(result, color);
			}
		}

		return new CheckWinResult();

	}

	@Override
	public MoveDto computeMove(GameDto game) {

		int playingColor = game.getMoves().size() % 2 == 0 ? EngineConstants.BLACK_COLOR : EngineConstants.WHITE_COLOR;
		
		if (game.getMoves().isEmpty()) {
			return new MoveDto(game.getBoardSize() / 2, game.getBoardSize() / 2, playingColor);
		}
		
		MoveDto computedMove = null;
		
		try {
			
			messagingService.sendIsRunning(true);
			
			DataWrapper dataWrapper = DataWrapper.of(game);
			Cell strikeOrCounterStrike = strikeService.findOrCounterStrike(dataWrapper, playingColor, game.getSettings());
			if (strikeOrCounterStrike != null) {
				computedMove = new MoveDto(strikeOrCounterStrike.getColumnIndex(), strikeOrCounterStrike.getRowIndex(), playingColor);
			} else {
				Cell minMaxMove = minMaxService.computeMinMax(dataWrapper, playingColor, null, game.getSettings());
				computedMove = new MoveDto(minMaxMove.getColumnIndex(), minMaxMove.getRowIndex(), playingColor);
			}
		} catch (InterruptedException e) {
			log.info("Interrupted engine service");
			Thread.currentThread().interrupt();
		} finally {
			messagingService.sendIsRunning(false);
		}
		
		JSONObject message = new JSONObject();
		
		message.put("type", "REFRESH_MOVE");
		message.put("content", computedMove);
		
		messagingService.sendRefreshMove(computedMove);
		
		return computedMove;
	}


	@Override
	public Double computeEvaluation(GameDto game) {
		
		int playingColor = extractPlayingColor(game);
		
		DataWrapper dataWrapper = DataWrapper.of(game);
		
		if (playingColor == EngineConstants.BLACK_COLOR) {
			return evaluationService.computeEvaluation(dataWrapper, playingColor, game.getSettings());
		} else if (playingColor == EngineConstants.WHITE_COLOR) {
			return -evaluationService.computeEvaluation(dataWrapper, playingColor, game.getSettings());
		}
		
		throw new IllegalArgumentException("Game has no valid playing color");
		
	}

	@Override
	public void stopComputation() {
		if (strikeService.isComputing()) {
			strikeService.stopComputation();
		} else if (minMaxService.isComputing()) {
			minMaxService.stopComputation();
		}
	}

	private CheckWinResult buildResult(int[][] win, int color) {

		CheckWinResult result = new CheckWinResult(true);

		for (int i = 0; i < win.length; i++) {
			result.getWinMoves().add(new MoveDto(win[i][0], win[i][1], color));
		}

		return result;
	}

	private int extractPlayingColor(GameDto game) {
		return game.getMoves().size() % 2 == 0 ? EngineConstants.BLACK_COLOR : EngineConstants.WHITE_COLOR;
	}

}
