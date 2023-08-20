package fr.leblanc.gomoku.engine.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Test;
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
class ThreatServiceTest extends AbstractGomokuTest {
	
	@Test
	void testThreat5AndDoubleThreat4() throws JsonProcessingException {
		GameDTO gameDto = GomokuTestsHelper.readGameDto("threatT5DT4.json");
		
		ThreatContext playingThreatContext = GameData.of(gameDto).computeThreatContext(GomokuColor.BLACK_COLOR);
		assertEquals(1, playingThreatContext.getThreatsOfType(ThreatType.THREAT_5).size());
		assertEquals(new Cell(5, 10), playingThreatContext.getThreatsOfType(ThreatType.THREAT_5).get(0).getTargetCell());
		
		ThreatContext opponentThreatContext = GameData.of(gameDto).computeThreatContext(GomokuColor.WHITE_COLOR);
		assertEquals(2, opponentThreatContext.getThreatsOfType(ThreatType.DOUBLE_THREAT_4).size());
		assertTrue(opponentThreatContext.getThreatsOfType(ThreatType.DOUBLE_THREAT_4).stream().anyMatch(t -> t.getTargetCell().equals(new Cell(10, 6))));
		assertTrue(opponentThreatContext.getThreatsOfType(ThreatType.DOUBLE_THREAT_4).stream().anyMatch(t -> t.getTargetCell().equals(new Cell(6, 10))));
	}
	
	@Test
	void testDoubleThreat3AndDoubleThreat3() throws JsonProcessingException {
		GameDTO gameDto = GomokuTestsHelper.readGameDto("threatDT3DT3.json");
		
		ThreatContext playingThreatContext = GameData.of(gameDto).computeThreatContext(GomokuColor.BLACK_COLOR);
		assertEquals(4, playingThreatContext.getThreatsOfType(ThreatType.DOUBLE_THREAT_3).size());
		assertTrue(playingThreatContext.getThreatsOfType(ThreatType.DOUBLE_THREAT_3).stream().anyMatch(t -> t.getTargetCell().equals(new Cell(10, 5))));
		assertTrue(playingThreatContext.getThreatsOfType(ThreatType.DOUBLE_THREAT_3).stream().anyMatch(t -> t.getTargetCell().equals(new Cell(9, 6))));
		assertTrue(playingThreatContext.getThreatsOfType(ThreatType.DOUBLE_THREAT_3).stream().anyMatch(t -> t.getTargetCell().equals(new Cell(6, 9))));
		assertTrue(playingThreatContext.getThreatsOfType(ThreatType.DOUBLE_THREAT_3).stream().anyMatch(t -> t.getTargetCell().equals(new Cell(5, 10))));
		
		ThreatContext opponentThreatContext = GameData.of(gameDto).computeThreatContext(GomokuColor.WHITE_COLOR);
		assertEquals(4, opponentThreatContext.getThreatsOfType(ThreatType.DOUBLE_THREAT_3).size());
		assertTrue(opponentThreatContext.getThreatsOfType(ThreatType.DOUBLE_THREAT_3).stream().anyMatch(t -> t.getTargetCell().equals(new Cell(10, 6))));
		assertTrue(opponentThreatContext.getThreatsOfType(ThreatType.DOUBLE_THREAT_3).stream().anyMatch(t -> t.getTargetCell().equals(new Cell(11, 5))));
		assertTrue(opponentThreatContext.getThreatsOfType(ThreatType.DOUBLE_THREAT_3).stream().anyMatch(t -> t.getTargetCell().equals(new Cell(7, 9))));
		assertTrue(opponentThreatContext.getThreatsOfType(ThreatType.DOUBLE_THREAT_3).stream().anyMatch(t -> t.getTargetCell().equals(new Cell(6, 10))));
	}
	
	@Test
	void testDoubleThreat2AndDoubleThreat2() throws JsonProcessingException {
		GameDTO gameDto = GomokuTestsHelper.readGameDto("threatDT2DT2.json");
		
		ThreatContext playingThreatContext = GameData.of(gameDto).computeThreatContext(GomokuColor.BLACK_COLOR);
		assertEquals(18, playingThreatContext.getThreatsOfType(ThreatType.DOUBLE_THREAT_2).size());
		
		ThreatContext opponentThreatContext = GameData.of(gameDto).computeThreatContext(GomokuColor.WHITE_COLOR);
		assertEquals(18, opponentThreatContext.getThreatsOfType(ThreatType.DOUBLE_THREAT_2).size());
	}
}
