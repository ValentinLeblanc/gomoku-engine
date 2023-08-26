package fr.leblanc.gomoku.engine.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.core.JsonProcessingException;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.EvaluationContext;
import fr.leblanc.gomoku.engine.model.GameData;
import fr.leblanc.gomoku.engine.model.GomokuColor;
import fr.leblanc.gomoku.engine.model.MinMaxContext;
import fr.leblanc.gomoku.engine.model.MinMaxResult;
import fr.leblanc.gomoku.engine.model.messaging.GameDTO;
import fr.leblanc.gomoku.engine.model.messaging.MoveDTO;
import fr.leblanc.gomoku.engine.util.GomokuTestsHelper;

@SpringBootTest
class MinMaxServiceTest extends AbstractGomokuTest {

	@Autowired
	private MinMaxService minMaxService;
	
	@Autowired
	private EvaluationService evaluationService;
	
	@Test
	void testMinMaxExtent() throws InterruptedException {
		GameDTO gameDto = new GameDTO();
		gameDto.setBoardSize(15);
		gameDto.getMoves().add(new MoveDTO(7, 7, GomokuColor.BLACK_COLOR));
		
		MinMaxResult minMaxResult = minMaxService.computeMinMax(GameData.of(gameDto), new MinMaxContext(TEST_GAME_ID, 2, 2));
		
		assertNotNull(minMaxResult);
		assertFalse(minMaxResult.getOptimalMoves().isEmpty());
	}
	
	@Test
	void testMinMaxEvaluation() throws JsonProcessingException, InterruptedException {

		GameDTO gameDto = GomokuTestsHelper.readGameDto("minMax1.json");

		int playingColor = GomokuColor.WHITE_COLOR;

		int i = 0;

		while (i < 5) {
			i++;
			computeMinMaxAndTestEvaluation(gameDto, 2, playingColor);
		}

		computeMinMaxAndTestEvaluation(gameDto, 3, playingColor);

		computeMinMaxAndTestEvaluation(gameDto, 3, -playingColor);
	}
	
	private void computeMinMaxAndTestEvaluation(GameDTO gameDto, int depth, int playingColor) throws InterruptedException {
		int color = playingColor;

		MinMaxResult minMaxResult = minMaxService.computeMinMax(GameData.of(gameDto), new MinMaxContext(TEST_GAME_ID, depth, 0, false));
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

		assertEquals(evaluation, evaluationService.computeEvaluation(TEST_GAME_ID, new EvaluationContext(GameData.of(gameDto)).internal()).getEvaluation(), 0.0001);
	
	}

	@Test
	void testMinMaxDepth() throws JsonProcessingException, InterruptedException {

		GameDTO gameDto = GomokuTestsHelper.readGameDto("minMax2.json");

		int playingColor = GomokuColor.WHITE_COLOR;
		
		MinMaxResult minMaxResult = minMaxService.computeMinMax(GameData.of(gameDto), new MinMaxContext(TEST_GAME_ID, 4, 0, false));

		assertEquals(4, minMaxResult.getOptimalMoves().size());

		assertEquals(new Cell(8, 6), minMaxResult.getOptimalMoves().get(0));

		double evaluation = minMaxResult.getEvaluation();

		MoveDTO newMove = new MoveDTO(minMaxResult.getOptimalMoves().get(0), playingColor);

		newMove.setNumber(gameDto.getMoves().size());

		gameDto.getMoves().add(newMove);
		
		minMaxResult = minMaxService.computeMinMax(GameData.of(gameDto), new MinMaxContext(TEST_GAME_ID, 3, 0, false));

		assertEquals(3, minMaxResult.getOptimalMoves().size());

		assertEquals(evaluation, minMaxResult.getEvaluation(), 0.001);

		evaluation = minMaxResult.getEvaluation();

		newMove = new MoveDTO(minMaxResult.getOptimalMoves().get(0), -playingColor);

		newMove.setNumber(gameDto.getMoves().size());

		gameDto.getMoves().add(newMove);

		minMaxResult = minMaxService.computeMinMax(GameData.of(gameDto), new MinMaxContext(TEST_GAME_ID, 2, 0, false));

		assertEquals(2, minMaxResult.getOptimalMoves().size());

		assertEquals(evaluation, minMaxResult.getEvaluation(), 0.001);

	}

}
