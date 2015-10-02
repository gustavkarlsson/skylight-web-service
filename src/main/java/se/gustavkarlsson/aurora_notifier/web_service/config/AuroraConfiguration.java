package se.gustavkarlsson.aurora_notifier.web_service.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

import javax.validation.constraints.Min;

public class AuroraConfiguration extends Configuration {

	@Min(1)
	@JsonProperty
	private int kpIndexCacheInvalidationMinutes = 15;

	public int getKpIndexCacheInvalidationMinutes() {
		return kpIndexCacheInvalidationMinutes;
	}
}
