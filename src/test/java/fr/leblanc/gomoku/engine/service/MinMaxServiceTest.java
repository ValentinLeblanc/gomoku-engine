package fr.leblanc.gomoku.engine.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.core.JsonProcessingException;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.EngineConstants;
import fr.leblanc.gomoku.engine.model.GameData;
import fr.leblanc.gomoku.engine.model.MinMaxResult;
import fr.leblanc.gomoku.engine.model.messaging.GameDTO;
import fr.leblanc.gomoku.engine.model.messaging.MoveDTO;
import fr.leblanc.gomoku.engine.util.GomokuTestsHelper;
import fr.leblanc.gomoku.engine.util.cache.GomokuCacheSupport;

@SpringBootTest
class MinMaxServiceTest {

	@Autowired
	private MinMaxService minMaxService;
	
	@Autowired
	private EvaluationService evaluationService;
	
	@Test
	void testMinMaxExtent() throws InterruptedException {
		GameDTO gameDto = new GameDTO();
		gameDto.setBoardSize(15);
		gameDto.getMoves().add(new MoveDTO(7, 7, EngineConstants.BLACK_COLOR));
		
		MinMaxResult minMaxResult = GomokuCacheSupport.doInCacheContext(() -> {
			return minMaxService.computeMinMax(GameData.of(gameDto), null, 2, 2);
		});
		
		assertNotNull(minMaxResult);
		assertFalse(minMaxResult.getOptimalMoves().isEmpty());
	}
	
	@Test
	void testMinMaxEvaluation() throws JsonProcessingException, InterruptedException {

		GameDTO gameDto = GomokuTestsHelper.readGameDto("minMax1.json");

		int playingColor = EngineConstants.WHITE_COLOR;

		int i = 0;

		while (i < 5) {
			i++;
			computeMinMaxAndTestEvaluation(gameDto, 2, playingColor);
		}

		computeMinMaxAndTestEvaluation(gameDto, 3, playingColor);

		computeMinMaxAndTestEvaluation(gameDto, 3, -playingColor);
	}
	
	private void computeMinMaxAndTestEvaluation(GameDTO gameDto, int depth, int playingColor) throws InterruptedException {
	
		GomokuCacheSupport.doInCacheContext(() -> {
	
			int color = playingColor;
	
			MinMaxResult minMaxResult = minMaxService.computeMinMax(GameData.of(gameDto), null, depth, 0);
			assertNotNull(minMaxResult);
	
			double evaluation = minMaxResult.getEvaluation();
	
			assertEquals(depth, minMaxResult.getOptimalMoves().size());
	
			for (int index = 0; index < depth; index++) {
				Cell move = minMaxResult.getOptimalMoves().get(index);
	
				MoveDTO newMove = new MoveDTO(move, color);
	
				newMove.setNumber(gameDto.getMoves().size());
	
				gameDto.getMoves().add(newMove);
				color = -color;
			}
	
			assertEquals(evaluation, evaluationService.computeEvaluation(GameData.of(gameDto)).getEvaluation(), 0.0001);
			return null;
		});
	
	}

	@Test
	void testMinMaxDepth() throws JsonProcessingException, InterruptedException {

		GameDTO gameDto = GomokuTestsHelper.readGameDto("minMax2.json");

		int playingColor = EngineConstants.WHITE_COLOR;
		
		MinMaxResult minMaxResult = GomokuCacheSupport.doInCacheContext(() -> {
			return minMaxService.computeMinMax(GameData.of(gameDto), null, 4, 0);
		});

		assertEquals(4, minMaxResult.getOptimalMoves().size());

		assertEquals(new Cell(8, 6), minMaxResult.getOptimalMoves().get(0));

		double evaluation = minMaxResult.getEvaluation();

		MoveDTO newMove = new MoveDTO(minMaxResult.getOptimalMoves().get(0), playingColor);

		newMove.setNumber(gameDto.getMoves().size());

		gameDto.getMoves().add(newMove);
		
		minMaxResult = GomokuCacheSupport.doInCacheContext(() -> {
			return minMaxService.computeMinMax(GameData.of(gameDto), null, 3, 0);
		});

		assertEquals(3, minMaxResult.getOptimalMoves().size());

		assertEquals(evaluation, minMaxResult.getEvaluation(), 0.001);

		evaluation = minMaxResult.getEvaluation();

		newMove = new MoveDTO(minMaxResult.getOptimalMoves().get(0), -playingColor);

		newMove.setNumber(gameDto.getMoves().size());

		gameDto.getMoves().add(newMove);

		minMaxResult = GomokuCacheSupport.doInCacheContext(() -> {
			return minMaxService.computeMinMax(GameData.of(gameDto), null, 2, 0);
		});

		assertEquals(2, minMaxResult.getOptimalMoves().size());

		assertEquals(evaluation, minMaxResult.getEvaluation(), 0.001);

	}

}
