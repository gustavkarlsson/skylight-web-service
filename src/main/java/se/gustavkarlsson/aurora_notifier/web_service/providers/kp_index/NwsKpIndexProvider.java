package se.gustavkarlsson.aurora_notifier.web_service.providers.kp_index;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.gustavkarlsson.aurora_notifier.web_service.providers.Provider;
import se.gustavkarlsson.aurora_notifier.web_service.providers.ProviderException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

import static com.google.common.base.Preconditions.checkNotNull;

public class NwsKpIndexProvider implements Provider<Float> {

	private static final Logger logger = LoggerFactory.getLogger(NwsKpIndexProvider.class);

	private static final String URL = "http://services.swpc.noaa.gov/text/wing-kp.txt";

	private final Timer getValueTimer;
	private final Meter exceptionsMeter;

	@Inject
	public NwsKpIndexProvider(MetricRegistry metrics) {
		checkNotNull(metrics);
		getValueTimer = createGetValueTimer(metrics);
		exceptionsMeter = createExceptionsMeter(metrics);
	}

	public NwsKpIndexProvider() {
		this(new MetricRegistry());
	}

	private Timer createGetValueTimer(MetricRegistry metrics) {
		return metrics.timer(MetricRegistry.name(getClass(), "getValue"));
	}

	private Meter createExceptionsMeter(MetricRegistry metrics) {
		return metrics.meter(MetricRegistry.name(getClass(), "exceptions"));
	}

	@Override
	public Float getValue() throws ProviderException {
		try (Timer.Context timerContext = getValueTimer.time()) {
			URL url = new URL(URL);
			String urlContent = getUrlContent(url);
			float kpIndex = parseKpIndex(urlContent);
			timerContext.stop();
			return kpIndex;
		} catch (Exception e) {
			exceptionsMeter.mark();
			logger.warn("Failed to get value", e);
			throw new ProviderException(e);
		}
	}

	private String getUrlContent(URL url) throws IOException {
		InputStream stream = url.openStream();
		Scanner scanner = new Scanner(stream, "UTF-8");
		scanner.useDelimiter("\\A");
		String content = scanner.hasNext() ? scanner.next() : "";
		scanner.close();
		return content;
	}

	private float parseKpIndex(final String content) {
		final String[] lines = content.split("\\n");
		final String lastLine = lines[lines.length - 1];
		final String[] lastLineSplit = lastLine.split("\\s+");
		final String kpIndexString = lastLineSplit[17];
		return Float.parseFloat(kpIndexString);
	}
}
