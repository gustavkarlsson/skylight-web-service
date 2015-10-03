package se.gustavkarlsson.aurora_notifier.web_service.app;


import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import io.dropwizard.Application;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.joda.time.Duration;
import se.gustavkarlsson.aurora_notifier.common.domain.Timestamped;
import se.gustavkarlsson.aurora_notifier.web_service.config.AuroraConfiguration;
import se.gustavkarlsson.aurora_notifier.web_service.health.ProviderHealthCheck;
import se.gustavkarlsson.aurora_notifier.web_service.providers.CachingProvider;
import se.gustavkarlsson.aurora_notifier.web_service.providers.Provider;
import se.gustavkarlsson.aurora_notifier.web_service.providers.RaceProvider;
import se.gustavkarlsson.aurora_notifier.web_service.providers.kp_index.NationalWeatherServiceKpIndexProvider;
import se.gustavkarlsson.aurora_notifier.web_service.providers.kp_index.SpaceWeatherLiveKpIndexProvider;
import se.gustavkarlsson.aurora_notifier.web_service.resources.KpIndexResource;

import java.util.Arrays;

public class AuroraApplication extends Application<AuroraConfiguration> {

	private static final String APPLICATION_NAME = "Aurora Notifier Web Service";

	private AuroraConfiguration configuration;
	private MetricRegistry metrics;
	private JerseyEnvironment jersey;
	private HealthCheckRegistry healthChecks;

	public static void main(String[] args) throws Exception {
		new AuroraApplication().run(args);
	}

	@Override
	public String getName() {
		return APPLICATION_NAME;
	}

	@Override
	public void initialize(Bootstrap<AuroraConfiguration> bootstrap) {
	}

	@Override
	public void run(AuroraConfiguration configuration, Environment environment) {
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
		final Provider<Timestamped<Float>> kpIndexProvider = createKpIndexProvider();
		final KpIndexResource kpIndexResource = new KpIndexResource(kpIndexProvider);
		jersey.register(kpIndexResource);
	}

	private Provider<Timestamped<Float>> createKpIndexProvider() {
		Provider<Float> nws = new NationalWeatherServiceKpIndexProvider(metrics);
		Provider<Float> swl = new SpaceWeatherLiveKpIndexProvider(metrics);
		Provider<Float> raceProvider = new RaceProvider<>(Arrays.asList(nws, swl));
		return new CachingProvider<>(raceProvider, Duration.standardMinutes(configuration.getKpIndexCacheInvalidationMinutes()));
	}
}
