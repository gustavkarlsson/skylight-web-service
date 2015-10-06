package se.gustavkarlsson.aurora_notifier.web_service.suppliers;

import com.google.inject.Inject;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class RaceSupplier<T> implements Supplier<T> {

	private final Set<Supplier<T>> suppliers = Collections.newSetFromMap(new ConcurrentHashMap<>());
	private ExecutorService executor;

	@Inject
	public RaceSupplier(Set<Supplier<T>> suppliers) {
		checkNotNull(suppliers);
		checkArgument(!suppliers.isEmpty(), "No suppliers");
		checkArgument(!suppliers.contains(null), "One supplier is null");
		this.suppliers.addAll(suppliers);
		executor = Executors.newFixedThreadPool(suppliers.size());
	}

	@Override
	public T get() throws SupplierException {
		ExecutorService es= executor;
		try {
			List<Callable<T>> tasks = new ArrayList<>();
			for (Supplier<T> supplier : suppliers) {
				tasks.add(supplier::get);
			}
			return es.invokeAny(tasks);
		} catch (ExecutionException ex) {
			throw new SupplierException("No supplier successfully produced a result");
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
