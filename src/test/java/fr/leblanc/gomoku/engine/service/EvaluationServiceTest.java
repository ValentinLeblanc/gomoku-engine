package fr.leblanc.gomoku.engine.service;

import static org.junit.Assert.assertTrue;

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
			"threat5.json",
			"doubleThreat4.json",
			"doubleThreat3DoubleThreat3.json",
			"threat4DoubleThreat3.json",
			"doubleThreat4AndThreat5.json" 
		})
	void testEval(String arg) throws JsonProcessingException {

		GameDto gameDto = GomokuTestsHelper.readGameDto(arg);

		assertTrue(evaluationService.computeEvaluation(DataWrapper.of(gameDto), EngineConstants.BLACK_COLOR) >= 100);

	}

}
