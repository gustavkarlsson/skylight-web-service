package se.gustavkarlsson.aurora_notifier.web_service;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import io.dropwizard.setup.Environment;
import se.gustavkarlsson.aurora_notifier.web_service.guice_annotations.Update;
import se.gustavkarlsson.aurora_notifier.web_service.suppliers.NwsKpIndexSupplier;
import se.gustavkarlsson.aurora_notifier.web_service.suppliers.PotsdamKpIndexSupplier;
import se.gustavkarlsson.aurora_notifier.web_service.suppliers.SwlKpIndexSupplier;

import javax.inject.Singleton;
import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

class AuroraModule extends AbstractModule {

	@Override
	protected void configure() {
		Multibinder<Supplier<Float>> kpIndexSuppliersBinder = Multibinder.newSetBinder(binder(), new TypeLiteral<Supplier<Float>>() {});
		kpIndexSuppliersBinder.addBinding().to(NwsKpIndexSupplier.class);
		kpIndexSuppliersBinder.addBinding().to(SwlKpIndexSupplier.class);
		kpIndexSuppliersBinder.addBinding().to(PotsdamKpIndexSupplier.class);
	}

	@Provides
	@Singleton
	@Update
	public Duration provideUpdateDelay(AuroraConfiguration config) {
		return config.getUpdateDelay();
	}

	@Provides
	@Singleton
	public MetricRegistry provideMetrics(Environment env) {
		return env.metrics();
	}

	@Provides
	@Singleton
	public ScheduledExecutorService provideScheduledExecutorService() {
		return Executors.newSingleThreadScheduledExecutor();
	}

	@Provides
	@Singleton
	public Clock provideClock() {
		return Clock.systemUTC();
	}
}
