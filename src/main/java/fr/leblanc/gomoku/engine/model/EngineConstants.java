package fr.leblanc.gomoku.engine.model;

public class EngineConstants {

	public static final int NONE_COLOR = 0;
	public static final int BLACK_COLOR = 1;
	public static final int WHITE_COLOR = -1;
	
	public static final int[] DOWN_VECTOR = {0, 1};
	public static final int[] RIGHT_VECTOR = {1, 0};
	public static final int[] DOWN_RIGHT_VECTOR = {1, 1};
	public static final int[] DOWN_LEFT_VECTOR = {-1, 1};
	
	public static final int[][] VECTORS = { DOWN_VECTOR, RIGHT_VECTOR, DOWN_RIGHT_VECTOR, DOWN_LEFT_VECTOR };
	public static final int[] COLORS = { BLACK_COLOR, WHITE_COLOR };

	public static final int HORIZONTAL = 0;
	public static final int VERTICAL = 1;
	public static final int DIAGONAL1 = 2;
	public static final int DIAGONAL2 = 3;

	private EngineConstants() {
		
	}
}
