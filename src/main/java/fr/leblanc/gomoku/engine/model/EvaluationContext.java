package fr.leblanc.gomoku.engine.model;

import java.util.Objects;

public class EvaluationContext {
	
	private GameData gameData;

	private int playingColor;
	private boolean internal = false;
	private boolean useStrikeService = false;
	
	public EvaluationContext(GameData gameData) {
		super();
		this.gameData = gameData;
	}
	
	public EvaluationContext internal() {
		this.internal = true;
		return this;
	}
	
	public EvaluationContext useStrikeService() {
		this.useStrikeService = true;
		return this;
	}

	public int getPlayingColor() {
		return playingColor;
	}

	public void setPlayingColor(int playingColor) {
		this.playingColor = playingColor;
	}
	
	public boolean isUseStrikeService() {
		return useStrikeService;
	}

	public boolean isInternal() {
		return internal;
	}

	public void setLogEnabled(boolean logEnabled) {
		this.internal = logEnabled;
	}

	public GameData getGameData() {
		return gameData;
	}

	public void reversePlayingColor() {
		playingColor = -playingColor;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(gameData, internal, playingColor);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EvaluationContext other = (EvaluationContext) obj;
		return Objects.equals(gameData, other.gameData) && internal == other.internal
				&& playingColor == other.playingColor;
	}

	@Override
	public String toString() {
		return "EvaluationContext [gameData=" + gameData + ", playingColor=" + playingColor + ", external=" + internal + "]";
	}
	
}
