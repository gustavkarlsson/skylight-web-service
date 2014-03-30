package se.gustavkarlsson.aurora_notifier_web_service.app;


import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import io.dropwizard.Application;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.joda.time.Duration;
import se.gustavkarlsson.aurora_notifier_web_service.services.fetcher.kp_index.CachingKpIndexFetcher;
import se.gustavkarlsson.aurora_notifier_web_service.services.fetcher.kp_index.NationalWeatherServiceKpIndexFetcher;
import se.gustavkarlsson.aurora_notifier_web_service.config.AuroraNotifierWebServiceConfiguration;
import se.gustavkarlsson.aurora_notifier_web_service.health.KpIndexFetcherHealthCheck;
import se.gustavkarlsson.aurora_notifier_web_service.resources.KpIndexResource;

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

		final CachingKpIndexFetcher fetcher = new NationalWeatherServiceKpIndexFetcher(
				Duration.standardMinutes(configuration.getKpIndexUpdateDelayMinutes()), metrics);

		final KpIndexResource kpIndexResource = new KpIndexResource(fetcher, metrics);
		jersey.register(kpIndexResource);

		final KpIndexFetcherHealthCheck healthCheck = new KpIndexFetcherHealthCheck(fetcher);
		healthChecks.register("kpIndexFetcher", healthCheck);
	}
}