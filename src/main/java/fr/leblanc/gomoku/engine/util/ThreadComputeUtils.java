package fr.leblanc.gomoku.engine.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.leblanc.gomoku.engine.exception.ComputationStoppedException;

public class ThreadComputeUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(ThreadComputeUtils.class);

	private ThreadComputeUtils() {
	}
	
	public static <T, R, U extends Callable<R>> R computeFirst(List<T> dataSet, Function<List<T>, U> commandSupplier, int timeout, Consumer<R> afterComputeCallback, int threadPoolSize) throws InterruptedException {
		try {
			List<U> commands = createThreadedCommands(dataSet, commandSupplier, threadPoolSize);
			if (timeout == -1) {
				timeout = Integer.MAX_VALUE;
			}
			if (!commands.isEmpty()) {
				ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(threadPoolSize);
				R result = newFixedThreadPool.invokeAny(commands, timeout, TimeUnit.SECONDS);
				newFixedThreadPool.shutdownNow();
				afterComputeCallback.accept(result);
				return result;
			}
		} catch (ExecutionException e) {
			if (e.getCause() instanceof InterruptedException interruptedException) {
				throw interruptedException;
			}
		} catch (TimeoutException e) {
			if (logger.isDebugEnabled()) {
				logger.debug("ThreadComputeUtils timeout ({}s)", timeout);
			}
		}
		return null;
	}
	
	public static <T, R, U extends Callable<R>> List<R> computeAll(List<T> dataSet, Function<List<T>, U> commandSupplier, int threadPoolSize) throws InterruptedException {
		List<U> commands = createThreadedCommands(dataSet, commandSupplier, threadPoolSize);
		ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(threadPoolSize);
		return newFixedThreadPool.invokeAll(commands).stream().map(r -> {
			try {
				return r.get();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new ComputationStoppedException(e);
			} catch (ExecutionException e) {
				if (e.getCause() instanceof InterruptedException) {
					throw new ComputationStoppedException(e);
				}
				throw new IllegalStateException(e);
			}
		}).toList();
	}
	
	private static <T, R, U extends Callable<R>> List<U> createThreadedCommands(List<T> dataSet, Function<List<T>, U> commandSupplier, int threadPoolSize) {
		List<U> commands = new ArrayList<>();
		Map<Integer, List<T>> batchMap = new HashMap<>();
		Iterator<T> iterator = dataSet.iterator();
		while (iterator.hasNext()) {
			for (int i = 0; i < threadPoolSize && iterator.hasNext(); i++) {
				batchMap.computeIfAbsent(i, key -> new ArrayList<>()).add(iterator.next());
			}
		}
		for (List<T> cells : batchMap.values()) {
			commands.add(commandSupplier.apply(cells));
		}
		return commands;
	}
	
}
