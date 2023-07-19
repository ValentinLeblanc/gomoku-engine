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
	
	
	// Evaluation
	public static final int WIN_EVALUATION = 10000;
	public static final int THREAT_5_POTENTIAL = 2000;
	public static final int DOUBLE_THREAT_4_POTENTIAL = 500;
	public static final int THREAT_4_DOUBLE_THREAT_3_POTENTIAL = 250;
	public static final int DOUBLE_THREAT_3_DOUBLE_THREAT_3_POTENTIAL = 100;
	public static final int THREAT_4_DOUBLE_THREAT_2_POTENTIAL = 15;
	public static final int DOUBLE_THREAT_3_DOUBLE_THREAT_2_POTENTIAL = 10;
	public static final int DOUBLE_THREAT_3_THREAT_3_POTENTIAL = 10;
	public static final int DOUBLE_THREAT_2_DOUBLE_THREAT_2_POTENTIAL = 5;
	public static final int THREAT_4_POTENTIAL = 20;
	public static final int DOUBLE_THREAT_3_POTENTIAL = 15;
	public static final int THREAT_3_POTENTIAL = 5;
	public static final int DOUBLE_THREAT_2_POTENTIAL = 2;
	public static final CompoThreatType[] COMPO_THREAT_TYPES = {
			new CompoThreatType(ThreatType.THREAT_5, null, true),
			new CompoThreatType(ThreatType.THREAT_5, null, false),
			new CompoThreatType(ThreatType.DOUBLE_THREAT_4, null, true),
			new CompoThreatType(ThreatType.THREAT_4, ThreatType.THREAT_4, true),
			new CompoThreatType(ThreatType.THREAT_4, ThreatType.DOUBLE_THREAT_3, true),
			new CompoThreatType(ThreatType.THREAT_4, ThreatType.DOUBLE_THREAT_2, true),
			new CompoThreatType(ThreatType.DOUBLE_THREAT_4, null, false),
			new CompoThreatType(ThreatType.THREAT_4, ThreatType.THREAT_4, false),
			new CompoThreatType(ThreatType.THREAT_4, ThreatType.DOUBLE_THREAT_3, false),
			new CompoThreatType(ThreatType.THREAT_4, ThreatType.DOUBLE_THREAT_2, false),
			new CompoThreatType(ThreatType.DOUBLE_THREAT_3, ThreatType.DOUBLE_THREAT_3, true),
			new CompoThreatType(ThreatType.DOUBLE_THREAT_3, ThreatType.DOUBLE_THREAT_2, true),
			new CompoThreatType(ThreatType.DOUBLE_THREAT_3, ThreatType.DOUBLE_THREAT_3, false),
			new CompoThreatType(ThreatType.DOUBLE_THREAT_3, ThreatType.DOUBLE_THREAT_2, false),
			new CompoThreatType(ThreatType.DOUBLE_THREAT_2, ThreatType.DOUBLE_THREAT_2, true),
			new CompoThreatType(ThreatType.DOUBLE_THREAT_2, ThreatType.DOUBLE_THREAT_2, false),
			};
	
	private EngineConstants() {
		
	}
}
