package se.gustavkarlsson.aurora_notifier.web_service.providers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class RaceProvider<T> implements Provider<T> {

	private final List<Provider<T>> providers = new ArrayList<>();

	public RaceProvider(Collection<Provider<T>> providers) {
		checkNotNull(providers);
		checkArgument(!providers.isEmpty(), "No providers");
		checkArgument(!providers.contains(null), "One provider is null");
		this.providers.addAll(providers);
	}

	@Override
	public T getValue() throws ProviderException {
		Optional<T> winnerValue = providers.parallelStream().map(Provider::getValue).findAny();
		if (!winnerValue.isPresent()) {
			throw new ProviderException("No provider produced a value");
		}
		return winnerValue.get();
	}
}
