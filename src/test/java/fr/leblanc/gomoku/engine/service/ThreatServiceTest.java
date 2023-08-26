package fr.leblanc.gomoku.engine.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.core.JsonProcessingException;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.GameData;
import fr.leblanc.gomoku.engine.model.GomokuColor;
import fr.leblanc.gomoku.engine.model.Threat;
import fr.leblanc.gomoku.engine.model.ThreatContext;
import fr.leblanc.gomoku.engine.model.ThreatType;
import fr.leblanc.gomoku.engine.util.GomokuTestsHelper;

@SpringBootTest
class ThreatServiceTest extends AbstractGomokuTest {
	
	@Autowired
	private ThreatService threatService;
	
	@Test
	void testThreat5AndDoubleThreat4() throws JsonProcessingException {
		GameData gameData = GameData.of(GomokuTestsHelper.readGameDto("threatT5DT4.json"));
		
		ThreatContext playingThreatContext = threatService.getOrUpdateThreatContext(gameData, GomokuColor.BLACK_COLOR);
		assertEquals(1, playingThreatContext.getThreatsOfType(ThreatType.THREAT_5).size());
		assertEquals(new Cell(5, 10), playingThreatContext.getThreatsOfType(ThreatType.THREAT_5).get(0).getTargetCell());
		
		ThreatContext opponentThreatContext = threatService.getOrUpdateThreatContext(gameData, GomokuColor.WHITE_COLOR);
		assertEquals(2, opponentThreatContext.getThreatsOfType(ThreatType.DOUBLE_THREAT_4).size());
		assertTrue(opponentThreatContext.getThreatsOfType(ThreatType.DOUBLE_THREAT_4).stream().anyMatch(t -> t.getTargetCell().equals(new Cell(10, 6))));
		assertTrue(opponentThreatContext.getThreatsOfType(ThreatType.DOUBLE_THREAT_4).stream().anyMatch(t -> t.getTargetCell().equals(new Cell(6, 10))));
	}
	
	@Test
	void testDoubleThreat3AndDoubleThreat3() throws JsonProcessingException {
		GameData gameData = GameData.of(GomokuTestsHelper.readGameDto("threatDT3DT3.json"));
		
		ThreatContext playingThreatContext = threatService.getOrUpdateThreatContext(gameData, GomokuColor.BLACK_COLOR);
		assertEquals(4, playingThreatContext.getThreatsOfType(ThreatType.DOUBLE_THREAT_3).size());
		assertTrue(playingThreatContext.getThreatsOfType(ThreatType.DOUBLE_THREAT_3).stream().anyMatch(t -> t.getTargetCell().equals(new Cell(10, 5))));
		assertTrue(playingThreatContext.getThreatsOfType(ThreatType.DOUBLE_THREAT_3).stream().anyMatch(t -> t.getTargetCell().equals(new Cell(9, 6))));
		assertTrue(playingThreatContext.getThreatsOfType(ThreatType.DOUBLE_THREAT_3).stream().anyMatch(t -> t.getTargetCell().equals(new Cell(6, 9))));
		assertTrue(playingThreatContext.getThreatsOfType(ThreatType.DOUBLE_THREAT_3).stream().anyMatch(t -> t.getTargetCell().equals(new Cell(5, 10))));
		
		ThreatContext opponentThreatContext = threatService.getOrUpdateThreatContext(gameData, GomokuColor.WHITE_COLOR);
		assertEquals(4, opponentThreatContext.getThreatsOfType(ThreatType.DOUBLE_THREAT_3).size());
		assertTrue(opponentThreatContext.getThreatsOfType(ThreatType.DOUBLE_THREAT_3).stream().anyMatch(t -> t.getTargetCell().equals(new Cell(10, 6))));
		assertTrue(opponentThreatContext.getThreatsOfType(ThreatType.DOUBLE_THREAT_3).stream().anyMatch(t -> t.getTargetCell().equals(new Cell(11, 5))));
		assertTrue(opponentThreatContext.getThreatsOfType(ThreatType.DOUBLE_THREAT_3).stream().anyMatch(t -> t.getTargetCell().equals(new Cell(7, 9))));
		assertTrue(opponentThreatContext.getThreatsOfType(ThreatType.DOUBLE_THREAT_3).stream().anyMatch(t -> t.getTargetCell().equals(new Cell(6, 10))));
	}
	
	@Test
	void testDoubleThreat2AndDoubleThreat2() throws JsonProcessingException {
		GameData gameData = GameData.of(GomokuTestsHelper.readGameDto("threatDT2DT2.json"));
		
		ThreatContext playingThreatContext = threatService.getOrUpdateThreatContext(gameData, GomokuColor.BLACK_COLOR);
		assertEquals(18, playingThreatContext.getThreatsOfType(ThreatType.DOUBLE_THREAT_2).size());
		
		ThreatContext opponentThreatContext = threatService.getOrUpdateThreatContext(gameData, GomokuColor.WHITE_COLOR);
		assertEquals(18, opponentThreatContext.getThreatsOfType(ThreatType.DOUBLE_THREAT_2).size());
	}
	
