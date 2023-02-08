package fr.leblanc.gomoku.engine.service;

import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.core.JsonProcessingException;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.DataWrapper;
import fr.leblanc.gomoku.engine.model.EngineConstants;
import fr.leblanc.gomoku.engine.model.MinMaxResult;
import fr.leblanc.gomoku.engine.model.messaging.GameDto;
import fr.leblanc.gomoku.engine.model.messaging.MoveDto;
import fr.leblanc.gomoku.engine.util.GomokuTestsHelper;
import fr.leblanc.gomoku.engine.util.cache.L2CacheSupport;

@SpringBootTest
class MinMaxServiceTest {

	@Autowired
	private MinMaxService minMaxService;
	
	@Autowired
	private EvaluationService evaluationService;
	
	@Test
	void minMax1Test() throws JsonProcessingException, InterruptedException {

		GameDto gameDto = GomokuTestsHelper.readGameDto("minMax1.json");

		int playingColor = EngineConstants.WHITE_COLOR;

		int i = 0;

		while (i < 5) {
			i++;
			testNextMoves(gameDto, 2, playingColor);
		}

		testNextMoves(gameDto, 3, playingColor);

		testNextMoves(gameDto, 3, -playingColor);
	}
	
	private void testNextMoves(GameDto gameDto, int depth, int playingColor) throws InterruptedException {
	
		L2CacheSupport.doInCacheContext(() -> {
	
			int color = playingColor;
	
			MinMaxResult minMaxResult = minMaxService.computeMinMax(DataWrapper.of(gameDto), null, depth, 0);
			assertNotNull(minMaxResult);
	
			double evaluation = minMaxResult.getEvaluation();
	
			assertEquals(depth, minMaxResult.getOptimalMoves().size());
	
			for (int index = 0; index < depth; index++) {
				Cell move = minMaxResult.getOptimalMoves().get(index);
	
				MoveDto newMove = new MoveDto(move, color);
	
				newMove.setNumber(gameDto.getMoves().size());
	
				gameDto.getMoves().add(newMove);
				color = -color;
			}
	
			assertEquals(evaluation, evaluationService.computeEvaluation(DataWrapper.of(gameDto)).getEvaluation(), 0.0001);
			return null;
		});
	
	}

	@Test
	void minMax2Test() throws JsonProcessingException, InterruptedException {

		GameDto gameDto = GomokuTestsHelper.readGameDto("minMax2.json");

		int playingColor = EngineConstants.WHITE_COLOR;

		MinMaxResult minMaxResult = L2CacheSupport.doInCacheContext(() -> {
			return minMaxService.computeMinMax(DataWrapper.of(gameDto), null, 4, 0);
		});

		assertEquals(4, minMaxResult.getOptimalMoves().size());

		assertEquals(new Cell(8, 6), minMaxResult.getOptimalMoves().get(0));

		double evaluation = minMaxResult.getEvaluation();

		MoveDto newMove = new MoveDto(minMaxResult.getOptimalMoves().get(0), playingColor);

		newMove.setNumber(gameDto.getMoves().size());

		gameDto.getMoves().add(newMove);

		minMaxResult = L2CacheSupport.doInCacheContext(() -> {
			return minMaxService.computeMinMax(DataWrapper.of(gameDto), null, 3, 0);
		});

		assertEquals(3, minMaxResult.getOptimalMoves().size());

		assertEquals(evaluation, minMaxResult.getEvaluation(), 0.001);

		evaluation = minMaxResult.getEvaluation();

		newMove = new MoveDto(minMaxResult.getOptimalMoves().get(0), -playingColor);

		newMove.setNumber(gameDto.getMoves().size());

		gameDto.getMoves().add(newMove);

		minMaxResult = L2CacheSupport.doInCacheContext(() -> {
			return minMaxService.computeMinMax(DataWrapper.of(gameDto), null, 2, 0);
		});

		assertEquals(2, minMaxResult.getOptimalMoves().size());

		assertEquals(evaluation, minMaxResult.getEvaluation(), 0.001);

	}

}
