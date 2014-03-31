package se.gustavkarlsson.aurora_notifier_web_service.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

import javax.validation.constraints.Min;

public class AuroraNotifierWebServiceConfiguration extends Configuration {

	@Min(1)
	private int kpIndexCacheInvalidationMinutes = 15;

	@JsonProperty
	public int getKpIndexCacheInvalidationMinutes() {
		return kpIndexCacheInvalidationMinutes;
	}

	@JsonProperty
	public void setKpIndexCacheInvalidationMinutes(int kpIndexCacheInvalidationMinutes) {
		this.kpIndexCacheInvalidationMinutes = kpIndexCacheInvalidationMinutes;
	}
}