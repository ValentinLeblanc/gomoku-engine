package fr.leblanc.gomoku.engine.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.leblanc.gomoku.engine.model.messaging.GameDTO;

public class GomokuTestsHelper {

	private GomokuTestsHelper() {
		
	}
	
	public static GameDTO readGameDto(String resourceName) throws JsonProcessingException {
		InputStream inputStream = GomokuTestsHelper.class.getClassLoader().getResourceAsStream(resourceName);

		try (BufferedReader reader =  new BufferedReader(new InputStreamReader(inputStream))) {
			String jsonGame = reader.lines().collect(Collectors.joining("\n"));
			
			ObjectMapper objectMapper = new ObjectMapper();
			
			return objectMapper.readValue(jsonGame, GameDTO.class);
		} catch (IOException e) {
			throw new IllegalStateException("Error while reading test file");
		}
	}
	
	public static void writeGameDto(GameDTO game, String fileName) {
		
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))){
			ObjectMapper objectMapper = new ObjectMapper();
			
			writer.write(objectMapper.writeValueAsString(game));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
