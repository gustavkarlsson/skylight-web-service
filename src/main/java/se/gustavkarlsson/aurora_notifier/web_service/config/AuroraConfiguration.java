package se.gustavkarlsson.aurora_notifier.web_service.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.validation.ValidationMethod;
import org.joda.time.Duration;

import javax.validation.Valid;

public class AuroraConfiguration extends Configuration {

	// Parsed as milliseconds or according to ISO-8601
	@JsonProperty
	@Valid
	private Duration kpIndexCacheDuration = Duration.standardMinutes(15);

	public Duration getKpIndexCacheDuration() {
		return kpIndexCacheDuration;
	}

	@ValidationMethod(message="kpIndexCacheDuration must be at least 60000 (1 minute)")
	@JsonIgnore
	public boolean isKpIndexCacheDurationValid() {
		return kpIndexCacheDuration.getMillis() >= Duration.standardMinutes(1).getMillis();
	}
}
