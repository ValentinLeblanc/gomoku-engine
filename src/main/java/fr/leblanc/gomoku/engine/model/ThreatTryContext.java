package fr.leblanc.gomoku.engine.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ThreatTryContext {
	
	private ThreatType threatType1;
	
	private ThreatType threatType2;
	
	private boolean isPlaying;
	
	public ThreatTryContext getNext() {
		
		if (ThreatType.THREAT_5.equals(threatType1)) {
			if (isPlaying) {
				return new ThreatTryContext(ThreatType.THREAT_5, null, !isPlaying);
			}
			return new ThreatTryContext(ThreatType.DOUBLE_THREAT_4, null, !isPlaying);
		}
		
		if (ThreatType.DOUBLE_THREAT_4.equals(threatType1)) {
			return new ThreatTryContext(ThreatType.THREAT_4, ThreatType.THREAT_4, isPlaying);
		}
		
		if (ThreatType.THREAT_4.equals(threatType1)) {
			if (ThreatType.THREAT_4.equals(threatType2)) {
				return new ThreatTryContext(ThreatType.THREAT_4, ThreatType.DOUBLE_THREAT_3, isPlaying);
			}
			if (ThreatType.DOUBLE_THREAT_3.equals(threatType2)) {
				
				if (isPlaying) {
					return new ThreatTryContext(ThreatType.DOUBLE_THREAT_4, null, !isPlaying);
				}
				
				return new ThreatTryContext(ThreatType.DOUBLE_THREAT_3, ThreatType.DOUBLE_THREAT_3, !isPlaying);
			}
		}
		
		if (ThreatType.DOUBLE_THREAT_3.equals(threatType1)) {
			if (ThreatType.DOUBLE_THREAT_3.equals(threatType2)) {
				return new ThreatTryContext(ThreatType.DOUBLE_THREAT_3, ThreatType.DOUBLE_THREAT_2, isPlaying);
			}
			if (ThreatType.DOUBLE_THREAT_2.equals(threatType2)) {
				if (isPlaying) {
					return new ThreatTryContext(ThreatType.DOUBLE_THREAT_3, ThreatType.DOUBLE_THREAT_3, !isPlaying);
				}
				return new ThreatTryContext(ThreatType.DOUBLE_THREAT_2, ThreatType.DOUBLE_THREAT_2, !isPlaying);
			}
		}
		if (ThreatType.DOUBLE_THREAT_2.equals(threatType1)) {
			if (ThreatType.DOUBLE_THREAT_2.equals(threatType2)) {
				if (isPlaying) {
					return new ThreatTryContext(ThreatType.DOUBLE_THREAT_2, ThreatType.DOUBLE_THREAT_2, !isPlaying);
				}
			}
		}
		
		return null;
	}
	
	public int getPotential() {
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

}
