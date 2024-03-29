package fr.leblanc.gomoku.engine.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.core.JsonProcessingException;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.GameData;
import fr.leblanc.gomoku.engine.model.GomokuColor;
import fr.leblanc.gomoku.engine.model.StrikeContext;
import fr.leblanc.gomoku.engine.util.GomokuTestsHelper;

@SpringBootTest
class StrikeServiceTest extends AbstractGomokuTest {

	@Autowired
	private StrikeService strikeService;
	
	@Autowired
	private CacheService cacheService;

	@Test
	void testDirectStrikeAndDefendFromStrike() throws InterruptedException, JsonProcessingException {
		GameData gameData = GameData.of(GomokuTestsHelper.readGameDto("directStrike1.json"));

		StrikeContext strikeContext = new StrikeContext(TEST_GAME_ID, 2, -1);

		Cell strikeResult = strikeService.directStrike(gameData, GomokuColor.BLACK_COLOR, strikeContext);
		testDirectStrikeResult(gameData, new Cell(10, 4), strikeResult, GomokuColor.BLACK_COLOR);
		
		strikeResult = strikeService.defendFromDirectStrike(gameData, GomokuColor.WHITE_COLOR, strikeContext, true).get(0);
		testDefendFromStrikeResult(gameData, new Cell(11, 3), strikeResult, GomokuColor.WHITE_COLOR);
		gameData.addMove(new Cell(11, 3), GomokuColor.WHITE_COLOR);

		strikeResult = strikeService.directStrike(gameData, GomokuColor.BLACK_COLOR, strikeContext);
		testDirectStrikeResult(gameData, new Cell(10, 6), strikeResult, GomokuColor.BLACK_COLOR);

		strikeResult = strikeService.defendFromDirectStrike(gameData, GomokuColor.WHITE_COLOR, strikeContext, true).get(0);
		testDefendFromStrikeResult(gameData, new Cell(10, 3), strikeResult, GomokuColor.WHITE_COLOR);
		gameData.addMove(new Cell(10, 3), GomokuColor.WHITE_COLOR);

		strikeResult = strikeService.directStrike(gameData, GomokuColor.BLACK_COLOR, strikeContext);
		testDirectStrikeResult(gameData, new Cell(11, 6), strikeResult, GomokuColor.BLACK_COLOR);

	}

	@Test
	void testSecondaryStrike1() throws JsonProcessingException, InterruptedException {
		StrikeContext strikeContext = new StrikeContext(TEST_GAME_ID, 2, -1);
		GameData gameData = GameData.of(GomokuTestsHelper.readGameDto("secondaryStrike1.json"));
		Cell strikeResult = strikeService.secondaryStrike(gameData, GomokuColor.BLACK_COLOR, strikeContext);
		testSecondaryStrikeResult(gameData, new Cell(9, 6), strikeResult, GomokuColor.BLACK_COLOR);
	}
	
	@Test
	void testSecondaryStrike2() throws JsonProcessingException, InterruptedException {
		StrikeContext strikeContext = new StrikeContext(TEST_GAME_ID, 1, -1);
		GameData gameData = GameData.of(GomokuTestsHelper.readGameDto("secondaryStrike2.json"));
		Cell strikeResult = strikeService.secondaryStrike(gameData, GomokuColor.BLACK_COLOR, strikeContext);
		assertNull(strikeResult);
	}
	
	@Test
	void testSecondaryStrike3() throws JsonProcessingException, InterruptedException {
		StrikeContext strikeContext = new StrikeContext(TEST_GAME_ID, 4, -1);
		GameData gameData = GameData.of(GomokuTestsHelper.readGameDto("secondaryStrike3.json"));
		Cell strikeResult = strikeService.secondaryStrike(gameData, GomokuColor.BLACK_COLOR, strikeContext);
		testSecondaryStrikeResult(gameData, strikeResult, strikeResult, GomokuColor.BLACK_COLOR);
	}
	
