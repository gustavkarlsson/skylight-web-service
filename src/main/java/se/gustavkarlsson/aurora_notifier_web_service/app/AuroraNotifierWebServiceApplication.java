package se.gustavkarlsson.aurora_notifier_web_service.app;


import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.joda.time.Duration;
import se.gustavkarlsson.aurora_notifier_web_service.app.fetching.KpIndexFetcher;
import se.gustavkarlsson.aurora_notifier_web_service.app.fetching.NationalWeatherServiceKpIndexFetcher;
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
	public void run(AuroraNotifierWebServiceConfiguration configuration,
	                Environment environment) {
		final KpIndexFetcher fetcher = new NationalWeatherServiceKpIndexFetcher(Duration.standardMinutes(configuration.getKpIndexUpdateDelayMinutes()));
		final KpIndexResource kpIndexResource = new KpIndexResource(fetcher);
		environment.jersey().register(kpIndexResource);

		final KpIndexFetcherHealthCheck healthCheck =
				new KpIndexFetcherHealthCheck(fetcher);
		environment.healthChecks().register("configuration", healthCheck);
	}
}