package fr.leblanc.gomoku.engine.model;

public record GameSettings(Boolean strikeEnabled, Boolean minMaxEnabled,
		Boolean displayAnalysis, Integer minMaxExtent, Integer minMaxDepth, Integer strikeDepth, Integer strikeTimeout) {

}
