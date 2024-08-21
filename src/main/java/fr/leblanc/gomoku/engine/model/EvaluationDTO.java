package fr.leblanc.gomoku.engine.model;

import java.util.Map;

public record EvaluationDTO(Double evaluation, Map<Cell, Double> cellMap) {

}
