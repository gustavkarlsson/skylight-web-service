package se.gustavkarlsson.aurora_notifier.web_service.health;

import com.codahale.metrics.health.HealthCheck;
import se.gustavkarlsson.aurora_notifier.common.domain.KpIndexWsReport;
import se.gustavkarlsson.aurora_notifier.web_service.providers.Provider;

public class ProviderHealthCheck extends HealthCheck {

	private final Provider<KpIndexWsReport> provider;

	public ProviderHealthCheck(Provider<KpIndexWsReport> provider) {
		this.provider = provider;
	}

	@Override
	protected Result check() throws Exception {
		provider.getValue();
		return Result.healthy();
	}
}
