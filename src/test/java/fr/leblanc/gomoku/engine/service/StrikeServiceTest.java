package fr.leblanc.gomoku.engine.service;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.core.JsonProcessingException;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.DataWrapper;
import fr.leblanc.gomoku.engine.model.EngineConstants;
import fr.leblanc.gomoku.engine.model.StrikeResult;
import fr.leblanc.gomoku.engine.model.StrikeResult.StrikeType;
import fr.leblanc.gomoku.engine.util.GomokuTestsHelper;
import fr.leblanc.gomoku.engine.util.cache.L2CacheSupport;

@SpringBootTest
class StrikeServiceTest {

	@Autowired
	private StrikeService strikeService;
	
	@Test
	void findOrCounterStrikeTest() throws InterruptedException {
		
		L2CacheSupport.doInCacheContext(() -> {
			try {
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
				
				StrikeResult strikeResult = strikeService.processStrike(dataWrapper, EngineConstants.BLACK_COLOR, 2, 2, -1);
				assertEquals(StrikeType.DIRECT_STRIKE, strikeResult.getStrikeType());
				assertEquals(new Cell(10, 4), strikeResult.getResultCell());
				dataWrapper.addMove(new Cell(10, 4), EngineConstants.BLACK_COLOR);
				
				strikeResult = strikeService.processStrike(dataWrapper, EngineConstants.WHITE_COLOR, 2, 2, -1);
				assertEquals(StrikeType.DEFEND_STRIKE, strikeResult.getStrikeType());
				assertEquals(new Cell(11, 3), strikeResult.getResultCell());
				dataWrapper.addMove(new Cell(11, 3), EngineConstants.WHITE_COLOR);
				
				strikeResult = strikeService.processStrike(dataWrapper, EngineConstants.BLACK_COLOR, 2, 2, -1);
				assertEquals(StrikeType.DIRECT_STRIKE, strikeResult.getStrikeType());
				assertEquals(new Cell(10, 6), strikeResult.getResultCell());
				dataWrapper.addMove(new Cell(10, 6), EngineConstants.BLACK_COLOR);
				
				strikeResult = strikeService.processStrike(dataWrapper, EngineConstants.WHITE_COLOR, 2, 2, -1);
				assertEquals(StrikeType.DEFEND_STRIKE, strikeResult.getStrikeType());
				assertEquals(new Cell(10, 3), strikeResult.getResultCell());
				dataWrapper.addMove(new Cell(10, 3), EngineConstants.WHITE_COLOR);
				
				strikeResult = strikeService.processStrike(dataWrapper, EngineConstants.BLACK_COLOR, 2, 2, -1);
				assertEquals(StrikeType.DIRECT_STRIKE, strikeResult.getStrikeType());
				assertEquals(new Cell(11, 6), strikeResult.getResultCell());
				dataWrapper.addMove(new Cell(11, 6), EngineConstants.BLACK_COLOR);
				
				strikeResult = strikeService.processStrike(dataWrapper, EngineConstants.WHITE_COLOR, 2, 2, -1);
				assertEquals(StrikeType.DEFEND_STRIKE, strikeResult.getStrikeType());
				dataWrapper.addMove(new Cell(12, 6), EngineConstants.WHITE_COLOR);
			} catch (Exception e) {
				
			}
			
			return null;
		});
	}
	
	@Test
	void secondaryStrikeTest() throws JsonProcessingException, InterruptedException {
		
		L2CacheSupport.doInCacheContext(() -> {
			try {
				StrikeResult strikeResult = strikeService.processStrike(DataWrapper.of(GomokuTestsHelper.readGameDto("secondaryStrike1.json")), EngineConstants.BLACK_COLOR, 2, 2, -1);
				
				assertEquals(StrikeType.SECONDARY_STRIKE, strikeResult.getStrikeType());
				assertEquals(new Cell(9,6), strikeResult.getResultCell());
				
				strikeResult = strikeService.processStrike(DataWrapper.of(GomokuTestsHelper.readGameDto("secondaryStrike2.json")), EngineConstants.BLACK_COLOR, 1, 2, 10);
				
				assertEquals(StrikeType.DEFEND_STRIKE, strikeResult.getStrikeType());
				
				strikeResult = strikeService.processStrike(DataWrapper.of(GomokuTestsHelper.readGameDto("secondaryStrike3.json")), EngineConstants.WHITE_COLOR, 4, 2, 5);
				
				assertEquals(StrikeType.SECONDARY_STRIKE, strikeResult.getStrikeType());
				
				strikeResult = strikeService.processStrike(DataWrapper.of(GomokuTestsHelper.readGameDto("secondaryStrike4.json")), EngineConstants.BLACK_COLOR, 1, 2, 15);
				
				assertEquals(StrikeType.EMPTY_STRIKE, strikeResult.getStrikeType());
				
			} catch (Exception e) {
				
			}
			return null;
		});
		
	}
}
