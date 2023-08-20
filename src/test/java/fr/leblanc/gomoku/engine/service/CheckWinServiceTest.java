package fr.leblanc.gomoku.engine.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.GomokuColor;
import fr.leblanc.gomoku.engine.model.GameData;

@SpringBootTest
class CheckWinServiceTest {

	@Autowired
	private CheckWinService checkWinService;
	
	@Test
	void checkWinTest() {
		
		GameData gameData = new GameData(15);
		
		gameData.addMove(new Cell(0, 0), GomokuColor.BLACK_COLOR);
		gameData.addMove(new Cell(1, 0), GomokuColor.WHITE_COLOR);
		gameData.addMove(new Cell(2, 0), GomokuColor.BLACK_COLOR);
		gameData.addMove(new Cell(3, 0), GomokuColor.WHITE_COLOR);
		gameData.addMove(new Cell(13, 0), GomokuColor.BLACK_COLOR);
		
		assertFalse(checkWinService.checkWin(gameData).isWin());

		gameData = new GameData(15);
		
		gameData.addMove(new Cell(0, 0), GomokuColor.BLACK_COLOR);
		gameData.addMove(new Cell(1, 0), GomokuColor.BLACK_COLOR);
		gameData.addMove(new Cell(2, 0), GomokuColor.BLACK_COLOR);
		gameData.addMove(new Cell(3, 0), GomokuColor.BLACK_COLOR);
		gameData.addMove(new Cell(4, 0), GomokuColor.BLACK_COLOR);
		
		assertEquals(GomokuColor.BLACK_COLOR, checkWinService.checkWin(gameData).getColor());
		
		gameData = new GameData(15);
		
		gameData.addMove(new Cell(10, 0), GomokuColor.BLACK_COLOR);
		gameData.addMove(new Cell(11, 0), GomokuColor.BLACK_COLOR);
		gameData.addMove(new Cell(12, 0), GomokuColor.BLACK_COLOR);
		gameData.addMove(new Cell(13, 0), GomokuColor.BLACK_COLOR);
		gameData.addMove(new Cell(14, 0), GomokuColor.BLACK_COLOR);
		
		assertEquals(GomokuColor.BLACK_COLOR, checkWinService.checkWin(gameData).getColor());
		
		gameData = new GameData(15);
		
		gameData.addMove(new Cell(0, 0), GomokuColor.WHITE_COLOR);
		gameData.addMove(new Cell(0, 1), GomokuColor.WHITE_COLOR);
		gameData.addMove(new Cell(0, 2), GomokuColor.WHITE_COLOR);
		gameData.addMove(new Cell(0, 3), GomokuColor.WHITE_COLOR);
		gameData.addMove(new Cell(0, 4), GomokuColor.WHITE_COLOR);
		
		assertEquals(GomokuColor.WHITE_COLOR, checkWinService.checkWin(gameData).getColor());
		
		gameData = new GameData(15);
		
		gameData.addMove(new Cell(0, 10), GomokuColor.WHITE_COLOR);
		gameData.addMove(new Cell(0, 11), GomokuColor.WHITE_COLOR);
		gameData.addMove(new Cell(0, 12), GomokuColor.WHITE_COLOR);
		gameData.addMove(new Cell(0, 13), GomokuColor.WHITE_COLOR);
		gameData.addMove(new Cell(0, 14), GomokuColor.WHITE_COLOR);
		
		assertEquals(GomokuColor.WHITE_COLOR, checkWinService.checkWin(gameData).getColor());
		
	}

}
