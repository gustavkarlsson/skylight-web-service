package se.gustavkarlsson.aurora_notifier.web_service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.validation.ValidationMethod;
import java.time.Duration;

class AuroraConfiguration extends Configuration {
	static final String AT_LEAST_ONE_MINUTE = "must be at least 60000 (1 minute)";

	// Parsed as milliseconds or according to ISO-8601
	@JsonProperty
	private Duration updateDelay = Duration.ofMinutes(15);

	Duration getUpdateDelay() {
		return updateDelay;
	}

	void setUpdateDelay(Duration updateDelay) {
		this.updateDelay = updateDelay;
	}

	@ValidationMethod(message= AT_LEAST_ONE_MINUTE)
	@JsonIgnore
	public boolean isUpdateDelayValid() {
		return updateDelay.toMillis() >= Duration.ofMinutes(1).toMillis();
	}
}
