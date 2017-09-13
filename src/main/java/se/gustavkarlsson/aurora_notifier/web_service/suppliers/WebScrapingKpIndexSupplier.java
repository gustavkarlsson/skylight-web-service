package se.gustavkarlsson.aurora_notifier.web_service.suppliers;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.slf4j.LoggerFactory.getLogger;

abstract class WebScrapingKpIndexSupplier implements Supplier<Float> {
	private static final Logger logger = getLogger(WebScrapingKpIndexSupplier.class);

	private static final String BEGINNING_OF_STRING_TOKEN = "\\A";

	private final Timer getTimer;
	private final Meter exceptionsMeter;
	private final URL url;

	WebScrapingKpIndexSupplier(MetricRegistry metrics, URL url) {
		checkNotNull(metrics);
		checkNotNull(url);
		getTimer = createGetTimer(metrics);
		exceptionsMeter = createExceptionsMeter(metrics);
		this.url = url;
	}

	private Timer createGetTimer(MetricRegistry metrics) {
		return metrics.timer(MetricRegistry.name(getClass(), "get"));
	}

	private Meter createExceptionsMeter(MetricRegistry metrics) {
		return metrics.meter(MetricRegistry.name(getClass(), "exceptions"));
	}

	@Override
	public Float get() {
		try (Timer.Context ignored = getTimer.time()) {
			logger.debug("Getting content from {}", url);
			String urlContent = getUrlContent(url);
			logger.debug("Parsing KP index from content");
			float kpIndex = parseKpIndex(urlContent);
			logger.debug("Parsed KP index: {}", kpIndex);
			return kpIndex;
		} catch (Exception e) {
			exceptionsMeter.mark();
			logger.warn("Failed to get KP index", e);
			return null;
		}
	}

	private static String getUrlContent(URL url) throws IOException {
		try (InputStream stream = url.openStream()) {
			Scanner scanner = new Scanner(stream, StandardCharsets.UTF_8.name());
			scanner.useDelimiter(BEGINNING_OF_STRING_TOKEN);
			return scanner.hasNext() ? scanner.next() : "";
		}
	}

	static URL parseUrl(String url) {
		try {
			return new URL(url);
		} catch (MalformedURLException e) {
			throw new IllegalStateException("URL constant not valid", e);
		}
	}

	protected abstract float parseKpIndex(String urlContent);
}
