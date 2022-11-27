package fr.leblanc.gomoku.engine.controller;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashSet;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.google.gson.Gson;

import fr.leblanc.gomoku.engine.model.messaging.GameDto;

@SpringBootTest
@AutoConfigureMockMvc
class EngineControllerTest {

	@Autowired
	private MockMvc mockMvc;
	
	@Test
	void checkWinRequestTest() throws Exception {

		GameDto gameDto = new GameDto(15, new HashSet<>(), null);

		Gson gson = new Gson();
		String json = gson.toJson(gameDto);

		MvcResult result = mockMvc.perform(post("/checkWin")
				.content(json)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		assertEquals("{\"winMoves\":[],\"win\":false}", result.getResponse().getContentAsString());
	}
}
