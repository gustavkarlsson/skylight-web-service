package se.gustavkarlsson.aurora_notifier_web_service.providers;

import org.joda.time.DateTime;
import org.joda.time.Duration;

public class TimeCachedProvider<T> implements CachedProvider<T> {

	private final Provider<T> provider;
	private final Duration invalidateDuration;

	private DateTime lastUpdateTime = null;
	private T lastValue;

	public TimeCachedProvider(Provider<T> provider, Duration invalidateDuration) {
		this.provider = provider;
		this.invalidateDuration = invalidateDuration;
	}

	@Override
	public T getValue() throws ProviderException {
		if (isUpdateNeeded()) {
			updateValue();
		}
		return lastValue;
	}

	@Override
	public T getLastValue() throws CacheException {
		if (!hasBeenUpdated()) {
			throw new CacheException("No cached value is available");
		}
		return lastValue;
	}

	private boolean isUpdateNeeded() {
		if (!hasBeenUpdated()) {
			return true;
		}
		return isInvalid();
	}

	private boolean isInvalid() {
		DateTime updateNeededTime = lastUpdateTime.plus(invalidateDuration);
		return DateTime.now().isAfter(updateNeededTime);
	}

	private boolean hasBeenUpdated() {
		return lastUpdateTime != null;
	}

	private void updateValue() throws ProviderException {
		lastValue = provider.getValue();
		lastUpdateTime = new DateTime(DateTime.now());
	}
}
