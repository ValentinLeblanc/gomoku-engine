package fr.leblanc.gomoku.engine.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import fr.leblanc.gomoku.engine.model.CheckWinResult;
import fr.leblanc.gomoku.engine.model.EngineConstants;
import fr.leblanc.gomoku.engine.model.GameDto;
import fr.leblanc.gomoku.engine.model.MoveDto;

@SpringBootTest
class EngineServiceImplTest {

	@Autowired
	private EngineService engineService;
	
	@Test
	void checkWinTest() {
		
		GameDto game = new GameDto();
		
		game.setBoardSize(15);
		
		MoveDto move0 = new MoveDto(0, 0, EngineConstants.BLACK_COLOR);
		MoveDto move1 = new MoveDto(1, 0, EngineConstants.WHITE_COLOR);
		MoveDto move2 = new MoveDto(2, 0, EngineConstants.BLACK_COLOR);
		MoveDto move3 = new MoveDto(3, 0, EngineConstants.WHITE_COLOR);
		MoveDto move4 = new MoveDto(13, 0, EngineConstants.BLACK_COLOR);
		
		game.getMoves().add(move0);
		game.getMoves().add(move1);
		game.getMoves().add(move2);
		game.getMoves().add(move3);
		game.getMoves().add(move4);
		
		CheckWinResult result = engineService.checkWin(game);
		
		assertFalse(result.isWin());
		
		move0 = new MoveDto(0, 0, EngineConstants.BLACK_COLOR);
		move1 = new MoveDto(1, 0, EngineConstants.BLACK_COLOR);
		move2 = new MoveDto(2, 0, EngineConstants.BLACK_COLOR);
		move3 = new MoveDto(3, 0, EngineConstants.BLACK_COLOR);
		move4 = new MoveDto(4, 0, EngineConstants.BLACK_COLOR);
		
		checkWinTest(game, move0, move1, move2, move3, move4);
		
		move0 = new MoveDto(10, 0, EngineConstants.BLACK_COLOR);
		move1 = new MoveDto(11, 0, EngineConstants.BLACK_COLOR);
		move2 = new MoveDto(12, 0, EngineConstants.BLACK_COLOR);
		move3 = new MoveDto(13, 0, EngineConstants.BLACK_COLOR);
		move4 = new MoveDto(14, 0, EngineConstants.BLACK_COLOR);
		
		checkWinTest(game, move0, move1, move2, move3, move4);
		
		move0 = new MoveDto(0, 0, EngineConstants.BLACK_COLOR);
		move1 = new MoveDto(0, 1, EngineConstants.BLACK_COLOR);
		move2 = new MoveDto(0, 2, EngineConstants.BLACK_COLOR);
		move3 = new MoveDto(0, 3, EngineConstants.BLACK_COLOR);
		move4 = new MoveDto(0, 4, EngineConstants.BLACK_COLOR);
		
		checkWinTest(game, move0, move1, move2, move3, move4);
		
		move0 = new MoveDto(0, 10, EngineConstants.BLACK_COLOR);
		move1 = new MoveDto(0, 11, EngineConstants.BLACK_COLOR);
		move2 = new MoveDto(0, 12, EngineConstants.BLACK_COLOR);
		move3 = new MoveDto(0, 13, EngineConstants.BLACK_COLOR);
		move4 = new MoveDto(0, 14, EngineConstants.BLACK_COLOR);
		
		checkWinTest(game, move0, move1, move2, move3, move4);
		
		move0 = new MoveDto(0, 0, EngineConstants.BLACK_COLOR);
		move1 = new MoveDto(1, 1, EngineConstants.BLACK_COLOR);
		move2 = new MoveDto(2, 2, EngineConstants.BLACK_COLOR);
		move3 = new MoveDto(3, 3, EngineConstants.BLACK_COLOR);
		move4 = new MoveDto(4, 4, EngineConstants.BLACK_COLOR);
		
		checkWinTest(game, move0, move1, move2, move3, move4);
		
		move0 = new MoveDto(10, 10, EngineConstants.WHITE_COLOR);
		move1 = new MoveDto(11, 11, EngineConstants.WHITE_COLOR);
		move2 = new MoveDto(12, 12, EngineConstants.WHITE_COLOR);
		move3 = new MoveDto(13, 13, EngineConstants.WHITE_COLOR);
		move4 = new MoveDto(14, 14, EngineConstants.WHITE_COLOR);
		
		checkWinTest(game, move0, move1, move2, move3, move4);
		
		move0 = new MoveDto(4, 0, EngineConstants.BLACK_COLOR);
		move1 = new MoveDto(3, 1, EngineConstants.BLACK_COLOR);
		move2 = new MoveDto(2, 2, EngineConstants.BLACK_COLOR);
		move3 = new MoveDto(1, 3, EngineConstants.BLACK_COLOR);
		move4 = new MoveDto(0, 4, EngineConstants.BLACK_COLOR);
		
		checkWinTest(game, move0, move1, move2, move3, move4);
		
		move0 = new MoveDto(14, 0, EngineConstants.WHITE_COLOR);
		move1 = new MoveDto(13, 1, EngineConstants.WHITE_COLOR);
		move2 = new MoveDto(12, 2, EngineConstants.WHITE_COLOR);
		move3 = new MoveDto(11, 3, EngineConstants.WHITE_COLOR);
		move4 = new MoveDto(10, 4, EngineConstants.WHITE_COLOR);
		
		checkWinTest(game, move0, move1, move2, move3, move4);
		
		move0 = new MoveDto(0, 14, EngineConstants.WHITE_COLOR);
		move1 = new MoveDto(1, 13, EngineConstants.WHITE_COLOR);
		move2 = new MoveDto(2, 12, EngineConstants.WHITE_COLOR);
		move3 = new MoveDto(3, 11, EngineConstants.WHITE_COLOR);
		move4 = new MoveDto(4, 10, EngineConstants.WHITE_COLOR);
		
		checkWinTest(game, move0, move1, move2, move3, move4);
		
	}

	private void checkWinTest(GameDto game, MoveDto move0, MoveDto move1, MoveDto move2, MoveDto move3, MoveDto move4) {
		
		game.getMoves().clear();
		
		CheckWinResult result;
		game.getMoves().add(move0);
		game.getMoves().add(move1);
		game.getMoves().add(move2);
		game.getMoves().add(move3);
		game.getMoves().add(move4);
		
		result = engineService.checkWin(game);
		
		assertTrue(result.isWin());
		
		assertTrue(result.getWinMoves().contains(move0));
		assertTrue(result.getWinMoves().contains(move1));
		assertTrue(result.getWinMoves().contains(move2));
		assertTrue(result.getWinMoves().contains(move3));
		assertTrue(result.getWinMoves().contains(move4));
	}
	
}
