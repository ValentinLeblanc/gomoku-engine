package fr.leblanc.gomoku.engine.model;

public enum ThreatType {

	THREAT_5,
	DOUBLE_THREAT_4,
	DOUBLE_THREAT_3,
	DOUBLE_THREAT_2,
	THREAT_4,
	THREAT_3,
	THREAT_2;
	
	private static final double THREAT_5_POTENTIAL = 1000;
	private static final double DOUBLE_THREAT_4_POTENTIAL = 500;
	private static final double THREAT_4_DOUBLE_THREAT_3_POTENTIAL = 250;
	private static final double THREAT_4_POTENTIAL = 4;
	private static final double DOUBLE_THREAT_3_POTENTIAL = 15;
	private static final double THREAT_3_POTENTIAL = 3;
	private static final double DOUBLE_THREAT_2_POTENTIAL = 2;
	private static final double THREAT_2_POTENTIAL = 2;


	public static ThreatType valueOf(int type) {
		
		if (type == 2) {
			return THREAT_2;
		}
		
		if (type == 3) {
			return THREAT_3;
		}
		
		if (type == 4) {
			return THREAT_4;
		}
		
		if (type == 5) {
			return THREAT_5;
		}
		
		return null;
	}
	
	public static ThreatType valueDoubleOf(int type) {
		
		if (type == 2) {
			return DOUBLE_THREAT_2;
		}
		
		if (type == 3) {
			return DOUBLE_THREAT_3;
		}
		
		if (type == 4) {
			return DOUBLE_THREAT_4;
		}
		
		return null;
	}
	
	public int getValue() {
		if (this == THREAT_5) {
			return 5;
		}
		
		if (this == THREAT_4) {
			return 4;
		}
		
		if (this == THREAT_3) {
			return 3;
		}
		
		if (this == THREAT_2) {
			return 2;
		}
		
		return 0;
	}
	
	public double getPotential() {
		if (this == THREAT_5) {
			return THREAT_5_POTENTIAL;
		}
		
		if (this == DOUBLE_THREAT_4) {
			return THREAT_4_POTENTIAL * THREAT_4_POTENTIAL;
		}
		
		if (this == THREAT_4) {
			return THREAT_4_POTENTIAL;
		}
		
		if (this == DOUBLE_THREAT_3) {
			return THREAT_3_POTENTIAL * THREAT_3_POTENTIAL;
		}
		
		if (this == THREAT_3) {
			return THREAT_3_POTENTIAL;
		}
		
		if (this == DOUBLE_THREAT_2) {
			return THREAT_2_POTENTIAL * THREAT_2_POTENTIAL;
		}
		
		if (this == THREAT_2) {
			return THREAT_2_POTENTIAL;
		}
		
		return 0;
	}
	
}