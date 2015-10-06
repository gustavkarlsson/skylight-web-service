package se.gustavkarlsson.aurora_notifier.web_service.providers;

import com.google.inject.Inject;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class RaceProvider<T> implements Provider<T> {

	private final Set<Provider<T>> providers = Collections.newSetFromMap(new ConcurrentHashMap<>());

	@Inject
	public RaceProvider(Set<Provider<T>> providers) {
		checkNotNull(providers);
		checkArgument(!providers.isEmpty(), "No providers");
		checkArgument(!providers.contains(null), "One provider is null");
		this.providers.addAll(providers);
	}

	@Override
	public T getValue() throws ProviderException {
		Optional<T> winnerValue = providers
				.parallelStream()
				.map(RaceProvider::tryGetValue)
				.filter(v -> v != null)
				.findAny();
		if (!winnerValue.isPresent()) {
			throw new ProviderException("No provider produced a value");
		}
		return winnerValue.get();
	}

	private static <T> T tryGetValue(Provider<T> provider) {
		try {
			return provider.getValue();
		} catch (ProviderException e) {
			// Exceptions should be logged in provider
			return null;
		}
	}
}
