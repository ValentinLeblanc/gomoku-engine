package fr.leblanc.gomoku.engine.model;

import java.util.List;

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
	
	public boolean isDoubleType() {
		return this == DOUBLE_THREAT_2 || this == DOUBLE_THREAT_3 || this == DOUBLE_THREAT_4;
	}
	
	public ThreatType getDoubleThreatType() {
		
		if (this == THREAT_4) {
			return DOUBLE_THREAT_4;
		}
		
		if (this == THREAT_3) {
			return DOUBLE_THREAT_3;
		}
		
		if (this == THREAT_2) {
			return DOUBLE_THREAT_2;
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
	
	public static  int potentialOf(ThreatType threatType1, ThreatType threatType2) {
		if (ThreatType.THREAT_5.equals(threatType1)) {
			return EngineConstants.THREAT_5_POTENTIAL;
		}
		
		if (ThreatType.DOUBLE_THREAT_4.equals(threatType1)) {
			return EngineConstants.DOUBLE_THREAT_4_POTENTIAL;
		}
		
		if (ThreatType.THREAT_4.equals(threatType1)) {
			if (ThreatType.THREAT_4.equals(threatType2)) {
				return EngineConstants.DOUBLE_THREAT_4_POTENTIAL;
			}
			if (ThreatType.DOUBLE_THREAT_3.equals(threatType2)) {
				return EngineConstants.THREAT_4_DOUBLE_THREAT_3_POTENTIAL;
			}
			
		}
		
		if (ThreatType.DOUBLE_THREAT_3.equals(threatType1)) {
			if (ThreatType.DOUBLE_THREAT_3.equals(threatType2)) {
				return EngineConstants.DOUBLE_THREAT_3_DOUBLE_THREAT_3_POTENTIAL;
			}
			if (ThreatType.DOUBLE_THREAT_2.equals(threatType2)) {
				return EngineConstants.DOUBLE_THREAT_3_DOUBLE_THREAT_2_POTENTIAL;
			}
			
		}
		if (ThreatType.DOUBLE_THREAT_2.equals(threatType1)) {
			if (ThreatType.DOUBLE_THREAT_2.equals(threatType2)) {
				return EngineConstants.DOUBLE_THREAT_2_DOUBLE_THREAT_2_POTENTIAL;
			}
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

	public List<ThreatType> getBetterOrEqualThreatTypes() {
		
		if (this == THREAT_5) {
			return List.of(THREAT_5);
		}
		
		if (this == DOUBLE_THREAT_4) {
			return List.of(THREAT_5, DOUBLE_THREAT_4, THREAT_4);
		}
		
		if (this == THREAT_4) {
			return List.of(THREAT_5, DOUBLE_THREAT_4, THREAT_4);
		}
		
		if (this == DOUBLE_THREAT_3) {
			return List.of(THREAT_5, DOUBLE_THREAT_4, THREAT_4, DOUBLE_THREAT_3);
		}
		
		if (this == DOUBLE_THREAT_2) {
			return List.of(THREAT_5, DOUBLE_THREAT_4, THREAT_4, DOUBLE_THREAT_3, DOUBLE_THREAT_2);
		}
		
		throw new IllegalStateException("ThreatType not recognized : " + this);
	}
	
	public List<ThreatType> getBlockingThreatTypes() {
		
		if (this == THREAT_5) {
			return List.of();
		}
		
		if (this == DOUBLE_THREAT_4) {
			return List.of();
		}
		
		if (this == THREAT_4) {
			return List.of(THREAT_4);
		}
		
		if (this == DOUBLE_THREAT_3) {
			return List.of(THREAT_5, THREAT_4, DOUBLE_THREAT_3);
		}
		
		if (this == DOUBLE_THREAT_2) {
			return List.of(THREAT_5, THREAT_4, DOUBLE_THREAT_3);
		}
		
		return List.of();
	}
	
	public List<ThreatType> getKillingThreatTypes() {
		
		if (this == THREAT_5) {
			return List.of();
		}
		
		if (this == DOUBLE_THREAT_4) {
			return List.of(THREAT_5);
		}
		
		if (this == THREAT_4) {
			return List.of(THREAT_5);
		}
		
		if (this == DOUBLE_THREAT_3) {
			return List.of(THREAT_5, DOUBLE_THREAT_4);
		}
		
		if (this == DOUBLE_THREAT_2) {
			return List.of(THREAT_5, DOUBLE_THREAT_4, DOUBLE_THREAT_3);
		}
		
		return List.of();
	}
	
}