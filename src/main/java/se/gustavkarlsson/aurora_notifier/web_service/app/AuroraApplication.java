package se.gustavkarlsson.aurora_notifier.web_service.app;


import com.google.inject.Module;
import com.hubspot.dropwizard.guice.GuiceBundle;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import se.gustavkarlsson.aurora_notifier.web_service.config.AuroraConfiguration;
import se.gustavkarlsson.aurora_notifier.web_service.guice.AuroraModule;

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
		GuiceBundle<AuroraConfiguration> guiceBundle = GuiceBundle.<AuroraConfiguration>newBuilder()
				.addModule(getModule())
				.enableAutoConfig("se.gustavkarlsson.aurora_notifier.web_service")
				.setConfigClass(AuroraConfiguration.class)
				.build();
		bootstrap.addBundle(guiceBundle);
	}

	@Override
	public void run(AuroraConfiguration configuration, Environment environment) {
	}

	protected Module getModule() {
		return new AuroraModule();
	}
}
