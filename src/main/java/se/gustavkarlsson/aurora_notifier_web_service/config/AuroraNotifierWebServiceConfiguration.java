package se.gustavkarlsson.aurora_notifier_web_service.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

import javax.validation.constraints.Min;

public class AuroraNotifierWebServiceConfiguration extends Configuration {

	@Min(1)
	@JsonProperty
	private int kpIndexCacheInvalidationMinutes = 15;

	public int getKpIndexCacheInvalidationMinutes() {
		return kpIndexCacheInvalidationMinutes;
	}
}
