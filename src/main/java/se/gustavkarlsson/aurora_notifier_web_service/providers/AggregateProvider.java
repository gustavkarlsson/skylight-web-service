package se.gustavkarlsson.aurora_notifier_web_service.providers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class AggregateProvider<T> implements Provider<T> {

	private final List<Provider<T>> providers = new ArrayList<>();

	public AggregateProvider(Provider<T>... providers) {
		for (Provider<T> provider : providers) {
			this.providers.add(provider);
		}
	}

	public AggregateProvider(Collection<Provider<T>> providers) {
		this.providers.addAll(providers);
	}

	public AggregateProvider() {
	}

	public void addProvider(Provider<T> provider) {
		providers.add(provider);
	}

	protected List<T> getValues() throws ProviderException {
		List<T> values = new ArrayList<>();
		for (Provider<T> provider : providers) {
			// TODO Handle ProviderExceptions
			values.add(provider.getValue());
		}
		return values;
	}
}
