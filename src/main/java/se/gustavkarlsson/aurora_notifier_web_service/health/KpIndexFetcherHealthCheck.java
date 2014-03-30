package se.gustavkarlsson.aurora_notifier_web_service.health;

import com.codahale.metrics.health.HealthCheck;
import se.gustavkarlsson.aurora_notifier_web_service.services.fetcher.kp_index.CachingKpIndexFetcher;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

public class KpIndexFetcherHealthCheck extends HealthCheck {

	private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
	private static final Validator validator = factory.getValidator();

	private final CachingKpIndexFetcher fetcher;

	public KpIndexFetcherHealthCheck(CachingKpIndexFetcher fetcher) {
		this.fetcher = fetcher;
	}

	@Override
	protected Result check() throws Exception {
		fetcher.fetch();
		return Result.healthy();
	}
}