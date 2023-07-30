package fr.leblanc.gomoku.engine.service.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Service;

import fr.leblanc.gomoku.engine.service.ComputationService;
import fr.leblanc.gomoku.engine.util.TypedAction;

@Service
public class ComputationServiceImpl implements ComputationService {
	
	private final ThreadLocal<Long> threadComputationId = new ThreadLocal<>();
	private ConcurrentMap<Long, Boolean> isComputingMap = new ConcurrentHashMap<>();
	private ConcurrentMap<Long, Boolean> stopComputationMap = new ConcurrentHashMap<>();

	@Override
	public Long getCurrentThreadComputationId() {
		return threadComputationId.get();
	}
	
	@Override
	public void setCurrentThreadComputationId(Long computationId) {
		threadComputationId.set(computationId);
	}
	
	@Override
	public <T> T startComputation(Long computationId, TypedAction<T> action) throws InterruptedException {
		
		if (isComputing(computationId)) {
			throw new IllegalStateException("Computation is already ongoing: " + computationId);
		}
		
		setCurrentThreadComputationId(computationId);
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
	
}
