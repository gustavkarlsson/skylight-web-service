package se.gustavkarlsson.aurora_notifier.web_service.health;

import com.hubspot.dropwizard.guice.InjectableHealthCheck;
import se.gustavkarlsson.aurora_notifier.web_service.providers.Provider;

public abstract class ProviderHealthCheck extends InjectableHealthCheck {

	protected final Provider<?> provider;

	public ProviderHealthCheck(Provider<?> provider) {
		this.provider = provider;
	}

	@Override
	protected Result check() throws Exception {
		provider.getValue();
		return Result.healthy();
	}
}
