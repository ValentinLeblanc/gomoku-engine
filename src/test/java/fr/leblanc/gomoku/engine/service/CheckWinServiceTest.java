package fr.leblanc.gomoku.engine.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.DataWrapper;
import fr.leblanc.gomoku.engine.model.EngineConstants;

@SpringBootTest
class CheckWinServiceTest {

	@Autowired
	private CheckWinService checkWinService;
	
	@Test
	void checkWinTest() {
		
		int[][] result = new int[5][2];
		
		DataWrapper dataWrapper = new DataWrapper(15);
		
		dataWrapper.addMove(new Cell(0, 0), EngineConstants.BLACK_COLOR);
		dataWrapper.addMove(new Cell(1, 0), EngineConstants.WHITE_COLOR);
		dataWrapper.addMove(new Cell(2, 0), EngineConstants.BLACK_COLOR);
		dataWrapper.addMove(new Cell(3, 0), EngineConstants.WHITE_COLOR);
		dataWrapper.addMove(new Cell(13, 0), EngineConstants.BLACK_COLOR);
		
		assertFalse(checkWinService.checkWin(dataWrapper, EngineConstants.BLACK_COLOR, result));
		assertFalse(checkWinService.checkWin(dataWrapper, EngineConstants.WHITE_COLOR, result));

		dataWrapper = new DataWrapper(15);
		
		dataWrapper.addMove(new Cell(0, 0), EngineConstants.BLACK_COLOR);
		dataWrapper.addMove(new Cell(1, 0), EngineConstants.BLACK_COLOR);
		dataWrapper.addMove(new Cell(2, 0), EngineConstants.BLACK_COLOR);
		dataWrapper.addMove(new Cell(3, 0), EngineConstants.BLACK_COLOR);
		dataWrapper.addMove(new Cell(4, 0), EngineConstants.BLACK_COLOR);
		
		assertTrue(checkWinService.checkWin(dataWrapper, EngineConstants.BLACK_COLOR, result));
		
		dataWrapper = new DataWrapper(15);
		
		dataWrapper.addMove(new Cell(10, 0), EngineConstants.BLACK_COLOR);
		dataWrapper.addMove(new Cell(11, 0), EngineConstants.BLACK_COLOR);
		dataWrapper.addMove(new Cell(12, 0), EngineConstants.BLACK_COLOR);
		dataWrapper.addMove(new Cell(13, 0), EngineConstants.BLACK_COLOR);
		dataWrapper.addMove(new Cell(14, 0), EngineConstants.BLACK_COLOR);
		
		assertTrue(checkWinService.checkWin(dataWrapper, EngineConstants.BLACK_COLOR, result));
		
		dataWrapper = new DataWrapper(15);
		
		dataWrapper.addMove(new Cell(0, 0), EngineConstants.WHITE_COLOR);
		dataWrapper.addMove(new Cell(0, 1), EngineConstants.WHITE_COLOR);
		dataWrapper.addMove(new Cell(0, 2), EngineConstants.WHITE_COLOR);
		dataWrapper.addMove(new Cell(0, 3), EngineConstants.WHITE_COLOR);
		dataWrapper.addMove(new Cell(0, 4), EngineConstants.WHITE_COLOR);
		
		assertTrue(checkWinService.checkWin(dataWrapper, EngineConstants.WHITE_COLOR, result));
		
		dataWrapper = new DataWrapper(15);
		
		dataWrapper.addMove(new Cell(0, 10), EngineConstants.WHITE_COLOR);
		dataWrapper.addMove(new Cell(0, 11), EngineConstants.WHITE_COLOR);
		dataWrapper.addMove(new Cell(0, 12), EngineConstants.WHITE_COLOR);
		dataWrapper.addMove(new Cell(0, 13), EngineConstants.WHITE_COLOR);
		dataWrapper.addMove(new Cell(0, 14), EngineConstants.WHITE_COLOR);
		
		assertTrue(checkWinService.checkWin(dataWrapper, EngineConstants.WHITE_COLOR, result));
		
	}

}
