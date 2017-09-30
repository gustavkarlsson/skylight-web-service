package se.gustavkarlsson.aurora_notifier.web_service.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.dropwizard.logging.AbstractAppenderFactory;
import io.dropwizard.logging.async.AsyncAppenderFactory;
import io.dropwizard.logging.filter.LevelFilterFactory;
import io.dropwizard.logging.layout.LayoutFactory;
import net.anthavio.airbrake.AirbrakeLogbackAppender;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;

@JsonTypeName("airbrake")
public class AirbrakeAppenderFactory extends AbstractAppenderFactory<ILoggingEvent> {

	private String env = "development";

	@NotBlank
	private String apiKey;

	@NotNull
	private AirbrakeLogbackAppender.Notify notify = AirbrakeLogbackAppender.Notify.ALL;

	@JsonProperty
	public String getEnv() {
		return env;
	}

	@JsonProperty
	public void setEnv(String env) {
		this.env = env;
	}

	@JsonProperty
	public String getApiKey() {
		return apiKey;
	}

	@JsonProperty
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	@JsonProperty
	public AirbrakeLogbackAppender.Notify getNotify() {
		return notify;
	}

	@JsonProperty
	public void setNotify(AirbrakeLogbackAppender.Notify notify) {
		this.notify = notify;
	}

	@Override
	public Appender<ILoggingEvent> build(LoggerContext context, String applicationName, LayoutFactory<ILoggingEvent> layoutFactory,
										 LevelFilterFactory<ILoggingEvent> levelFilterFactory, AsyncAppenderFactory<ILoggingEvent> asyncAppenderFactory) {
		FixedAirbrakeLogbackAppender appender = new FixedAirbrakeLogbackAppender();
		appender.setContext(context);
		appender.setName("airbrake-appender");
		appender.setEnv(env);
		appender.setApiKey(apiKey);
		if (notify != null) {
			appender.setNotify(notify);
		}
		appender.addFilter(levelFilterFactory.build(threshold));
		getFilterFactories().forEach(f -> appender.addFilter(f.build()));
		appender.start();
		return wrapAsync(appender, asyncAppenderFactory);
	}
}
