package fr.leblanc.gomoku.engine.service;

import static org.junit.Assert.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.core.JsonProcessingException;

import fr.leblanc.gomoku.engine.model.DataWrapper;
import fr.leblanc.gomoku.engine.model.messaging.GameDto;
import fr.leblanc.gomoku.engine.util.GomokuTestsHelper;

@SpringBootTest
class EvaluationServiceTest {

	@Autowired
	private EvaluationService evaluationService;

	@ParameterizedTest
	@ValueSource(strings = {
			"testAttack1.json",
			"testAttack2.json",
			"testAttack3.json",
			"testAttack4.json",
			"testAttack5.json",
//			"testAttack6.json",
			"testAttack7.json"
//			"testAttack8.json"
		})
	void testAttack(String arg) throws JsonProcessingException {

		GameDto gameDto = GomokuTestsHelper.readGameDto(arg);

		assertTrue(evaluationService.computeEvaluation(DataWrapper.of(gameDto)) >= 100);

	}
	
	@ParameterizedTest
	@ValueSource(strings = {
			"testCannotAttack1.json",
			"testCannotAttack2.json"
		})
	void testCannotAttack(String arg) throws JsonProcessingException {
		
		GameDto gameDto = GomokuTestsHelper.readGameDto(arg);
		
		assertTrue(evaluationService.computeEvaluation(DataWrapper.of(gameDto)) < 100);
		
	}
	
	@ParameterizedTest
	@ValueSource(strings = {
			"testCannotDefend1.json"
		})
	void testCannotDefend(String arg) throws JsonProcessingException {
		
		GameDto gameDto = GomokuTestsHelper.readGameDto(arg);
		
		assertTrue(evaluationService.computeEvaluation(DataWrapper.of(gameDto)) < -100);
		
	}

}
