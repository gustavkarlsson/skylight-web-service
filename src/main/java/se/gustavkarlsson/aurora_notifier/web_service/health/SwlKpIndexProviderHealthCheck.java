package se.gustavkarlsson.aurora_notifier.web_service.health;

import com.google.inject.Inject;
import se.gustavkarlsson.aurora_notifier.web_service.providers.kp_index.SwlKpIndexProvider;

public class SwlKpIndexProviderHealthCheck extends ProviderHealthCheck {

	@Inject
	public SwlKpIndexProviderHealthCheck(SwlKpIndexProvider provider) {
		super(provider);
	}

	@Override
	public String getName() {
		return "swlKpIndex";
	}
}
