package se.gustavkarlsson.aurora_notifier.web_service;


import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import ru.vyarus.dropwizard.guice.GuiceBundle;

public class AuroraApplication extends Application<AuroraConfiguration> {

	private static final String APPLICATION_NAME = "Aurora Notifier Web Service";

	public static void main(String[] args) throws Exception {
		new AuroraApplication().run(args);
	}

	@Override
	public String getName() {
		return APPLICATION_NAME;
	}

	@Override
	public void initialize(Bootstrap<AuroraConfiguration> bootstrap) {
		GuiceBundle<AuroraConfiguration> guiceBundle = GuiceBundle.<AuroraConfiguration>builder()
				.enableAutoConfig("se.gustavkarlsson.aurora_notifier.web_service")
				.modules(new AuroraModule())
				.build();
		bootstrap.addBundle(guiceBundle);
	}

	@Override
	public void run(AuroraConfiguration configuration, Environment environment) {
	}
}
