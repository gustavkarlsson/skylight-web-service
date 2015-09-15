package se.gustavkarlsson.aurora_notifier_web_service.providers;

import org.joda.time.DateTime;
import org.joda.time.Duration;

public class CachingProvider<T> implements Provider<T> {

	private final Provider<T> provider;
	private final Duration invalidateDuration;

	private DateTime lastUpdateTime = null;
	private T cached;

	public CachingProvider(Provider<T> provider, Duration invalidateDuration) {
		this.provider = provider;
		this.invalidateDuration = invalidateDuration;
	}

	@Override
	public T getValue() throws ProviderException {
		if (needsUpdate()) {
			try {
				update();
			} catch (ProviderException e) {
				// TODO log that we're falling back on cached value
			}
		}
		return cached;
	}

	private boolean needsUpdate() {
		return !cachedExists() || !isValid();
	}

	private boolean isValid() {
		DateTime updateNeededTime = lastUpdateTime.plus(invalidateDuration);
		return DateTime.now().isBefore(updateNeededTime);
	}

	private boolean cachedExists() {
		return lastUpdateTime != null;
	}

	private void update() throws ProviderException {
		cached = provider.getValue();
		lastUpdateTime = new DateTime(DateTime.now());
	}
}
