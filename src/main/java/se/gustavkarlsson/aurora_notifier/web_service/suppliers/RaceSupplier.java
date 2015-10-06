package se.gustavkarlsson.aurora_notifier.web_service.suppliers;

import com.google.inject.Inject;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class RaceSupplier<T> implements Supplier<T> {

	private final Set<Supplier<T>> suppliers = Collections.newSetFromMap(new ConcurrentHashMap<>());

	@Inject
	public RaceSupplier(Set<Supplier<T>> suppliers) {
		checkNotNull(suppliers);
		checkArgument(!suppliers.isEmpty(), "No suppliers");
		checkArgument(!suppliers.contains(null), "One supplier is null");
		this.suppliers.addAll(suppliers);
	}

	@Override
	public T get() throws SupplierException {
		Optional<T> winnerValue = suppliers
				.parallelStream()
				.map(Supplier::get)
				.filter(v -> v != null)
				.findAny();
		if (!winnerValue.isPresent()) {
			throw new SupplierException("No supplier produced a value");
		}
		return winnerValue.get();
	}
}