	@Test
	void testThreatContextUpdate() throws JsonProcessingException {
		
		GameData gameData = GameData.of(GomokuTestsHelper.readGameDto("testAddMoveUpdate.json"));
		
		testContextUpdate(gameData);
		
		gameData.addMove(new Cell(10, 5), GomokuColor.WHITE_COLOR);
		testContextUpdate(gameData);
		
		gameData.addMove(new Cell(10, 7), GomokuColor.BLACK_COLOR);
		testContextUpdate(gameData);
		
		gameData.addMove(new Cell(8, 5), GomokuColor.WHITE_COLOR);
		testContextUpdate(gameData);
		
		gameData.addMove(new Cell(6, 7), GomokuColor.BLACK_COLOR);
		gameData.addMove(new Cell(5, 6), GomokuColor.WHITE_COLOR);
		gameData.addMove(new Cell(7, 7), GomokuColor.BLACK_COLOR);
		gameData.removeMove(new Cell(7, 7));
		gameData.removeMove(new Cell(5, 6));
		gameData.removeMove(new Cell(6, 7));
		testContextUpdate(gameData);
		
		gameData.removeMove(new Cell(8, 5));
		gameData.removeMove(new Cell(10, 7));
		testContextUpdate(gameData);
		
		gameData.removeMove(new Cell(10, 5));
		testContextUpdate(gameData);
	}
	
	@Test
	void testThreatContextUpdate2() {
		
		GameData gameData = new GameData(15);
		testContextUpdate(gameData);
		
		gameData.addMove(new Cell(7, 7), GomokuColor.BLACK_COLOR);
		testContextUpdate(gameData);
		
		gameData.addMove(new Cell(8, 8), GomokuColor.WHITE_COLOR);
		testContextUpdate(gameData);
		
		gameData.addMove(new Cell(7, 9), GomokuColor.BLACK_COLOR);
		testContextUpdate(gameData);
		
		gameData.addMove(new Cell(7, 6), GomokuColor.WHITE_COLOR);
		testContextUpdate(gameData);
		
		gameData.addMove(new Cell(6, 8), GomokuColor.BLACK_COLOR);
		testContextUpdate(gameData);
		
	}

	private void testContextUpdate(GameData gameData) {
		GameData gameDataCopy = new GameData(gameData);
		compareThreatLists(gameDataCopy, gameData, GomokuColor.BLACK_COLOR, ThreatType.THREAT_5);
		compareThreatLists(gameDataCopy, gameData, GomokuColor.WHITE_COLOR, ThreatType.THREAT_5);
		compareThreatLists(gameDataCopy, gameData, GomokuColor.BLACK_COLOR, ThreatType.THREAT_4);
		compareThreatLists(gameDataCopy, gameData, GomokuColor.WHITE_COLOR, ThreatType.THREAT_4);
		compareThreatLists(gameDataCopy, gameData, GomokuColor.BLACK_COLOR, ThreatType.THREAT_3);
		compareThreatLists(gameDataCopy, gameData, GomokuColor.WHITE_COLOR, ThreatType.THREAT_3);
		compareThreatLists(gameDataCopy, gameData, GomokuColor.BLACK_COLOR, ThreatType.THREAT_2);
		compareThreatLists(gameDataCopy, gameData, GomokuColor.WHITE_COLOR, ThreatType.THREAT_2);
		compareThreatLists(gameDataCopy, gameData, GomokuColor.BLACK_COLOR, ThreatType.DOUBLE_THREAT_4);
		compareThreatLists(gameDataCopy, gameData, GomokuColor.WHITE_COLOR, ThreatType.DOUBLE_THREAT_4);
		compareThreatLists(gameDataCopy, gameData, GomokuColor.BLACK_COLOR, ThreatType.DOUBLE_THREAT_3);
		compareThreatLists(gameDataCopy, gameData, GomokuColor.WHITE_COLOR, ThreatType.DOUBLE_THREAT_3);
		compareThreatLists(gameDataCopy, gameData, GomokuColor.BLACK_COLOR, ThreatType.DOUBLE_THREAT_2);
		compareThreatLists(gameDataCopy, gameData, GomokuColor.WHITE_COLOR, ThreatType.DOUBLE_THREAT_2);
	}
	
	private void compareThreatLists(GameData gameDataCopy, GameData gameData, int color, ThreatType threatType) {
		List<Threat> referenceList = threatService.getOrUpdateThreatContext(gameDataCopy, color).getThreatsOfType(threatType);
		List<Threat> testedList = threatService.getOrUpdateThreatContext(gameData, color).getThreatsOfType(threatType);
		if (referenceList.size() < testedList.size()) {
			List<Threat> excessList = testedList.stream().filter(t -> !referenceList.contains(t)).toList();
			fail("Excessing threats: " + excessList);
		}
		if (referenceList.size() > testedList.size()) {
			List<Threat> missingList = referenceList.stream().filter(t -> !testedList.contains(t)).toList();
			fail("Missing threats: " + missingList);
		}
	}
}
