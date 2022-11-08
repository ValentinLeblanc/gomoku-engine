package fr.leblanc.gomoku.engine.model;

public enum ThreatType {

	THREAT_5,
	DOUBLE_THREAT_4,
	DOUBLE_THREAT_3,
	DOUBLE_THREAT_2,
	THREAT_4,
	THREAT_3,
	THREAT_2;
	
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
			return EngineConstants.THREAT_5_POTENTIAL;
		}
		
		if (this == DOUBLE_THREAT_4) {
			return EngineConstants.DOUBLE_THREAT_4_POTENTIAL;
		}
		
		if (this == THREAT_4) {
			return EngineConstants.THREAT_4_POTENTIAL;
		}
		
		if (this == DOUBLE_THREAT_3) {
			return EngineConstants.DOUBLE_THREAT_3_POTENTIAL;
		}
		
		if (this == THREAT_3) {
			return EngineConstants.THREAT_3_POTENTIAL;
		}
		
		if (this == DOUBLE_THREAT_2) {
			return EngineConstants.DOUBLE_THREAT_2_POTENTIAL;
		}
		
		return 0;
	}
	
}