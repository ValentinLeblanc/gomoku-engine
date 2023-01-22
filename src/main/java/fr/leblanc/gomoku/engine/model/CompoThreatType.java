package fr.leblanc.gomoku.engine.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CompoThreatType {

	private ThreatType threatType1;

	private ThreatType threatType2;

	private boolean isPlaying;

	public static CompoThreatType of(ThreatType type1, ThreatType type2, boolean isPlaying) {
		return new CompoThreatType(type1, type2, isPlaying);
	}

	public CompoThreatType getNext() {

		if (ThreatType.THREAT_5.equals(threatType1)) {
			if (isPlaying) {
				return new CompoThreatType(ThreatType.THREAT_5, null, !isPlaying);
			}
			return new CompoThreatType(ThreatType.DOUBLE_THREAT_4, null, !isPlaying);
		}

		if (ThreatType.DOUBLE_THREAT_4.equals(threatType1)) {
			return new CompoThreatType(ThreatType.THREAT_4, ThreatType.THREAT_4, isPlaying);
		}

		if (ThreatType.THREAT_4.equals(threatType1)) {
			if (ThreatType.THREAT_4.equals(threatType2)) {
				return new CompoThreatType(ThreatType.THREAT_4, ThreatType.DOUBLE_THREAT_3, isPlaying);
			}
			if (ThreatType.DOUBLE_THREAT_3.equals(threatType2)) {

				if (isPlaying) {
					return new CompoThreatType(ThreatType.DOUBLE_THREAT_4, null, !isPlaying);
				}

				return new CompoThreatType(ThreatType.DOUBLE_THREAT_3, ThreatType.DOUBLE_THREAT_3, !isPlaying);
			}
		}

		if (ThreatType.DOUBLE_THREAT_3.equals(threatType1)) {
			if (ThreatType.DOUBLE_THREAT_3.equals(threatType2)) {
				return new CompoThreatType(ThreatType.DOUBLE_THREAT_3, ThreatType.DOUBLE_THREAT_2, isPlaying);
			}
			if (ThreatType.DOUBLE_THREAT_2.equals(threatType2)) {
				if (isPlaying) {
					return new CompoThreatType(ThreatType.DOUBLE_THREAT_3, ThreatType.DOUBLE_THREAT_3, !isPlaying);
				}
				return new CompoThreatType(ThreatType.DOUBLE_THREAT_2, ThreatType.DOUBLE_THREAT_2, !isPlaying);
			}
		}
		if (ThreatType.DOUBLE_THREAT_2.equals(threatType1)) {
			if (ThreatType.DOUBLE_THREAT_2.equals(threatType2)) {
				if (isPlaying) {
					return new CompoThreatType(ThreatType.DOUBLE_THREAT_2, ThreatType.DOUBLE_THREAT_2, !isPlaying);
				}
			}
		}

		return null;
	}

	public int getPotential() {

		if (!isPlaying) {
			return (int) (getRawPotential() / 1.5);
		}

		return getRawPotential();
	}

	private int getRawPotential() {
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
			if (ThreatType.DOUBLE_THREAT_2.equals(threatType2)) {
				return EngineConstants.THREAT_4_DOUBLE_THREAT_2_POTENTIAL;
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

	public List<CompoThreatType> getSimilarOrBetterCompoThreatTypes(boolean isMember, boolean withSimilar) {
		
		boolean isThreatPlaying = isMember ? isPlaying : !isPlaying;
		
		if (ThreatType.THREAT_5.equals(threatType1)) {
			if (!withSimilar) {
				return List.of();
			}
			return List.of(CompoThreatType.of(ThreatType.THREAT_5, null, isThreatPlaying));
		}

		if (ThreatType.THREAT_4.equals(threatType1) && ThreatType.THREAT_4.equals(threatType2)) {
			if (!withSimilar) {
				return List.of(CompoThreatType.of(ThreatType.THREAT_5, null, isThreatPlaying));
			}
			return List.of(CompoThreatType.of(ThreatType.THREAT_5, null, isThreatPlaying),
					CompoThreatType.of(ThreatType.DOUBLE_THREAT_4, null, isThreatPlaying),
					CompoThreatType.of(ThreatType.THREAT_4, ThreatType.THREAT_4, isThreatPlaying),
					CompoThreatType.of(ThreatType.THREAT_4, ThreatType.DOUBLE_THREAT_3, isThreatPlaying));
		}

		if (ThreatType.DOUBLE_THREAT_4.equals(threatType1)) {
			if (!withSimilar) {
				return List.of(CompoThreatType.of(ThreatType.THREAT_5, null, isThreatPlaying));
			}
			return List.of(CompoThreatType.of(ThreatType.THREAT_5, null, isThreatPlaying),
					CompoThreatType.of(ThreatType.DOUBLE_THREAT_4, null, isThreatPlaying),
					CompoThreatType.of(ThreatType.THREAT_4, ThreatType.THREAT_4, isThreatPlaying),
					CompoThreatType.of(ThreatType.THREAT_4, ThreatType.DOUBLE_THREAT_3, isThreatPlaying));
		}
		
		if (ThreatType.THREAT_4.equals(threatType1) && ThreatType.DOUBLE_THREAT_3.equals(threatType2)) {
			if (!withSimilar) {
				return List.of(CompoThreatType.of(ThreatType.THREAT_5, null, isThreatPlaying));
			}
			return List.of(CompoThreatType.of(ThreatType.THREAT_5, null, isThreatPlaying),
					CompoThreatType.of(ThreatType.DOUBLE_THREAT_4, null, isThreatPlaying),
					CompoThreatType.of(ThreatType.THREAT_4, ThreatType.THREAT_4, isThreatPlaying),
			CompoThreatType.of(ThreatType.THREAT_4, ThreatType.DOUBLE_THREAT_3, isThreatPlaying));
		}
		
		if (ThreatType.THREAT_4.equals(threatType1) && ThreatType.DOUBLE_THREAT_2.equals(threatType2)) {
			if (!withSimilar) {
				return List.of(CompoThreatType.of(ThreatType.THREAT_5, null, isThreatPlaying));
			}
			return List.of(CompoThreatType.of(ThreatType.THREAT_5, null, isThreatPlaying),
					CompoThreatType.of(ThreatType.DOUBLE_THREAT_4, null, isThreatPlaying),
					CompoThreatType.of(ThreatType.THREAT_4, ThreatType.THREAT_4, isThreatPlaying),
					CompoThreatType.of(ThreatType.THREAT_4, ThreatType.DOUBLE_THREAT_3, isThreatPlaying));
		}
		
		if (ThreatType.DOUBLE_THREAT_3.equals(threatType1) && ThreatType.DOUBLE_THREAT_3.equals(threatType2)) {
			if (!withSimilar) {
				return List.of(CompoThreatType.of(ThreatType.THREAT_5, null, isThreatPlaying),
						CompoThreatType.of(ThreatType.DOUBLE_THREAT_4, null, isThreatPlaying),
						CompoThreatType.of(ThreatType.THREAT_4, ThreatType.THREAT_4, isThreatPlaying),
						CompoThreatType.of(ThreatType.THREAT_4, ThreatType.DOUBLE_THREAT_3, isThreatPlaying));
			}
			return List.of(CompoThreatType.of(ThreatType.THREAT_5, null, isThreatPlaying),
					CompoThreatType.of(ThreatType.DOUBLE_THREAT_4, null, isThreatPlaying),
					CompoThreatType.of(ThreatType.THREAT_4, ThreatType.THREAT_4, isThreatPlaying),
					CompoThreatType.of(ThreatType.THREAT_4, ThreatType.DOUBLE_THREAT_3, isThreatPlaying),
					CompoThreatType.of(ThreatType.DOUBLE_THREAT_3, ThreatType.DOUBLE_THREAT_3, isThreatPlaying));
		}
		
		if (ThreatType.DOUBLE_THREAT_3.equals(threatType1) && ThreatType.DOUBLE_THREAT_2.equals(threatType2)) {
			if (!withSimilar) {
				return List.of(CompoThreatType.of(ThreatType.THREAT_5, null, isThreatPlaying),
						CompoThreatType.of(ThreatType.DOUBLE_THREAT_4, null, isThreatPlaying),
						CompoThreatType.of(ThreatType.THREAT_4, ThreatType.THREAT_4, isThreatPlaying),
						CompoThreatType.of(ThreatType.THREAT_4, ThreatType.DOUBLE_THREAT_3, isThreatPlaying));
			}
			return List.of(CompoThreatType.of(ThreatType.THREAT_5, null, isThreatPlaying),
					CompoThreatType.of(ThreatType.DOUBLE_THREAT_4, null, isThreatPlaying),
					CompoThreatType.of(ThreatType.THREAT_4, ThreatType.THREAT_4, isThreatPlaying),
					CompoThreatType.of(ThreatType.THREAT_4, ThreatType.DOUBLE_THREAT_3, isThreatPlaying),
					CompoThreatType.of(ThreatType.THREAT_4, ThreatType.DOUBLE_THREAT_2, isThreatPlaying),
					CompoThreatType.of(ThreatType.DOUBLE_THREAT_3, ThreatType.DOUBLE_THREAT_3, isThreatPlaying),
					CompoThreatType.of(ThreatType.DOUBLE_THREAT_3, ThreatType.DOUBLE_THREAT_2, isThreatPlaying));
		}
		
		if (ThreatType.DOUBLE_THREAT_2.equals(threatType1) && ThreatType.DOUBLE_THREAT_2.equals(threatType2)) {
			if (!withSimilar) {
				return List.of(CompoThreatType.of(ThreatType.THREAT_5, null, isThreatPlaying),
						CompoThreatType.of(ThreatType.DOUBLE_THREAT_4, null, isThreatPlaying),
						CompoThreatType.of(ThreatType.THREAT_4, ThreatType.THREAT_4, isThreatPlaying),
						CompoThreatType.of(ThreatType.THREAT_4, ThreatType.DOUBLE_THREAT_3, isThreatPlaying),
						CompoThreatType.of(ThreatType.THREAT_4, ThreatType.DOUBLE_THREAT_2, isThreatPlaying),
						CompoThreatType.of(ThreatType.DOUBLE_THREAT_3, ThreatType.DOUBLE_THREAT_3, isThreatPlaying),
						CompoThreatType.of(ThreatType.DOUBLE_THREAT_3, ThreatType.DOUBLE_THREAT_2, isThreatPlaying));
			}
			return List.of(CompoThreatType.of(ThreatType.THREAT_5, null, isThreatPlaying),
					CompoThreatType.of(ThreatType.DOUBLE_THREAT_4, null, isThreatPlaying),
					CompoThreatType.of(ThreatType.THREAT_4, ThreatType.THREAT_4, isThreatPlaying),
					CompoThreatType.of(ThreatType.THREAT_4, ThreatType.DOUBLE_THREAT_3, isThreatPlaying),
					CompoThreatType.of(ThreatType.THREAT_4, ThreatType.DOUBLE_THREAT_2, isThreatPlaying),
					CompoThreatType.of(ThreatType.DOUBLE_THREAT_3, ThreatType.DOUBLE_THREAT_3, isThreatPlaying),
					CompoThreatType.of(ThreatType.DOUBLE_THREAT_3, ThreatType.DOUBLE_THREAT_2, isThreatPlaying),
					CompoThreatType.of(ThreatType.DOUBLE_THREAT_2, ThreatType.DOUBLE_THREAT_2, isThreatPlaying));
		}

		throw new IllegalStateException("CompoThreatType not implemented : " + this);
	}
	
}
