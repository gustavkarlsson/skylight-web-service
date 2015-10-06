package se.gustavkarlsson.aurora_notifier.web_service.providers;

import com.google.inject.BindingAnnotation;
import com.google.inject.Inject;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.gustavkarlsson.aurora_notifier.common.domain.Timestamped;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

public class CachingProvider<T> implements Provider<Timestamped<T>> {

	private static final Logger logger = LoggerFactory.getLogger(CachingProvider.class);

	private final Provider<T> provider;
	private final Duration invalidateDuration;

	private Timestamped<T> cached = null;

	@Inject
	public CachingProvider(Provider<T> provider, @CacheDuration Duration cacheDuration) {
		this.provider = checkNotNull(provider);
		this.invalidateDuration = checkNotNull(cacheDuration);
		checkArgument(cacheDuration.getMillis() >= 0, "Duration is negative: " + cacheDuration);
	}

	@Override
	public Timestamped<T> getValue() throws ProviderException {
		if (!isValid()) {
			try {
				update();
			} catch (ProviderException e) {
				logger.warn("Failed to update value. Falling back to cached value", e);
			}
		}
		if (!cachedExists()) {
			throw new ProviderException("No cached value exists");
		}
		return cached;
	}

	private boolean isValid() {
		return cachedExists() && DateTime.now().minus(invalidateDuration).isBefore(cached.getTimestamp());
	}

	private boolean cachedExists() {
		return cached != null;
	}

	private void update() throws ProviderException {
		cached = new Timestamped<>(provider.getValue());
	}

	@BindingAnnotation
	@Target({ FIELD, PARAMETER, METHOD }) @Retention(RUNTIME)
	public static @interface CacheDuration {}
}
