package se.gustavkarlsson.aurora_notifier.web_service.guice;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import io.dropwizard.setup.Environment;
import org.joda.time.Duration;
import se.gustavkarlsson.aurora_notifier.common.domain.Timestamped;
import se.gustavkarlsson.aurora_notifier.web_service.config.AuroraConfiguration;
import se.gustavkarlsson.aurora_notifier.web_service.providers.CachingProvider;
import se.gustavkarlsson.aurora_notifier.web_service.providers.Provider;
import se.gustavkarlsson.aurora_notifier.web_service.providers.RaceProvider;
import se.gustavkarlsson.aurora_notifier.web_service.providers.kp_index.NwsKpIndexProvider;
import se.gustavkarlsson.aurora_notifier.web_service.providers.kp_index.SwlKpIndexProvider;

public class AuroraModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(new TypeLiteral<Provider<Timestamped<Float>>>() {}).to(new TypeLiteral<CachingProvider<Float>>() {});
		bind(new TypeLiteral<Provider<Float>>() {}).to(new TypeLiteral<RaceProvider<Float>>() {});

		Multibinder<Provider<Float>> actionBinder = Multibinder.newSetBinder(binder(), new TypeLiteral<Provider<Float>>() {});
		actionBinder.addBinding().to(NwsKpIndexProvider.class);
		actionBinder.addBinding().to(SwlKpIndexProvider.class);
	}

	@Provides
	@CachingProvider.CacheDuration
	public Duration provideKpIndexCacheDuration(AuroraConfiguration config) {
		return config.getKpIndexCacheDuration();
	}

	@Provides
	public MetricRegistry provideMetrics(Environment env) {
		return env.metrics();
	}
}
