package se.gustavkarlsson.aurora_notifier.web_service.app;


import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import io.dropwizard.Application;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.joda.time.Duration;
import se.gustavkarlsson.aurora_notifier.common.domain.KpIndexReport;
import se.gustavkarlsson.aurora_notifier.web_service.config.AuroraNotifierWebServiceConfiguration;
import se.gustavkarlsson.aurora_notifier.web_service.health.ProviderHealthCheck;
import se.gustavkarlsson.aurora_notifier.web_service.providers.AggregateKpIndexProvider;
import se.gustavkarlsson.aurora_notifier.web_service.providers.CachingProvider;
import se.gustavkarlsson.aurora_notifier.web_service.providers.Provider;
import se.gustavkarlsson.aurora_notifier.web_service.providers.kp_index.NationalWeatherServiceKpIndexProvider;
import se.gustavkarlsson.aurora_notifier.web_service.providers.kp_index.SpaceWeatherLiveKpIndexProvider;
import se.gustavkarlsson.aurora_notifier.web_service.resources.KpIndexResource;

import java.util.Arrays;

public class AuroraNotifierWebServiceApplication extends Application<AuroraNotifierWebServiceConfiguration> {

	private static final String APPLICATION_NAME = "Aurora Notifier Web Service";

	private AuroraNotifierWebServiceConfiguration configuration;
	private MetricRegistry metrics;
	private JerseyEnvironment jersey;
	private HealthCheckRegistry healthChecks;

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
		this.configuration = configuration;
		this.metrics = environment.metrics();
		this.jersey = environment.jersey();
		this.healthChecks = environment.healthChecks();

		setupKpIndexResource();
		setupHealthChecks();
	}

	private void setupHealthChecks() {
		final ProviderHealthCheck nwsKpIndex = new ProviderHealthCheck(new NationalWeatherServiceKpIndexProvider());
		final ProviderHealthCheck swlKpIndex = new ProviderHealthCheck(new SpaceWeatherLiveKpIndexProvider());
		healthChecks.register("nwsKpIndex", nwsKpIndex);
		healthChecks.register("swlKpIndex", swlKpIndex);
	}

	private void setupKpIndexResource() {
		final Provider<KpIndexReport> kpIndexProvider = createKpIndexProvider();
		final KpIndexResource kpIndexResource = new KpIndexResource(kpIndexProvider);
		jersey.register(kpIndexResource);
	}

	private Provider<KpIndexReport> createKpIndexProvider() {
		Provider<KpIndexReport> nws = new CachingProvider<>(new NationalWeatherServiceKpIndexProvider(metrics), Duration.standardMinutes(configuration.getKpIndexCacheInvalidationMinutes()));
		Provider<KpIndexReport> swl = new CachingProvider<>(new SpaceWeatherLiveKpIndexProvider(metrics), Duration.standardMinutes(configuration.getKpIndexCacheInvalidationMinutes()));
		return new AggregateKpIndexProvider(Arrays.asList(nws, swl));
	}
}
