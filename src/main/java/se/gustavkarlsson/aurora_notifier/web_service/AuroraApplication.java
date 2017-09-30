package se.gustavkarlsson.aurora_notifier.web_service;


import com.google.inject.Stage;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
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
		setupGuice(bootstrap);
		setupEnvironmentVariableSubstitution(bootstrap);
	}

	private static void setupGuice(Bootstrap<AuroraConfiguration> bootstrap) {
		GuiceBundle<AuroraConfiguration> guiceBundle = GuiceBundle.<AuroraConfiguration>builder()
				.enableAutoConfig("se.gustavkarlsson.aurora_notifier.web_service")
				.modules(new AuroraModule())
				.build(Stage.PRODUCTION);
		bootstrap.addBundle(guiceBundle);
	}

	private static void setupEnvironmentVariableSubstitution(Bootstrap<AuroraConfiguration> bootstrap) {
		bootstrap.setConfigurationSourceProvider(
				new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
						new EnvironmentVariableSubstitutor()
				)
		);
	}

	@Override
	public void run(AuroraConfiguration configuration, Environment environment) {
	}
}
