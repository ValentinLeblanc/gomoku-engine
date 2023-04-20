package fr.leblanc.gomoku.engine.service.impl;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.CheckWinResult;
import fr.leblanc.gomoku.engine.model.DataWrapper;
import fr.leblanc.gomoku.engine.model.EngineConstants;
import fr.leblanc.gomoku.engine.model.MinMaxResult;
import fr.leblanc.gomoku.engine.model.StrikeResult;
import fr.leblanc.gomoku.engine.model.messaging.GameDto;
import fr.leblanc.gomoku.engine.model.messaging.MoveDto;
import fr.leblanc.gomoku.engine.service.CheckWinService;
import fr.leblanc.gomoku.engine.service.EngineService;
import fr.leblanc.gomoku.engine.service.EvaluationService;
import fr.leblanc.gomoku.engine.service.MessageService;
import fr.leblanc.gomoku.engine.service.MinMaxService;
import fr.leblanc.gomoku.engine.service.StrikeService;
import fr.leblanc.gomoku.engine.util.GameHelper;
import fr.leblanc.gomoku.engine.util.cache.L2CacheSupport;

@Service
public class EngineServiceImpl implements EngineService {

	private static final Logger logger = LoggerFactory.getLogger(EngineServiceImpl.class);
	
	@Autowired
	private EvaluationService evaluationService;
	
	@Autowired
	private StrikeService strikeService;
	
	@Autowired
	private MinMaxService minMaxService;
	
	@Autowired
	private CheckWinService checkWinService;
	
	@Autowired
	private MessageService messagingService;

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

		for (int color : EngineConstants.COLORS) {
			int[][] result = new int[5][2];
			if (checkWinService.checkWin(DataWrapper.of(game), color, result)) {
				return null;
			}
		}
		
		try {

			MoveDto computedMove = L2CacheSupport.doInCacheContext(() -> {
				MoveDto result = null;

				messagingService.sendIsRunning(true);

				if (game.getSettings().isStrikeEnabled()) {
					// STRIKE
					StrikeResult strikeOrCounterStrike = strikeService.processStrike(DataWrapper.of(game), playingColor,
							game.getSettings().getStrikeDepth(), game.getSettings().getMinMaxDepth(),
							game.getSettings().getStrikeTimeout());
					if (strikeOrCounterStrike.hasResult()) {
						result = new MoveDto(strikeOrCounterStrike.getResultCell().getColumn(),
								strikeOrCounterStrike.getResultCell().getRow(), playingColor);
					}
				}

				// MINMAX
				if (result == null) {

					MinMaxResult minMaxResult = minMaxService.computeMinMax(DataWrapper.of(game), null,
							game.getSettings().getMinMaxDepth(), game.getSettings().getMinMaxExtent());

					if (minMaxResult != null && !minMaxResult.getOptimalMoves().isEmpty()) {
						Cell minMaxMove = minMaxResult.getOptimalMoves().get(0);
						result = new MoveDto(minMaxMove.getColumn(), minMaxMove.getRow(), playingColor);
					}
				}
				return result;

			});
			JSONObject message = new JSONObject();

			message.put("type", "REFRESH_MOVE");
			message.put("content", computedMove);

			messagingService.sendRefreshMove(computedMove);

			return computedMove;
			
		} catch (InterruptedException e) {
			logger.info("Interrupted engine service");
			Thread.currentThread().interrupt();
		} finally {
			messagingService.sendIsRunning(false);
		}
		
		return null;

	}

	@Override
	public Double computeEvaluation(GameDto game, boolean external) {
		
		int playingColor = GameHelper.extractPlayingColor(game);
		
		DataWrapper dataWrapper = DataWrapper.of(game);
		
		if (playingColor == EngineConstants.BLACK_COLOR) {
			return evaluationService.computeEvaluation(dataWrapper, external).getEvaluation();
		} else if (playingColor == EngineConstants.WHITE_COLOR) {
			return -evaluationService.computeEvaluation(dataWrapper, external).getEvaluation();
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

}
