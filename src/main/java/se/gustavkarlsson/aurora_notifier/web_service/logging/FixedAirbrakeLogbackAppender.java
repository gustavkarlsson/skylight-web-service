package se.gustavkarlsson.aurora_notifier.web_service.logging;

import airbrake.AirbrakeNotifier;
import airbrake.Backtrace;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.AppenderBase;
import net.anthavio.airbrake.AirbrakeNoticeBuilderUsingFilteredSystemProperties;
import net.anthavio.airbrake.http.HttpServletRequestEnhancerFactory;
import net.anthavio.airbrake.http.RequestEnhancer;
import net.anthavio.airbrake.http.RequestEnhancerFactory;

import java.util.LinkedList;

// TODO submit pull request to github project instead of using this hack
// Copy of net.anthavio.airbrake.AirbrakeLogbackAppender with a minor fix
class FixedAirbrakeLogbackAppender extends AppenderBase<ILoggingEvent> {

	public enum Notify {
		ALL, EXCEPTIONS, OFF;
	}

	private final AirbrakeNotifier airbrakeNotifier;

	private String apiKey;

	private String env;

	private String requestEnhancerFactory;

	private RequestEnhancer requestEnhancer;

	private net.anthavio.airbrake.AirbrakeLogbackAppender.Notify notify = net.anthavio.airbrake.AirbrakeLogbackAppender.Notify.EXCEPTIONS; // default compatible with airbrake-java

	private boolean enabled = true;

	private Backtrace backtraceBuilder = new Backtrace(new LinkedList<String>());

	public FixedAirbrakeLogbackAppender() {
		airbrakeNotifier = new AirbrakeNotifier();
	}

	protected FixedAirbrakeLogbackAppender(AirbrakeNotifier airbrakeNotifier) {
		this.airbrakeNotifier = airbrakeNotifier;
	}

	public void setApiKey(final String apiKey) {
		this.apiKey = apiKey;
	}

	public String getEnv() {
		return env;
	}

	public void setEnv(final String env) {
		this.env = env;
	}

	public Backtrace getBacktraceBuilder() {
		return backtraceBuilder;
	}

	public void setBacktraceBuilder(Backtrace backtraceBuilder) {
		this.backtraceBuilder = backtraceBuilder;
	}

	public String getRequestEnhancerFactory() {
		return requestEnhancerFactory;
	}

	public void setRequestEnhancerFactory(String requestEnhancerFactory) {
		this.requestEnhancerFactory = requestEnhancerFactory;
	}

	public void setUrl(final String url) {
		//TODO this should do addError instead of throwing exception
		if (url == null || !url.startsWith("http")) {
			throw new IllegalArgumentException("Wrong url: " + url);
		}
		airbrakeNotifier.setUrl(url);
	}

	public net.anthavio.airbrake.AirbrakeLogbackAppender.Notify getNotify() {
		return notify;
	}

	public void setNotify(net.anthavio.airbrake.AirbrakeLogbackAppender.Notify notify) {
		this.notify = notify;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	protected void append(final ILoggingEvent event) {
		if (!enabled || notify == net.anthavio.airbrake.AirbrakeLogbackAppender.Notify.OFF) {
			return;
		}

		IThrowableProxy proxy;
		if ((proxy = event.getThrowableProxy()) != null) {
			// Exception are always notified
			Throwable throwable = ((ThrowableProxy) proxy).getThrowable();
			AirbrakeNoticeBuilderUsingFilteredSystemProperties builder = new AirbrakeNoticeBuilderUsingFilteredSystemProperties(apiKey, backtraceBuilder, throwable, env);
			if (requestEnhancer != null) {
				requestEnhancer.enhance(builder);
			}
			airbrakeNotifier.notify(builder.newNotice());

		} else if (notify == net.anthavio.airbrake.AirbrakeLogbackAppender.Notify.ALL) {
			// others only if ALL is set
			StackTraceElement[] stackTrace = event.getCallerData();
			StackTraceElement stackTraceElement = stackTrace.length > 0 ? stackTrace[0] : null; // This line was added
			AirbrakeNoticeBuilderUsingFilteredSystemProperties builder = new AirbrakeNoticeBuilderUsingFilteredSystemProperties(apiKey, event.getFormattedMessage(), stackTraceElement, env);
			if (requestEnhancer != null) {
				requestEnhancer.enhance(builder);
			}
			airbrakeNotifier.notify(builder.newNotice());
		}
	}

	@Override
	public void stop() {
		super.stop();
	}

	@Override
	public void start() {
		if (apiKey == null || apiKey.isEmpty()) {
			addError("API key not set for the appender named [" + name + "].");
		}
		if (env == null || env.isEmpty()) {
			addError("Environment not set for the appender named [" + name + "].");
		}
		if (requestEnhancerFactory != null) {
			RequestEnhancerFactory factory = null;
			try {
				factory = (RequestEnhancerFactory) Class.forName(requestEnhancerFactory).newInstance();
			} catch (Exception x) {
				throw new IllegalStateException("Cannot create " + requestEnhancerFactory, x);
			}
			requestEnhancer = factory.get();
		} else if (HttpServletRequestEnhancerFactory.isServletApi()) {
			requestEnhancer = new HttpServletRequestEnhancerFactory().get();
		}
		super.start();
	}

}
