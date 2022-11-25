package fr.leblanc.gomoku.engine.service;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.DataWrapper;
import fr.leblanc.gomoku.engine.model.EngineConstants;
import fr.leblanc.gomoku.engine.model.EngineSettings;

@SpringBootTest
class StrikeServiceTest {

	@Autowired
	private StrikeService strikeService;
	
	@Test
	void findOrCounterStrikeTest() throws InterruptedException {
		
		DataWrapper dataWrapper = new DataWrapper(15);
		
		dataWrapper.addMove(new Cell(9, 6), EngineConstants.BLACK_COLOR);
		dataWrapper.addMove(new Cell(9, 7), EngineConstants.WHITE_COLOR);
		dataWrapper.addMove(new Cell(10, 5), EngineConstants.BLACK_COLOR);
		dataWrapper.addMove(new Cell(8, 7), EngineConstants.WHITE_COLOR);
		dataWrapper.addMove(new Cell(8, 6), EngineConstants.BLACK_COLOR);
		dataWrapper.addMove(new Cell(7, 8), EngineConstants.WHITE_COLOR);
		dataWrapper.addMove(new Cell(9, 5), EngineConstants.BLACK_COLOR);
		dataWrapper.addMove(new Cell(7, 6), EngineConstants.WHITE_COLOR);
		dataWrapper.addMove(new Cell(7, 7), EngineConstants.BLACK_COLOR);
		dataWrapper.addMove(new Cell(6, 8), EngineConstants.WHITE_COLOR);
		dataWrapper.addMove(new Cell(10, 7), EngineConstants.BLACK_COLOR);
		dataWrapper.addMove(new Cell(10, 8), EngineConstants.WHITE_COLOR);
		dataWrapper.addMove(new Cell(9, 8), EngineConstants.BLACK_COLOR);
		dataWrapper.addMove(new Cell(5, 8), EngineConstants.WHITE_COLOR);
		dataWrapper.addMove(new Cell(8, 3), EngineConstants.BLACK_COLOR);
		dataWrapper.addMove(new Cell(8, 5), EngineConstants.WHITE_COLOR);
		
		EngineSettings engineSettings = new EngineSettings();

		assertEquals(new Cell(10, 4), strikeService.findOrCounterStrike(dataWrapper, EngineConstants.BLACK_COLOR, engineSettings));
		
		dataWrapper.addMove(new Cell(10, 4), EngineConstants.BLACK_COLOR);
		
		assertEquals(new Cell(11, 3), strikeService.findOrCounterStrike(dataWrapper, EngineConstants.WHITE_COLOR, engineSettings));
		
		dataWrapper.addMove(new Cell(11, 3), EngineConstants.WHITE_COLOR);
		
		assertEquals(new Cell(10, 6), strikeService.findOrCounterStrike(dataWrapper, EngineConstants.BLACK_COLOR, engineSettings));
		
		dataWrapper.addMove(new Cell(10, 6), EngineConstants.BLACK_COLOR);
		
		assertEquals(new Cell(10, 3), strikeService.findOrCounterStrike(dataWrapper, EngineConstants.WHITE_COLOR, engineSettings));
		
		dataWrapper.addMove(new Cell(10, 3), EngineConstants.WHITE_COLOR);
		
		assertEquals(new Cell(11, 6), strikeService.findOrCounterStrike(dataWrapper, EngineConstants.BLACK_COLOR, engineSettings));
		
		dataWrapper.addMove(new Cell(11, 6), EngineConstants.BLACK_COLOR);
		
		assertEquals(new Cell(12, 6), strikeService.findOrCounterStrike(dataWrapper, EngineConstants.WHITE_COLOR, engineSettings));
		
	}
	
}
