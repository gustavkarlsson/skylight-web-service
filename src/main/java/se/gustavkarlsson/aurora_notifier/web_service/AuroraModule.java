package se.gustavkarlsson.aurora_notifier.web_service;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import io.dropwizard.setup.Environment;
import java.time.Duration;
import se.gustavkarlsson.aurora_notifier.common.domain.Timestamped;
import se.gustavkarlsson.aurora_notifier.web_service.suppliers.CachingSupplier;
import se.gustavkarlsson.aurora_notifier.web_service.suppliers.RaceSupplier;
import se.gustavkarlsson.aurora_notifier.web_service.suppliers.kp_index.NwsKpIndexSupplier;
import se.gustavkarlsson.aurora_notifier.web_service.suppliers.kp_index.SwlKpIndexSupplier;

import java.util.function.Supplier;

class AuroraModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(new TypeLiteral<Supplier<Timestamped<Float>>>() {}).to(new TypeLiteral<CachingSupplier<Float>>() {});
		bind(new TypeLiteral<Supplier<Float>>() {}).to(new TypeLiteral<RaceSupplier<Float>>() {});

		Multibinder<Supplier<Float>> actionBinder = Multibinder.newSetBinder(binder(), new TypeLiteral<Supplier<Float>>() {});
		actionBinder.addBinding().to(NwsKpIndexSupplier.class);
		actionBinder.addBinding().to(SwlKpIndexSupplier.class);
	}

	@Provides
	@CachingSupplier.CacheDuration
	public Duration provideKpIndexCacheDuration(AuroraConfiguration config) {
		return config.getKpIndexCacheDuration();
	}

	@Provides
	public MetricRegistry provideMetrics(Environment env) {
		return env.metrics();
	}
}
