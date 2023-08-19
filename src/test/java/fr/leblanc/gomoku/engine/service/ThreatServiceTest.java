package fr.leblanc.gomoku.engine.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.core.JsonProcessingException;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.GameData;
import fr.leblanc.gomoku.engine.model.GomokuColor;
import fr.leblanc.gomoku.engine.model.ThreatContext;
import fr.leblanc.gomoku.engine.model.ThreatType;
import fr.leblanc.gomoku.engine.model.messaging.GameDTO;
import fr.leblanc.gomoku.engine.util.GomokuTestsHelper;

@SpringBootTest
public class ThreatServiceTest extends AbstractGomokuTest {

	@Autowired
	private ThreatService threatService;
	
	@Test
	void testThreat5AndDoubleThreat4() throws JsonProcessingException {
		GameDTO gameDto = GomokuTestsHelper.readGameDto("threatT5DT4.json");
		
		ThreatContext playingThreatContext = threatService.computeThreatContext(GameData.of(gameDto), GomokuColor.BLACK_COLOR);
		assertEquals(1, playingThreatContext.getThreatTypeToThreatMap().get(ThreatType.THREAT_5).size());
		assertTrue(playingThreatContext.getThreatTypeToThreatMap().get(ThreatType.THREAT_5).get(0).getEmptyCells().contains(new Cell(5, 10)));
		
		ThreatContext opponentThreatContext = threatService.computeThreatContext(GameData.of(gameDto), GomokuColor.WHITE_COLOR);
		assertEquals(2, opponentThreatContext.getDoubleThreatTypeToThreatMap().get(ThreatType.DOUBLE_THREAT_4).size());
		assertTrue(opponentThreatContext.getDoubleThreatTypeToThreatMap().get(ThreatType.DOUBLE_THREAT_4).stream().anyMatch(t -> t.getTargetCell().equals(new Cell(10, 6))));
		assertTrue(opponentThreatContext.getDoubleThreatTypeToThreatMap().get(ThreatType.DOUBLE_THREAT_4).stream().anyMatch(t -> t.getTargetCell().equals(new Cell(6, 10))));
	}
	
	@Test
	void testDoubleThreat3AndDoubleThreat3() throws JsonProcessingException {
		GameDTO gameDto = GomokuTestsHelper.readGameDto("threatDT3DT3.json");
		
		ThreatContext playingThreatContext = threatService.computeThreatContext(GameData.of(gameDto), GomokuColor.BLACK_COLOR);
		assertEquals(4, playingThreatContext.getDoubleThreatTypeToThreatMap().get(ThreatType.DOUBLE_THREAT_3).size());
		assertTrue(playingThreatContext.getDoubleThreatTypeToThreatMap().get(ThreatType.DOUBLE_THREAT_3).stream().anyMatch(t -> t.getTargetCell().equals(new Cell(10, 5))));
		assertTrue(playingThreatContext.getDoubleThreatTypeToThreatMap().get(ThreatType.DOUBLE_THREAT_3).stream().anyMatch(t -> t.getTargetCell().equals(new Cell(9, 6))));
		assertTrue(playingThreatContext.getDoubleThreatTypeToThreatMap().get(ThreatType.DOUBLE_THREAT_3).stream().anyMatch(t -> t.getTargetCell().equals(new Cell(6, 9))));
		assertTrue(playingThreatContext.getDoubleThreatTypeToThreatMap().get(ThreatType.DOUBLE_THREAT_3).stream().anyMatch(t -> t.getTargetCell().equals(new Cell(5, 10))));
		
		ThreatContext opponentThreatContext = threatService.computeThreatContext(GameData.of(gameDto), GomokuColor.WHITE_COLOR);
		assertEquals(4, opponentThreatContext.getDoubleThreatTypeToThreatMap().get(ThreatType.DOUBLE_THREAT_3).size());
		assertTrue(opponentThreatContext.getDoubleThreatTypeToThreatMap().get(ThreatType.DOUBLE_THREAT_3).stream().anyMatch(t -> t.getTargetCell().equals(new Cell(10, 6))));
		assertTrue(opponentThreatContext.getDoubleThreatTypeToThreatMap().get(ThreatType.DOUBLE_THREAT_3).stream().anyMatch(t -> t.getTargetCell().equals(new Cell(11, 5))));
		assertTrue(opponentThreatContext.getDoubleThreatTypeToThreatMap().get(ThreatType.DOUBLE_THREAT_3).stream().anyMatch(t -> t.getTargetCell().equals(new Cell(7, 9))));
		assertTrue(opponentThreatContext.getDoubleThreatTypeToThreatMap().get(ThreatType.DOUBLE_THREAT_3).stream().anyMatch(t -> t.getTargetCell().equals(new Cell(6, 10))));
	}
	
	@Test
	void testDoubleThreat2AndDoubleThreat2() throws JsonProcessingException {
		GameDTO gameDto = GomokuTestsHelper.readGameDto("threatDT2DT2.json");
		
		ThreatContext playingThreatContext = threatService.computeThreatContext(GameData.of(gameDto), GomokuColor.BLACK_COLOR);
		assertEquals(18, playingThreatContext.getDoubleThreatTypeToThreatMap().get(ThreatType.DOUBLE_THREAT_2).size());
		
		ThreatContext opponentThreatContext = threatService.computeThreatContext(GameData.of(gameDto), GomokuColor.WHITE_COLOR);
		assertEquals(18, opponentThreatContext.getDoubleThreatTypeToThreatMap().get(ThreatType.DOUBLE_THREAT_2).size());
	}
}
