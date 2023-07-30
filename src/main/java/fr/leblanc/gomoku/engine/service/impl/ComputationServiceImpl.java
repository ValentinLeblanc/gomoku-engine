package fr.leblanc.gomoku.engine.service.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.leblanc.gomoku.engine.model.Cell;
import fr.leblanc.gomoku.engine.model.messaging.EngineMessageType;
import fr.leblanc.gomoku.engine.model.messaging.MoveDTO;
import fr.leblanc.gomoku.engine.service.ComputationService;
import fr.leblanc.gomoku.engine.service.WebSocketService;
import fr.leblanc.gomoku.engine.util.TypedAction;

@Service
public class ComputationServiceImpl implements ComputationService {
	
	@Autowired
	private WebSocketService webSocketService;
	
	private final ThreadLocal<Long> threadComputationId = new ThreadLocal<>();

	private ConcurrentMap<Long, Boolean> isComputingMap = new ConcurrentHashMap<>();
	private ConcurrentMap<Long, Boolean> stopComputationMap = new ConcurrentHashMap<>();

	@Override
	public Long getComputationId() {
		return threadComputationId.get();
	}
	
	@Override
	public void setComputationId(Long computationId) {
		threadComputationId.set(computationId);
	}
	
	@Override
	public <T> T doInComputationContext(Long computationId, TypedAction<T> action) throws InterruptedException {
		setComputationId(computationId);
		isComputingMap.put(computationId, Boolean.TRUE);
		try {
			return action.run();
		} finally {
			isComputingMap.put(computationId, Boolean.FALSE);
			threadComputationId.remove();
		}
	}
	
	@Override
	public boolean isComputing(Long computationId) {
		return isComputingMap.computeIfAbsent(computationId, k -> Boolean.FALSE).booleanValue();
	}
	
	@Override
	public boolean isComputationStopped() {
		Long computationId = threadComputationId.get();
		if (computationId != null) {
			return stopComputationMap.computeIfAbsent(computationId, k -> Boolean.FALSE).booleanValue();
		}
		return false;
	}
	
	@Override
	public void stopComputation(Long computationId) {
		try {
			stopComputationMap.put(computationId, Boolean.TRUE);
			// need to wait for all threads to stop
			Thread.sleep(100);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} finally {
			isComputingMap.put(computationId, Boolean.FALSE);
			stopComputationMap.put(computationId, Boolean.FALSE);
		}
	}
	
	@Override
	public void sendMinMaxProgress(int progress) {
		webSocketService.sendMessage(EngineMessageType.MINMAX_PROGRESS, threadComputationId.get(), progress);
	}

	@Override
	public void sendAnalysisMove(Cell analysedMove, int playingColor) {
		webSocketService.sendMessage(EngineMessageType.ANALYSIS_MOVE, threadComputationId.get(), new MoveDTO(analysedMove, playingColor));
	}
	
	@Override
	public void sendStrikeProgress(boolean strikeProgress) {
		webSocketService.sendMessage(EngineMessageType.STRIKE_PROGRESS, threadComputationId.get(), strikeProgress);
	}
}
