package se.gustavkarlsson.aurora_notifier_web_service.app;


import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import io.dropwizard.Application;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.joda.time.Duration;
import se.gustavkarlsson.aurora_notifier.common.domain.KpIndexWsReport;
import se.gustavkarlsson.aurora_notifier_web_service.config.AuroraNotifierWebServiceConfiguration;
import se.gustavkarlsson.aurora_notifier_web_service.health.ProviderHealthCheck;
import se.gustavkarlsson.aurora_notifier_web_service.providers.AggregateKpIndexProvider;
import se.gustavkarlsson.aurora_notifier_web_service.providers.CachingProvider;
import se.gustavkarlsson.aurora_notifier_web_service.providers.Provider;
import se.gustavkarlsson.aurora_notifier_web_service.providers.kp_index.NationalWeatherServiceKpIndexProvider;
import se.gustavkarlsson.aurora_notifier_web_service.providers.kp_index.SpaceWeatherLiveKpIndexProvider;
import se.gustavkarlsson.aurora_notifier_web_service.resources.KpIndexResource;

import java.util.Arrays;

public class AuroraNotifierWebServiceApplication extends Application<AuroraNotifierWebServiceConfiguration> {

	private static final String APPLICATION_NAME = "Aurora Notifier Web Service";

	public static void main(String[] args) throws Exception {
		new AuroraNotifierWebServiceApplication().run(args);
	}

	@Override
	public String getName() {
		return APPLICATION_NAME;
	}

	@Override
	public void initialize(Bootstrap<AuroraNotifierWebServiceConfiguration> bootstrap) {
	}

	@Override
	public void run(AuroraNotifierWebServiceConfiguration configuration, Environment environment) {
		MetricRegistry metrics = environment.metrics();
		JerseyEnvironment jersey = environment.jersey();
		HealthCheckRegistry healthChecks = environment.healthChecks();

		final Provider<KpIndexWsReport> kpIndexProvider = createKpIndexProvider(configuration, metrics);
		final KpIndexResource kpIndexResource = new KpIndexResource(kpIndexProvider, metrics);
		jersey.register(kpIndexResource);

		final ProviderHealthCheck healthCheck = new ProviderHealthCheck(kpIndexProvider);
		healthChecks.register("kpIndexProvider", healthCheck);
	}

	private Provider<KpIndexWsReport> createKpIndexProvider(AuroraNotifierWebServiceConfiguration configuration, MetricRegistry metrics) {
		Provider<KpIndexWsReport> nws = new CachingProvider<>(new NationalWeatherServiceKpIndexProvider(metrics), Duration.standardMinutes(configuration.getKpIndexCacheInvalidationMinutes()));
		Provider<KpIndexWsReport> swl = new CachingProvider<>(new SpaceWeatherLiveKpIndexProvider(metrics), Duration.standardMinutes(configuration.getKpIndexCacheInvalidationMinutes()));
		return new AggregateKpIndexProvider(Arrays.asList(nws, swl));
	}
}