package se.gustavkarlsson.aurora_notifier_web_service.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class AuroraNotifierWebServiceConfiguration extends Configuration {
	@Min(1)
	private long kpIndexUpdateDelayMinutes = 15;

	@JsonProperty
	public long getKpIndexUpdateDelayMinutes() {
		return kpIndexUpdateDelayMinutes;
	}

	@JsonProperty
	public void setKpIndexUpdateDelayMinutes(long kpIndexUpdateDelayMinutes) {
		this.kpIndexUpdateDelayMinutes = kpIndexUpdateDelayMinutes;
	}
}