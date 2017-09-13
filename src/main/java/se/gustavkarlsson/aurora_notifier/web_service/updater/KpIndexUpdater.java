package se.gustavkarlsson.aurora_notifier.web_service.updater;

import io.dropwizard.lifecycle.Managed;
import org.slf4j.Logger;
import se.gustavkarlsson.aurora_notifier.common.domain.Timestamped;
import se.gustavkarlsson.aurora_notifier.web_service.guice_annotations.Update;
import se.gustavkarlsson.aurora_notifier.web_service.repository.KpIndexRepository;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Clock;
import java.time.Duration;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.slf4j.LoggerFactory.getLogger;

@Singleton
public class KpIndexUpdater implements Managed {
	private static final Logger logger = getLogger(KpIndexUpdater.class);

	private final ScheduledExecutorService scheduler;
	private final Duration delay;
	private final Set<Supplier<Float>> kpIndexSuppliers;
	private final KpIndexRepository repository;
	private final Clock clock;
	private ScheduledFuture<?> scheduledTask = null;

	@Inject
	public KpIndexUpdater(ScheduledExecutorService scheduler, @Update Duration delay, Set<Supplier<Float>> kpIndexSuppliers, KpIndexRepository repository, Clock clock) {
		this.scheduler = checkNotNull(scheduler);
		this.delay = checkNotNull(delay);
		this.kpIndexSuppliers = checkNotNull(kpIndexSuppliers);
		checkArgument(!kpIndexSuppliers.contains(null), "Null elements not allowed");
		this.repository = checkNotNull(repository);
		this.clock = checkNotNull(clock);
	}

	@Override
	public void start() {
		checkState(scheduledTask == null, "Already started");
		this.scheduledTask = scheduler.scheduleAtFixedRate(this::update, 0, delay.toMillis(), MILLISECONDS);
		logger.info(String.format("Started updating Kp index in background with delay: %s", delay));
	}

	@Override
	public void stop() {
		checkState(scheduledTask != null, "Already stopped");
		this.scheduledTask.cancel(true);
		this.scheduledTask = null;
		logger.info("Stopped updating Kp index in background");
	}

	// Must never throw exception since that will prevent future executions
	public Timestamped<Float> update() {
		try {
			Double averageKpIndex = getAverageKpIndex();
			Timestamped<Float> timestampedKpIndex = averageKpIndex == null
					? null
					: new Timestamped<>(averageKpIndex.floatValue(), clock.millis());
			repository.setLastKpIndex(timestampedKpIndex);
			logger.info("Updated last Kp index to: " + timestampedKpIndex);
			return timestampedKpIndex;
		} catch (Exception e) {
			logger.error("An unknown error occurred while updating last Kp index", e);
			return null;
		}
	}

	private Double getAverageKpIndex() {
		Double value = kpIndexSuppliers.parallelStream()
				.map(Supplier::get)
				.filter(Objects::nonNull)
				.mapToDouble(Float::doubleValue)
				.average()
				.orElse(Double.NaN);
		if (value.isNaN()) {
			return null;
		}
		return value;
	}

}
