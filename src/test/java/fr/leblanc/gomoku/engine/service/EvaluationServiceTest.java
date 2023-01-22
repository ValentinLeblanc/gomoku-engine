package fr.leblanc.gomoku.engine.service;

import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.core.JsonProcessingException;

import fr.leblanc.gomoku.engine.model.DataWrapper;
import fr.leblanc.gomoku.engine.model.EngineConstants;
import fr.leblanc.gomoku.engine.model.messaging.GameDto;
import fr.leblanc.gomoku.engine.util.GomokuTestsHelper;

@SpringBootTest
class EvaluationServiceTest {

	@Autowired
	private EvaluationService evaluationService;

	@ParameterizedTest
	@ValueSource(strings = {
			"evalDT4_T5.json",
			"evalT5.json",
			"evalDT4.json",
			"evalT4T4.json",
			"evalT4DT3.json",
			"evalT4DT3_T4.json",
			"evalDT3DT3.json",
			"evalDT3DT3_T5.json"
		})
	void testAttack(String arg) throws JsonProcessingException {
		GameDto gameDto = GomokuTestsHelper.readGameDto(arg);
		assertTrue(evaluationService.computeEvaluation(DataWrapper.of(gameDto)) >= 100);
	}
	
	@Test
	void DT3DT3_DT4() throws JsonProcessingException {
		GameDto gameDto = GomokuTestsHelper.readGameDto("evalDT3DT3_DT4.json");
		assertTrue(evaluationService.computeEvaluation(DataWrapper.of(gameDto)) >= EngineConstants.DOUBLE_THREAT_3_DOUBLE_THREAT_3_POTENTIAL / 2);
	}
	
	@Test
	void evalDT3DT2_DT3() throws JsonProcessingException {
		GameDto gameDto = GomokuTestsHelper.readGameDto("evalDT3DT2_DT3.json");
		assertTrue(evaluationService.computeEvaluation(DataWrapper.of(gameDto)) <= EngineConstants.DOUBLE_THREAT_3_DOUBLE_THREAT_2_POTENTIAL);
	}
	
	@Test
	void eval_T4T4() throws JsonProcessingException {
		GameDto gameDto = GomokuTestsHelper.readGameDto("eval_T4T4.json");
		assertTrue(evaluationService.computeEvaluation(DataWrapper.of(gameDto)) <= -50);
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
			"eval_T4_DT3_T4_DT3.json",
			"eval_T5.json",
			"eval_DT4.json"
		})
	void testCannotDefend(String arg) throws JsonProcessingException {
		GameDto gameDto = GomokuTestsHelper.readGameDto(arg);
		assertTrue(evaluationService.computeEvaluation(DataWrapper.of(gameDto)) < -75);
	}

}
