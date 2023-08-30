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
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadUtils {
	
	private static final int THREAD_POOL_SIZE = 1;

	private static final Logger logger = LoggerFactory.getLogger(ThreadUtils.class);

	private ThreadUtils() {
	}
	
	public static <T, R, U extends Callable<R>> R invokeAny(List<T> dataSet, Function<List<T>, U> commandSupplier, int timeout, Consumer<R> afterProcess) throws InterruptedException {
		try {
			List<U> commands = createThreadedCommands(dataSet, commandSupplier);
			if (timeout == -1) {
				timeout = Integer.MAX_VALUE;
			}
			if (!commands.isEmpty()) {
				ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
				R result = newFixedThreadPool.invokeAny(commands, timeout, TimeUnit.SECONDS);
				newFixedThreadPool.shutdownNow();
				afterProcess.accept(result);
				return result;
			}
		} catch (ExecutionException e) {
			if (e.getCause() instanceof InterruptedException interruptedException) {
				throw interruptedException;
			}
		} catch (TimeoutException e) {
			if (logger.isDebugEnabled()) {
				logger.debug("ThreadUtils timeout ({}s)", timeout);
			}
		}
		return null;
	}
	
	public static <T, R, U extends Callable<R>> List<Future<R>> invokeAll(List<T> dataSet, Function<List<T>, U> commandSupplier) throws InterruptedException {
		List<U> commands = createThreadedCommands(dataSet, commandSupplier);
		ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
		return newFixedThreadPool.invokeAll(commands);
	}
	
	private static <T, R, U extends Callable<R>> List<U> createThreadedCommands(List<T> dataSet, Function<List<T>, U> commandSupplier) {
		List<U> commands = new ArrayList<>();
		Map<Integer, List<T>> batchMap = new HashMap<>();
		Iterator<T> iterator = dataSet.iterator();
		while (iterator.hasNext()) {
			for (int i = 0; i < THREAD_POOL_SIZE && iterator.hasNext(); i++) {
				batchMap.computeIfAbsent(i, key -> new ArrayList<>()).add(iterator.next());
			}
		}
		for (List<T> cells : batchMap.values()) {
			commands.add(commandSupplier.apply(cells));
		}
		return commands;
	}
	
}
