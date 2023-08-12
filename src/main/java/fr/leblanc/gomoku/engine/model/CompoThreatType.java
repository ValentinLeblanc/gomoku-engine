package fr.leblanc.gomoku.engine.model;

import java.util.List;
import java.util.Objects;

import fr.leblanc.gomoku.engine.service.EvaluationService;

public class CompoThreatType {

	public static final List<CompoThreatType> COMPO_THREAT_TYPES = List.of(
			CompoThreatType.of(ThreatType.THREAT_5, null, true),
			CompoThreatType.of(ThreatType.THREAT_5, null, false),
			CompoThreatType.of(ThreatType.DOUBLE_THREAT_4, null, true),
			CompoThreatType.of(ThreatType.THREAT_4, ThreatType.THREAT_4, true),
			CompoThreatType.of(ThreatType.THREAT_4, ThreatType.DOUBLE_THREAT_3, true),
			CompoThreatType.of(ThreatType.THREAT_4, ThreatType.DOUBLE_THREAT_2, true),
			CompoThreatType.of(ThreatType.DOUBLE_THREAT_4, null, false),
			CompoThreatType.of(ThreatType.THREAT_4, ThreatType.THREAT_4, false),
			CompoThreatType.of(ThreatType.THREAT_4, ThreatType.DOUBLE_THREAT_3, false),
			CompoThreatType.of(ThreatType.THREAT_4, ThreatType.DOUBLE_THREAT_2, false),
			CompoThreatType.of(ThreatType.DOUBLE_THREAT_3, ThreatType.DOUBLE_THREAT_3, true),
			CompoThreatType.of(ThreatType.DOUBLE_THREAT_3, ThreatType.DOUBLE_THREAT_2, true),
			CompoThreatType.of(ThreatType.DOUBLE_THREAT_3, ThreatType.THREAT_3, true),
			CompoThreatType.of(ThreatType.DOUBLE_THREAT_3, ThreatType.DOUBLE_THREAT_3, false),
			CompoThreatType.of(ThreatType.DOUBLE_THREAT_3, ThreatType.DOUBLE_THREAT_2, false),
			CompoThreatType.of(ThreatType.DOUBLE_THREAT_3, ThreatType.THREAT_3, false),
			CompoThreatType.of(ThreatType.DOUBLE_THREAT_3, null, true),
			CompoThreatType.of(ThreatType.DOUBLE_THREAT_3, null, false),
			CompoThreatType.of(ThreatType.THREAT_4, null, true),
			CompoThreatType.of(ThreatType.THREAT_4, null, false),
			CompoThreatType.of(ThreatType.DOUBLE_THREAT_2, ThreatType.DOUBLE_THREAT_2, true),
			CompoThreatType.of(ThreatType.DOUBLE_THREAT_2, ThreatType.DOUBLE_THREAT_2, false),
			CompoThreatType.of(ThreatType.THREAT_3, null, true),
			CompoThreatType.of(ThreatType.THREAT_3, null, false),
			CompoThreatType.of(ThreatType.THREAT_2, null, true),
			CompoThreatType.of(ThreatType.THREAT_2, null, false));
	
	private ThreatType threatType1;
	private ThreatType threatType2;
	private boolean isPlaying;
	private int potential;
	private int level;

	private CompoThreatType(ThreatType threatType1, ThreatType threatType2, boolean isPlaying) {
		super();
		this.threatType1 = threatType1;
		this.threatType2 = threatType2;
		this.isPlaying = isPlaying;
		computePotential();
		level = computeLevel();
	}

	private int computeLevel() {
		if (ThreatType.THREAT_5.equals(threatType1)) {
			return 0;
		}
		if (ThreatType.DOUBLE_THREAT_4.equals(threatType1)
				|| ThreatType.THREAT_4.equals(threatType1) && ThreatType.THREAT_4.equals(threatType2)
				|| ThreatType.THREAT_4.equals(threatType1) && ThreatType.DOUBLE_THREAT_3.equals(threatType2)) {
			return 1;
		}
		if (ThreatType.THREAT_4.equals(threatType1) && ThreatType.DOUBLE_THREAT_2.equals(threatType2)
				|| ThreatType.DOUBLE_THREAT_3.equals(threatType1) && ThreatType.DOUBLE_THREAT_3.equals(threatType2)
				|| ThreatType.DOUBLE_THREAT_3.equals(threatType1) && ThreatType.DOUBLE_THREAT_2.equals(threatType2)
				|| ThreatType.DOUBLE_THREAT_3.equals(threatType1) && ThreatType.THREAT_3.equals(threatType2)) {
			return 2;
		}
		if (ThreatType.DOUBLE_THREAT_2.equals(threatType1) && ThreatType.DOUBLE_THREAT_2.equals(threatType2)
				|| ThreatType.THREAT_4.equals(threatType1) && threatType2 == null
				|| ThreatType.THREAT_3.equals(threatType1) && threatType2 == null
				|| ThreatType.DOUBLE_THREAT_3.equals(threatType1) && threatType2 == null) {
			return 3;
		}
		if (ThreatType.THREAT_2.equals(threatType1) && threatType2 == null) {
			return 4;
		}
		throw new IllegalStateException("CompoThreatType level not defined: " + this);
	}

