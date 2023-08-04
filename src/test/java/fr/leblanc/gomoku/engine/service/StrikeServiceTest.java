package fr.leblanc.gomoku.engine.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.core.JsonProcessingException;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.GomokuColor;
import fr.leblanc.gomoku.engine.model.GameData;
import fr.leblanc.gomoku.engine.model.StrikeContext;
import fr.leblanc.gomoku.engine.util.GomokuTestsHelper;

@SpringBootTest
class StrikeServiceTest extends AbstractGomokuTest {

	@Autowired
	private StrikeService strikeService;

	@Test
	void findOrCounterStrikeTest() throws InterruptedException {
		GameData dataWrapper = new GameData(15);

		dataWrapper.addMove(new Cell(9, 6), GomokuColor.BLACK_COLOR);
		dataWrapper.addMove(new Cell(9, 7), GomokuColor.WHITE_COLOR);
		dataWrapper.addMove(new Cell(10, 5), GomokuColor.BLACK_COLOR);
		dataWrapper.addMove(new Cell(8, 7), GomokuColor.WHITE_COLOR);
		dataWrapper.addMove(new Cell(8, 6), GomokuColor.BLACK_COLOR);
		dataWrapper.addMove(new Cell(7, 8), GomokuColor.WHITE_COLOR);
		dataWrapper.addMove(new Cell(9, 5), GomokuColor.BLACK_COLOR);
		dataWrapper.addMove(new Cell(7, 6), GomokuColor.WHITE_COLOR);
		dataWrapper.addMove(new Cell(7, 7), GomokuColor.BLACK_COLOR);
		dataWrapper.addMove(new Cell(6, 8), GomokuColor.WHITE_COLOR);
		dataWrapper.addMove(new Cell(10, 7), GomokuColor.BLACK_COLOR);
		dataWrapper.addMove(new Cell(10, 8), GomokuColor.WHITE_COLOR);
		dataWrapper.addMove(new Cell(9, 8), GomokuColor.BLACK_COLOR);
		dataWrapper.addMove(new Cell(5, 8), GomokuColor.WHITE_COLOR);
		dataWrapper.addMove(new Cell(8, 3), GomokuColor.BLACK_COLOR);
		dataWrapper.addMove(new Cell(8, 5), GomokuColor.WHITE_COLOR);

		StrikeContext strikeContext = new StrikeContext(TEST_GAME_ID, 2, 2, -1);

		Cell strikeResult = strikeService.directStrike(dataWrapper, GomokuColor.BLACK_COLOR, strikeContext);
		assertEquals(new Cell(10, 4), strikeResult);
		dataWrapper.addMove(new Cell(10, 4), GomokuColor.BLACK_COLOR);

		strikeResult = strikeService.defendFromDirectStrike(dataWrapper, GomokuColor.WHITE_COLOR, strikeContext, true).get(0);
		assertEquals(new Cell(11, 3), strikeResult);
		dataWrapper.addMove(new Cell(11, 3), GomokuColor.WHITE_COLOR);

		strikeResult = strikeService.directStrike(dataWrapper, GomokuColor.BLACK_COLOR, strikeContext);
		assertEquals(new Cell(10, 6), strikeResult);
		dataWrapper.addMove(new Cell(10, 6), GomokuColor.BLACK_COLOR);

		strikeResult = strikeService.defendFromDirectStrike(dataWrapper, GomokuColor.WHITE_COLOR, strikeContext, true).get(0);
		assertEquals(new Cell(10, 3), strikeResult);
		dataWrapper.addMove(new Cell(10, 3), GomokuColor.WHITE_COLOR);

		strikeResult = strikeService.directStrike(dataWrapper, GomokuColor.BLACK_COLOR, strikeContext);
		assertEquals(new Cell(11, 6), strikeResult);
		dataWrapper.addMove(new Cell(11, 6), GomokuColor.BLACK_COLOR);

		strikeResult = strikeService.directStrike(dataWrapper, GomokuColor.WHITE_COLOR, strikeContext);
		dataWrapper.addMove(new Cell(12, 6), GomokuColor.WHITE_COLOR);
	}

	@Test
	void secondaryStrikeTest() throws JsonProcessingException, InterruptedException {

		StrikeContext strikeContext = new StrikeContext(TEST_GAME_ID, 2, 2, -1);

		Cell strikeResult = strikeService.secondaryStrike(GameData.of(GomokuTestsHelper.readGameDto("secondaryStrike1.json")), GomokuColor.BLACK_COLOR,
				strikeContext);

		assertEquals(new Cell(9, 6), strikeResult);

		strikeContext.setStrikeDepth(1);
		strikeResult = strikeService.secondaryStrike(GameData.of(GomokuTestsHelper.readGameDto("secondaryStrike2.json")),
				GomokuColor.BLACK_COLOR, strikeContext);

		strikeContext.setStrikeDepth(4);
		strikeResult = strikeService.secondaryStrike(GameData.of(GomokuTestsHelper.readGameDto("secondaryStrike3.json")),
				GomokuColor.WHITE_COLOR, strikeContext);

		strikeContext.setStrikeDepth(1);
		strikeResult = strikeService.secondaryStrike(GameData.of(GomokuTestsHelper.readGameDto("secondaryStrike4.json")),
				GomokuColor.BLACK_COLOR, strikeContext);

		assertNull(strikeResult);

	}
}
