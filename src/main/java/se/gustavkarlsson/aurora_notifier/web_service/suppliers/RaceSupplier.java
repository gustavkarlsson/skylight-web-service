package se.gustavkarlsson.aurora_notifier.web_service.suppliers;

import com.google.inject.Inject;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class RaceSupplier<T> implements Supplier<T> {

	private final Set<Supplier<T>> suppliers;
	private final ExecutorService executor;

	@Inject
	RaceSupplier(Set<Supplier<T>> suppliers) {
		checkNotNull(suppliers);
		checkArgument(!suppliers.isEmpty(), "No suppliers");
		checkArgument(!suppliers.contains(null), "One supplier is null");
		this.suppliers = new HashSet<>(suppliers);
		executor = Executors.newFixedThreadPool(suppliers.size());
	}

	@Override
	public T get() throws SupplierException {
		try {
			List<Callable<T>> tasks = new ArrayList<>();
			for (Supplier<T> supplier : suppliers) {
				tasks.add(supplier::get);
			}
			return executor.invokeAny(tasks);
		} catch (ExecutionException ex) {
			throw new SupplierException("No supplier successfully produced a result", ex);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
