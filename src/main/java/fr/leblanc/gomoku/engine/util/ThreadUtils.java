package fr.leblanc.gomoku.engine.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(ThreadUtils.class);

	private ThreadUtils() {
	}
	
	public static <T, R, U extends Callable<R>> R invokeAny(List<T> dataSet, int threadsInvolved, Function<List<T>, U> commandSupplier, int timeout) throws InterruptedException {
		try {
			List<U> commands = createThreadedCommands(dataSet, threadsInvolved, commandSupplier);
			if (timeout == -1) {
				timeout = Integer.MAX_VALUE;
			}
			if (!commands.isEmpty()) {
				return Executors.newFixedThreadPool(threadsInvolved).invokeAny(commands, timeout, TimeUnit.SECONDS);
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
	
	public static <T, R, U extends Callable<R>> List<Future<R>> invokeAll(List<T> dataSet, int threadsInvolved, Function<List<T>, U> commandSupplier) throws InterruptedException {
		List<U> commands = createThreadedCommands(dataSet, threadsInvolved, commandSupplier);
		return Executors.newFixedThreadPool(threadsInvolved).invokeAll(commands);
	}
	
	private static <T, R, U extends Callable<R>> List<U> createThreadedCommands(List<T> dataSet, int threadsInvolved, Function<List<T>, U> commandSupplier) {
		List<U> commands = new ArrayList<>();
		Map<Integer, List<T>> batchMap = new HashMap<>();
		Iterator<T> iterator = dataSet.iterator();
		while (iterator.hasNext()) {
			for (int i = 0; i < threadsInvolved && iterator.hasNext(); i++) {
				batchMap.computeIfAbsent(i, key -> new ArrayList<>()).add(iterator.next());
			}
		}
		for (List<T> cells : batchMap.values()) {
			commands.add(commandSupplier.apply(cells));
		}
		return commands;
	}
	
}
