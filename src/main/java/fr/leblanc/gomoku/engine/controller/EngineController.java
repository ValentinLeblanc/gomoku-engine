package fr.leblanc.gomoku.engine.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import fr.leblanc.gomoku.engine.model.GomokuColor;
import fr.leblanc.gomoku.engine.service.EngineService;

@RestController
public class EngineController {

	@Autowired
	private EngineService engineService;

	@PostMapping("/checkWin")
	public String checkWin(@RequestBody String body) throws JSONException {

		JSONObject request = new JSONObject(body);

		int boardSize = request.getInt("boardSize");

		int[][] data = new int[boardSize][boardSize];

		for (int rowIndex = 0; rowIndex < boardSize; rowIndex++) {
			for (int columnIndex = 0; columnIndex < boardSize; columnIndex++) {
				data[columnIndex][rowIndex] = GomokuColor.NONE.toNumber();
			}
		}

		JSONArray moves = request.getJSONArray("moves");

		for (int i = 0; i < moves.length(); i++) {
			JSONObject move = moves.getJSONObject(i);
			data[move.getInt("column") - 1][move.getInt("row") - 1] = GomokuColor.valueOf(move.getString("color")).toNumber();
		}
		
		JSONObject response = new JSONObject();
		
		int[][] result = engineService.checkForWin(data);
		
		if (result != null) {
			
			JSONArray jsonResult = new JSONArray();
			
			for (int i = 0; i < result.length; i++) {
				
				JSONObject jsonMove = new JSONObject();
				
				jsonMove.put("column", result[i][0] + 1);
				jsonMove.put("row", result[i][1] + 1);
				
				jsonResult.put(jsonMove);
			}
			
			response.put("result", jsonResult);
		}
		
		return response.toString();

	}

}
