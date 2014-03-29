package se.gustavkarlsson.aurora_notifier_web_service.health;

import com.codahale.metrics.health.HealthCheck;
import se.gustavkarlsson.aurora_notifier_web_service.app.fetching.KpIndexFetcher;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

public class KpIndexFetcherHealthCheck extends HealthCheck {

	private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
	private static final Validator validator = factory.getValidator();

	private final KpIndexFetcher fetcher;

	public KpIndexFetcherHealthCheck(KpIndexFetcher fetcher) {
		this.fetcher = fetcher;
	}

	@Override
	protected Result check() throws Exception {
		fetcher.update();
		return Result.healthy();
	}
}