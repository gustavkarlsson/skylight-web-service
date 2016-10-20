package se.gustavkarlsson.aurora_notifier.web_service.suppliers.kp_index;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.gustavkarlsson.aurora_notifier.web_service.suppliers.SupplierException;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Float.*;

public class NwsKpIndexSupplier implements Supplier<Float> {

	private static final Logger logger = LoggerFactory.getLogger(NwsKpIndexSupplier.class);

	private static final String URL = "http://services.swpc.noaa.gov/text/wing-kp.txt";
	private static final String BEGINNING_OF_STRING_TOKEN = "\\A";
	private static final String NEW_LINE_PATTERN = "\\n";
	private static final String WHITESPACES_PATTERN = "\\s+";
	private static final int VALUE_COLUMN_INDEX = 14;

	private final Timer getValueTimer;
	private final Meter exceptionsMeter;
	private final URL url;

	@Inject
	NwsKpIndexSupplier(MetricRegistry metrics) {
		this(metrics, getUrl());
	}

	NwsKpIndexSupplier(MetricRegistry metrics, URL url) {
		checkNotNull(metrics);
		checkNotNull(url);
		getValueTimer = createGetValueTimer(metrics);
		exceptionsMeter = createExceptionsMeter(metrics);
		this.url = url;
	}

	private static URL getUrl() {
		try {
			return new URL(URL);
		} catch (MalformedURLException e) {
			throw new IllegalStateException("URL constant not valid", e);
		}
	}

	private Timer createGetValueTimer(MetricRegistry metrics) {
		return metrics.timer(MetricRegistry.name(getClass(), "getValue"));
	}

	private Meter createExceptionsMeter(MetricRegistry metrics) {
		return metrics.meter(MetricRegistry.name(getClass(), "exceptions"));
	}

	@Override
	public Float get() throws SupplierException {
		try (Timer.Context ignored = getValueTimer.time()) {
			String urlContent = getUrlContent(url);
			return parseKpIndex(urlContent);
		} catch (Exception e) {
			exceptionsMeter.mark();
			logger.warn("Failed to get value", e);
			throw new SupplierException(e);
		}
	}

	private static String getUrlContent(URL url) throws IOException {
		try (InputStream stream = url.openStream()) {
			Scanner scanner = new Scanner(stream, StandardCharsets.UTF_8.name());
			scanner.useDelimiter(BEGINNING_OF_STRING_TOKEN);
			return scanner.hasNext() ? scanner.next() : "";
		}
	}

	private float parseKpIndex(final String content) {
		final String[] lines = content.split(NEW_LINE_PATTERN);
		final String lastLine = lines[lines.length - 1];
		final String[] lastLineSplit = lastLine.split(WHITESPACES_PATTERN);
		final String kpIndexString = lastLineSplit[VALUE_COLUMN_INDEX];
		return parseFloat(kpIndexString);
	}
}
