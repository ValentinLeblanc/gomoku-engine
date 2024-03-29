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
		if (this == THREAT_3) {
			return List.of(THREAT_5, DOUBLE_THREAT_4, THREAT_4, DOUBLE_THREAT_3, DOUBLE_THREAT_2, THREAT_3);
		}
		
		if (this == DOUBLE_THREAT_3) {
			return List.of(THREAT_5, DOUBLE_THREAT_4, THREAT_4, DOUBLE_THREAT_3);
		}
		if (this == DOUBLE_THREAT_2) {
			return List.of(THREAT_5, DOUBLE_THREAT_4, THREAT_4, DOUBLE_THREAT_3, DOUBLE_THREAT_2, THREAT_3);
		}
		throw new IllegalStateException("ThreatType not recognized : " + this);
	}
	
	public List<ThreatType> getBlockingThreatTypes() {
		if (this == THREAT_5) {
			return List.of();
		}
		if (this == DOUBLE_THREAT_4) {
			return List.of(THREAT_5);
		}
		if (this == THREAT_4) {
			return List.of(THREAT_5, THREAT_4);
		}
		if (this == DOUBLE_THREAT_3 || this == THREAT_3) {
			return List.of(THREAT_5, THREAT_4, DOUBLE_THREAT_3);
		}
		if (this == DOUBLE_THREAT_2 || this == THREAT_2) {
			return List.of(THREAT_5, THREAT_4, DOUBLE_THREAT_3, DOUBLE_THREAT_2);
		}
		return List.of();
	}
	
	public List<ThreatType> getKillingThreatTypes() {
		if (this == THREAT_5) {
			return List.of();
		}
		if (this == DOUBLE_THREAT_4 || this == THREAT_4) {
			return List.of(THREAT_5);
		}
		if (this == DOUBLE_THREAT_3 || this == THREAT_3) {
			return List.of(THREAT_5, DOUBLE_THREAT_4, THREAT_4);
		}
		if (this == DOUBLE_THREAT_2 || this == THREAT_2) {
			return List.of(THREAT_5, DOUBLE_THREAT_4, THREAT_4, DOUBLE_THREAT_3);
		}
		return List.of();
	}
	
}