	private void computePotential() {
		if (!isPlaying) {
			potential = (int) (getRawPotential() / 1.5);
		} else {
			potential = getRawPotential();
		}
	}

	public List<CompoThreatType> getSimilarOrBetterCompoThreatTypes(boolean isMember, boolean withSimilar) {
		boolean isThreatPlaying = isMember ? isPlaying : !isPlaying;
		return COMPO_THREAT_TYPES.stream().filter(t -> t.isPlaying == isThreatPlaying).filter(t -> withSimilar ? t.level <= this.level : t.level < this.level).toList();
	}

	public ThreatType getThreatType1() {
		return threatType1;
	}

	public ThreatType getThreatType2() {
		return threatType2;
	}

	public boolean isPlaying() {
		return isPlaying;
	}

	public static CompoThreatType of(ThreatType type1, ThreatType type2, boolean isPlaying) {
		return new CompoThreatType(type1, type2, isPlaying);
	}

	public int getPotential() {
		return potential;
	}

	private int getRawPotential() {
		if (ThreatType.THREAT_5.equals(threatType1)) {
			return EvaluationService.THREAT_5_POTENTIAL;
		}

		if (ThreatType.DOUBLE_THREAT_4.equals(threatType1)) {
			return EvaluationService.DOUBLE_THREAT_4_POTENTIAL;
		}

		if (ThreatType.THREAT_4.equals(threatType1)) {
			if (ThreatType.THREAT_4.equals(threatType2)) {
				return EvaluationService.DOUBLE_THREAT_4_POTENTIAL;
			}
			if (ThreatType.DOUBLE_THREAT_3.equals(threatType2)) {
				return EvaluationService.THREAT_4_DOUBLE_THREAT_3_POTENTIAL;
			}
			if (ThreatType.DOUBLE_THREAT_2.equals(threatType2)) {
				return EvaluationService.THREAT_4_DOUBLE_THREAT_2_POTENTIAL;
			}
			if (threatType2 == null) {
				return EvaluationService.THREAT_4_POTENTIAL;
			}
		}

		if (ThreatType.DOUBLE_THREAT_3.equals(threatType1)) {
			if (ThreatType.DOUBLE_THREAT_3.equals(threatType2)) {
				return EvaluationService.DOUBLE_THREAT_3_DOUBLE_THREAT_3_POTENTIAL;
			}
			if (ThreatType.DOUBLE_THREAT_2.equals(threatType2)) {
				return EvaluationService.DOUBLE_THREAT_3_DOUBLE_THREAT_2_POTENTIAL;
			}
			if (ThreatType.THREAT_3.equals(threatType2)) {
				return EvaluationService.DOUBLE_THREAT_3_THREAT_3_POTENTIAL;
			}
			if (threatType2 == null) {
				return EvaluationService.DOUBLE_THREAT_3_POTENTIAL;
			}
		}
		if (ThreatType.DOUBLE_THREAT_2.equals(threatType1) && ThreatType.DOUBLE_THREAT_2.equals(threatType2)) {
			return EvaluationService.DOUBLE_THREAT_2_DOUBLE_THREAT_2_POTENTIAL;
		}
		if (ThreatType.THREAT_3.equals(threatType1) && threatType2 == null) {
			return EvaluationService.THREAT_3_POTENTIAL;
		}
		
		if (ThreatType.THREAT_2.equals(threatType1) && threatType2 == null) {
			return EvaluationService.THREAT_2_POTENTIAL;
		}
		
		throw new IllegalStateException("CompoThreatType potential not defined: " + this);
	}

	@Override
	public int hashCode() {
		return Objects.hash(isPlaying, threatType1, threatType2);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CompoThreatType other = (CompoThreatType) obj;
		return isPlaying == other.isPlaying && threatType1 == other.threatType1 && threatType2 == other.threatType2;
	}

	@Override
	public String toString() {
		return "CompoThreatType [threatType1=" + threatType1 + ", threatType2=" + threatType2 + ", isPlaying="
				+ isPlaying + "]";
	}
	
}
