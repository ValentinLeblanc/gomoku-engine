package fr.leblanc.gomoku.engine.service.impl;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.MoveDto;
import fr.leblanc.gomoku.engine.repository.EngineRepository;
import fr.leblanc.gomoku.engine.service.AnalysisService;

@Service
public class AnalysisServiceImpl implements AnalysisService {

	@Autowired
	private EngineRepository engineRepository;

	@Override
	public void sendAnalysisCell(Cell analysisCell, int color) {
		JSONObject message = new JSONObject();
		
		message.put("type", "ANALYSIS_MOVE");
		message.put("content", new MoveDto(analysisCell, color));
		
		engineRepository.sendMessageToWebApp(message);
	}

	@Override
	public void sendPercentCompleted(Integer percent) {
		JSONObject message = new JSONObject();
		
		message.put("type", "COMPUTE_PROGRESS");
		message.put("content", percent.toString());
		
		engineRepository.sendMessageToWebApp(message);

	}

}
