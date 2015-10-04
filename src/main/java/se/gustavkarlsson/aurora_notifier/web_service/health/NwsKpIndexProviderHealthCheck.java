package se.gustavkarlsson.aurora_notifier.web_service.health;

import com.google.inject.Inject;
import se.gustavkarlsson.aurora_notifier.web_service.providers.kp_index.NwsKpIndexProvider;

public class NwsKpIndexProviderHealthCheck extends ProviderHealthCheck {

	@Inject
	public NwsKpIndexProviderHealthCheck(NwsKpIndexProvider provider) {
		super(provider);
	}

	@Override
	public String getName() {
		return "nwsKpIndex";
	}
}
