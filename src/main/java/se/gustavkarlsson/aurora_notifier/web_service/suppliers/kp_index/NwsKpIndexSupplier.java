package se.gustavkarlsson.aurora_notifier.web_service.suppliers.kp_index;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;

import java.net.URL;

import static java.lang.Float.parseFloat;

public class NwsKpIndexSupplier extends WebBasedKpIndexSupplier {
	private static final String URL = "http://services.swpc.noaa.gov/text/wing-kp.txt";
	private static final String NEW_LINE_PATTERN = "\\n";
	private static final String WHITESPACES_PATTERN = "\\s+";
	private static final int VALUE_COLUMN_INDEX = 14;

	@Inject
	NwsKpIndexSupplier(MetricRegistry metrics) {
		this(metrics, parseUrl(URL));
	}

	NwsKpIndexSupplier(MetricRegistry metrics, URL url) {
		super(metrics, url);
	}

	@Override
	protected float parseKpIndex(final String urlContent) {
		final String[] lines = urlContent.split(NEW_LINE_PATTERN);
		final String lastLine = lines[lines.length - 1];
		final String[] lastLineSplit = lastLine.split(WHITESPACES_PATTERN);
		final String kpIndexString = lastLineSplit[VALUE_COLUMN_INDEX];
		return parseFloat(kpIndexString);
	}
}
