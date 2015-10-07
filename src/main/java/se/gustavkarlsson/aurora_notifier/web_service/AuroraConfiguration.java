package se.gustavkarlsson.aurora_notifier.web_service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.validation.ValidationMethod;
import org.joda.time.Duration;

public class AuroraConfiguration extends Configuration {

	public static final String AT_LEAST_ONE_MINUTE = "must be at least 60000 (1 minute)";

	// Parsed as milliseconds or according to ISO-8601
	@JsonProperty
	private Duration kpIndexCacheDuration = Duration.standardMinutes(15);

	public Duration getKpIndexCacheDuration() {
		return kpIndexCacheDuration;
	}

	public void setKpIndexCacheDuration(Duration kpIndexCacheDuration) {
		this.kpIndexCacheDuration = kpIndexCacheDuration;
	}

	@ValidationMethod(message= AT_LEAST_ONE_MINUTE)
	@JsonIgnore
	public boolean isKpIndexCacheDurationValid() {
		return kpIndexCacheDuration.getMillis() >= Duration.standardMinutes(1).getMillis();
	}
}
