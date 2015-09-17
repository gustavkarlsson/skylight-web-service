package se.gustavkarlsson.aurora_notifier_web_service.providers;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

public class CachingProvider<T> implements Provider<T> {

	private static final Logger logger = LoggerFactory.getLogger(CachingProvider.class);

	private final Provider<T> provider;
	private final Duration invalidateDuration;

	private DateTime lastUpdateTime = null;
	private T cached = null;

	public CachingProvider(Provider<T> provider, Duration invalidateDuration) {
		this.provider = checkNotNull(provider);
		this.invalidateDuration = checkNotNull(invalidateDuration);
	}

	@Override
	public T getValue() throws ProviderException {
		if (needsUpdate()) {
			try {
				update();
			} catch (ProviderException e) {
				logger.warn("Failed to update value. Falling back to cached");
			}
		}
		if (!cachedExists()) {
			throw new ProviderException("No cached value exists");
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
