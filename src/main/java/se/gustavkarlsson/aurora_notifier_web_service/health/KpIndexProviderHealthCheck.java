package se.gustavkarlsson.aurora_notifier_web_service.health;

import com.codahale.metrics.health.HealthCheck;
import se.gustavkarlsson.aurora_notifier_web_service.domain.KpIndexHolder;
import se.gustavkarlsson.aurora_notifier_web_service.providers.Provider;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

public class KpIndexProviderHealthCheck extends HealthCheck {

	private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
	private static final Validator validator = factory.getValidator();

	private final Provider<KpIndexHolder> provider;

	public KpIndexProviderHealthCheck(Provider<KpIndexHolder> provider) {
		this.provider = provider;
	}

	@Override
	protected Result check() throws Exception {
		provider.getValue();
		return Result.healthy();
	}
}