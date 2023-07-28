package fr.leblanc.gomoku.engine.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.EngineConstants;
import fr.leblanc.gomoku.engine.model.GameData;

@SpringBootTest
class CheckWinServiceTest {

	@Autowired
	private CheckWinService checkWinService;
	
	@Test
	void checkWinTest() {
		
		GameData dataWrapper = new GameData(15);
		
		dataWrapper.addMove(new Cell(0, 0), EngineConstants.BLACK_COLOR);
		dataWrapper.addMove(new Cell(1, 0), EngineConstants.WHITE_COLOR);
		dataWrapper.addMove(new Cell(2, 0), EngineConstants.BLACK_COLOR);
		dataWrapper.addMove(new Cell(3, 0), EngineConstants.WHITE_COLOR);
		dataWrapper.addMove(new Cell(13, 0), EngineConstants.BLACK_COLOR);
		
		assertFalse(checkWinService.checkWin(dataWrapper).isWin());

		dataWrapper = new GameData(15);
		
		dataWrapper.addMove(new Cell(0, 0), EngineConstants.BLACK_COLOR);
		dataWrapper.addMove(new Cell(1, 0), EngineConstants.BLACK_COLOR);
		dataWrapper.addMove(new Cell(2, 0), EngineConstants.BLACK_COLOR);
		dataWrapper.addMove(new Cell(3, 0), EngineConstants.BLACK_COLOR);
		dataWrapper.addMove(new Cell(4, 0), EngineConstants.BLACK_COLOR);
		
		assertEquals(EngineConstants.BLACK_COLOR, checkWinService.checkWin(dataWrapper).getColor());
		
		dataWrapper = new GameData(15);
		
		dataWrapper.addMove(new Cell(10, 0), EngineConstants.BLACK_COLOR);
		dataWrapper.addMove(new Cell(11, 0), EngineConstants.BLACK_COLOR);
		dataWrapper.addMove(new Cell(12, 0), EngineConstants.BLACK_COLOR);
		dataWrapper.addMove(new Cell(13, 0), EngineConstants.BLACK_COLOR);
		dataWrapper.addMove(new Cell(14, 0), EngineConstants.BLACK_COLOR);
		
		assertEquals(EngineConstants.BLACK_COLOR, checkWinService.checkWin(dataWrapper).getColor());
		
		dataWrapper = new GameData(15);
		
		dataWrapper.addMove(new Cell(0, 0), EngineConstants.WHITE_COLOR);
		dataWrapper.addMove(new Cell(0, 1), EngineConstants.WHITE_COLOR);
		dataWrapper.addMove(new Cell(0, 2), EngineConstants.WHITE_COLOR);
		dataWrapper.addMove(new Cell(0, 3), EngineConstants.WHITE_COLOR);
		dataWrapper.addMove(new Cell(0, 4), EngineConstants.WHITE_COLOR);
		
		assertEquals(EngineConstants.WHITE_COLOR, checkWinService.checkWin(dataWrapper).getColor());
		
		dataWrapper = new GameData(15);
		
		dataWrapper.addMove(new Cell(0, 10), EngineConstants.WHITE_COLOR);
		dataWrapper.addMove(new Cell(0, 11), EngineConstants.WHITE_COLOR);
		dataWrapper.addMove(new Cell(0, 12), EngineConstants.WHITE_COLOR);
		dataWrapper.addMove(new Cell(0, 13), EngineConstants.WHITE_COLOR);
		dataWrapper.addMove(new Cell(0, 14), EngineConstants.WHITE_COLOR);
		
		assertEquals(EngineConstants.WHITE_COLOR, checkWinService.checkWin(dataWrapper).getColor());
		
	}

}
