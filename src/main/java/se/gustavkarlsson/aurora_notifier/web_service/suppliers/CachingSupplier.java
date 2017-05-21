package se.gustavkarlsson.aurora_notifier.web_service.suppliers;

import com.google.inject.BindingAnnotation;
import com.google.inject.Inject;
import se.gustavkarlsson.aurora_notifier.common.domain.Timestamped;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

public class CachingSupplier<T> implements Supplier<Timestamped<T>> {

	private final Supplier<T> supplier;
	private final Duration invalidateDuration;

	private Timestamped<T> cached = null;

	@Inject
	CachingSupplier(Supplier<T> supplier, @CacheDuration Duration cacheDuration) {
		this.supplier = checkNotNull(supplier);
		this.invalidateDuration = checkNotNull(cacheDuration);
		checkArgument(!cacheDuration.isNegative(), "Duration is negative: " + cacheDuration);
	}

	@Override
	public Timestamped<T> get() {
		if (!isValid()) {
			update();
		}
		return cached;
	}

	private boolean isValid() {
		return cachedExists() && !hasExpired();
	}

	private boolean hasExpired() {
		Instant cachedTimestamp = Instant.ofEpochMilli(cached.getTimestamp());
		Instant expiryTime = cachedTimestamp.plus(invalidateDuration);
		Instant now = Instant.now();
		return !expiryTime.isAfter(now);
	}

	private boolean cachedExists() {
		return cached != null;
	}

	private void update() {
		T value = supplier.get();
		cached = new Timestamped<>(value);
	}

	@BindingAnnotation
	@Target({ FIELD, PARAMETER, METHOD }) @Retention(RUNTIME)
	public @interface CacheDuration {}
}
