package fr.leblanc.gomoku.engine.service;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.DataWrapper;
import fr.leblanc.gomoku.engine.model.Threat;
import fr.leblanc.gomoku.engine.model.ThreatContext;
import fr.leblanc.gomoku.engine.model.ThreatType;

@Service
public class EvaluationService {

	private static final double THREAT_5_POTENTIAL = 1000;
	private static final double DOUBLE_THREAT_4_POTENTIAL = 500;
	private static final double THREAT_4_DOUBLE_THREAT_3_POTENTIAL = 250;
	private static final double THREAT_4_POTENTIAL = 20;
	private static final double DOUBLE_THREAT_3_POTENTIAL = 15;
	private static final double THREAT_3_POTENTIAL = 5;
	private static final double DOUBLE_THREAT_2_POTENTIAL = 2;
	
	private static final double OPPONENT_FACTOR = 0.5;
	
	@Autowired
	private ThreatContextService threatContextService;
	
	public double computeEvaluation(DataWrapper dataWrapper, int playingColor) {
		double evaluation = 0;
		
		ThreatContext playingThreatContext = threatContextService.computeThreatContext(dataWrapper.getData(), playingColor);
		
		ThreatContext opponentThreatContext = threatContextService.computeThreatContext(dataWrapper.getData(), -playingColor);
		
//		evaluation += computeThreat5Evaluation(playingThreatContext, opponentThreatContext);
//		
//		evaluation += computeThreat4Evaluation(playingThreatContext, opponentThreatContext);
//		
//		evaluation += computeThreat3Evaluation(playingThreatContext, opponentThreatContext);
//		
//		evaluation += computeThreat2Evaluation(playingThreatContext, opponentThreatContext);
		
		evaluation += computeThreatPotential(playingThreatContext, opponentThreatContext);
		
		return evaluation;
	}

	private double computeThreat5Evaluation(ThreatContext playingThreatContext, ThreatContext opponentThreatContext) {
		
		if (!playingThreatContext.getThreatToCellMap().get(ThreatType.THREAT_5).isEmpty()) {
			return THREAT_5_POTENTIAL;
		}
		
		if (opponentThreatContext.getThreatToCellMap().get(ThreatType.THREAT_5).size() > 1) {
			return - THREAT_5_POTENTIAL;
		}
		
		return 0;
	}
	
	private double computeThreat4Evaluation(ThreatContext playingThreatContext, ThreatContext opponentThreatContext) {
		
		if (!playingThreatContext.getThreatToCellMap().get(ThreatType.DOUBLE_THREAT_4).isEmpty()) {
			return DOUBLE_THREAT_4_POTENTIAL;
		}
		
		if (opponentThreatContext.getThreatToCellMap().get(ThreatType.DOUBLE_THREAT_4).size() > 1) {
			return - DOUBLE_THREAT_4_POTENTIAL;
		}
		
		int threat4DoubleThreat3Size = threatContextService.findCombinedThreats(playingThreatContext, ThreatType.THREAT_4, ThreatType.DOUBLE_THREAT_3).size();
		
		if (threat4DoubleThreat3Size > 0) {
			return THREAT_4_DOUBLE_THREAT_3_POTENTIAL * threat4DoubleThreat3Size;
		}
		
		int opponentThreat4DoubleThreat3Size = threatContextService.findCombinedThreats(opponentThreatContext, ThreatType.THREAT_4, ThreatType.DOUBLE_THREAT_3).size();
		
		if (opponentThreat4DoubleThreat3Size > 0) {
			return - THREAT_4_DOUBLE_THREAT_3_POTENTIAL * opponentThreat4DoubleThreat3Size;
		}
		
		return THREAT_4_POTENTIAL * playingThreatContext.getThreatToCellMap().get(ThreatType.THREAT_4).size() - 0.5 *THREAT_4_POTENTIAL * opponentThreatContext.getThreatToCellMap().get(ThreatType.THREAT_4).size();
		
	}

	private double computeThreat3Evaluation(ThreatContext playingThreatContext, ThreatContext opponentThreatContext) {
		
		double evaluation = 0;
		
		evaluation += DOUBLE_THREAT_3_POTENTIAL * playingThreatContext.getThreatToCellMap().get(ThreatType.DOUBLE_THREAT_3).size();
		
		evaluation -= 0.5 * DOUBLE_THREAT_3_POTENTIAL * opponentThreatContext.getThreatToCellMap().get(ThreatType.DOUBLE_THREAT_3).size();
		
		evaluation += THREAT_3_POTENTIAL * playingThreatContext.getThreatToCellMap().get(ThreatType.THREAT_3).size();
		
		evaluation -= 0.5 * THREAT_3_POTENTIAL * opponentThreatContext.getThreatToCellMap().get(ThreatType.THREAT_3).size();
		
		return evaluation;
	}
	
	private double computeThreat2Evaluation(ThreatContext playingThreatContext, ThreatContext opponentThreatContext) {
		
		double evaluation = 0;
		
		for (Cell threat : playingThreatContext.getThreatToCellMap().get(ThreatType.DOUBLE_THREAT_2)) {
			evaluation += DOUBLE_THREAT_2_POTENTIAL;
			evaluation += DOUBLE_THREAT_2_POTENTIAL * (playingThreatContext.getCellToThreatMap().get(threat).get(ThreatType.DOUBLE_THREAT_2).size() - 1);
		}
		
		for (Cell threat : opponentThreatContext.getThreatToCellMap().get(ThreatType.DOUBLE_THREAT_2)) {
			evaluation -= 0.5 * DOUBLE_THREAT_2_POTENTIAL;
			evaluation -= 0.5 * DOUBLE_THREAT_2_POTENTIAL * (opponentThreatContext.getCellToThreatMap().get(threat).get(ThreatType.DOUBLE_THREAT_2).size() - 1);
		}
		
		return evaluation;
	}
	
	private double computeThreatPotential(ThreatContext playingThreatContext, ThreatContext opponentThreatContext) {
		
		double evaluation = 0;
		
		for (Entry<Cell, Map<ThreatType, List<Threat>>> entry : playingThreatContext.getCellToThreatMap().entrySet()) {
			
			Cell cell = entry.getKey();
			
			Map<ThreatType, List<Threat>> cellThreatMap = entry.getValue();
			
			double cellEvaluation = 0;
			
			for (Entry<ThreatType, List<Threat>> subEntry : cellThreatMap.entrySet()) {
				
				ThreatType threatType1 = subEntry.getKey();
				
				List<Threat> threatList1 = subEntry.getValue();
				
				for (Threat threat1 : threatList1) {
					
					double cellThreatTypeEvaluation = threatType1.getPotential();
					
					for (Entry<ThreatType, List<Threat>> subEntry2 : cellThreatMap.entrySet()) {
						ThreatType threatType2 = subEntry2.getKey();
						List<Threat> threatList2 = subEntry2.getValue();
						
						for (Threat threat2 : threatList2) {
							if (threatType1.getValue() >= threatType2.getValue() && !threat1.equals(threat2)) {
								cellThreatTypeEvaluation += threatType1.getPotential() * threatType2.getPotential();
							}
						}
					}
					
					cellEvaluation += cellThreatTypeEvaluation;
				}
			}
			
			evaluation += cellEvaluation;
			
		}
		
		return evaluation;
	}

}