	@Test
	void testSecondaryStrike4() throws JsonProcessingException, InterruptedException {
		StrikeContext strikeContext = new StrikeContext(TEST_GAME_ID, 1, -1);
		GameData gameData = GameData.of(GomokuTestsHelper.readGameDto("secondaryStrike4.json"));
		Cell strikeResult = strikeService.secondaryStrike(gameData, GomokuColor.BLACK_COLOR, strikeContext);
		testSecondaryStrikeResult(gameData, strikeResult, strikeResult, GomokuColor.BLACK_COLOR);
	}
	
	private void testSecondaryStrikeResult(GameData gameData, Cell expectedResult, Cell result, int color) {
		assertNotNull(result);
		assertEquals(expectedResult, result);
		assertTrue(cacheService.getSecondaryStrikeCache(TEST_GAME_ID).containsKey(color));
		assertTrue(cacheService.getSecondaryStrikeCache(TEST_GAME_ID).get(color).containsKey(gameData));
		assertTrue(cacheService.getSecondaryStrikeCache(TEST_GAME_ID).get(color).get(gameData).isPresent());
		assertEquals(expectedResult, cacheService.getSecondaryStrikeCache(TEST_GAME_ID).get(color).get(gameData).get());
		assertTrue(cacheService.getEvaluationCache(TEST_GAME_ID).containsKey(color));
		assertTrue(cacheService.getEvaluationCache(TEST_GAME_ID).get(color).containsKey(gameData));
		assertEquals(EvaluationService.STRIKE_EVALUATION, cacheService.getEvaluationCache(TEST_GAME_ID).get(color).get(gameData).getEvaluation(), 0.001d);
		gameData.addMove(expectedResult, color);
		assertTrue(cacheService.getEvaluationCache(TEST_GAME_ID).containsKey(color));
		assertTrue(cacheService.getEvaluationCache(TEST_GAME_ID).get(-color).containsKey(gameData));
		assertEquals(-EvaluationService.STRIKE_EVALUATION, cacheService.getEvaluationCache(TEST_GAME_ID).get(-color).get(gameData).getEvaluation(), 0.001d);
	}
	
	private void testDirectStrikeResult(GameData gameData, Cell expectedResult, Cell result, int color) {
		assertEquals(expectedResult, result);
		assertTrue(cacheService.getDirectStrikeCache(TEST_GAME_ID).containsKey(color));
		assertTrue(cacheService.getDirectStrikeCache(TEST_GAME_ID).get(color).containsKey(gameData));
		assertTrue(cacheService.getDirectStrikeCache(TEST_GAME_ID).get(color).get(gameData).isPresent());
		assertEquals(expectedResult, cacheService.getDirectStrikeCache(TEST_GAME_ID).get(color).get(gameData).get());
		assertTrue(cacheService.getEvaluationCache(TEST_GAME_ID).containsKey(color));
		assertTrue(cacheService.getEvaluationCache(TEST_GAME_ID).get(color).containsKey(gameData));
		assertEquals(EvaluationService.STRIKE_EVALUATION, cacheService.getEvaluationCache(TEST_GAME_ID).get(color).get(gameData).getEvaluation(), 0.001d);
		gameData.addMove(expectedResult, color);
		assertTrue(cacheService.getEvaluationCache(TEST_GAME_ID).containsKey(color));
		assertTrue(cacheService.getEvaluationCache(TEST_GAME_ID).get(-color).containsKey(gameData));
		assertEquals(-EvaluationService.STRIKE_EVALUATION, cacheService.getEvaluationCache(TEST_GAME_ID).get(-color).get(gameData).getEvaluation(), 0.001d);
	}
	
	private void testDefendFromStrikeResult(GameData gameData, Cell expectedResult, Cell result, int color) {
		assertEquals(expectedResult, result);
		assertTrue(cacheService.getCounterStrikeCache(TEST_GAME_ID).containsKey(color));
		assertTrue(cacheService.getCounterStrikeCache(TEST_GAME_ID).get(color).containsKey(gameData));
		assertTrue(cacheService.getCounterStrikeCache(TEST_GAME_ID).get(color).get(gameData).contains(expectedResult));
	}
	
}
