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

import fr.leblanc.gomoku.engine.model.messaging.GameDto;

public class GomokuTestsHelper {

	private GomokuTestsHelper() {
		
	}
	
	public static GameDto readGameDto(String resourceName) throws JsonProcessingException {
		InputStream inputStream = GomokuTestsHelper.class.getClassLoader().getResourceAsStream(resourceName);

		String jsonGame = new BufferedReader(new InputStreamReader(inputStream)).lines()
				.collect(Collectors.joining("\n"));

		ObjectMapper objectMapper = new ObjectMapper();

		return objectMapper.readValue(jsonGame, GameDto.class);
	}
	
	public static void writeGameDto(GameDto game, String fileName) {
		
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))){
			ObjectMapper objectMapper = new ObjectMapper();
			
			writer.write(objectMapper.writeValueAsString(game));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
