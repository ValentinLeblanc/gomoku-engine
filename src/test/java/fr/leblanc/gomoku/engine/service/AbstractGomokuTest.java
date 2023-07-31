package fr.leblanc.gomoku.engine.service;

import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractGomokuTest {

	protected static final Long TEST_GAME_ID = -1l;
	
	@Autowired
	private CacheService cacheService;
	
	@AfterEach
	public void afterEach() {
		cacheService.clearCache(TEST_GAME_ID);
	}
	
}
