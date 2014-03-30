package se.gustavkarlsson.aurora_notifier_web_service.services.fetcher.kp_index;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.joda.time.Duration;
import se.gustavkarlsson.aurora_notifier_web_service.domain.KpIndexHolder;
import se.gustavkarlsson.aurora_notifier_web_service.services.fetcher.FetchException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

public class NationalWeatherServiceKpIndexFetcher extends CachingKpIndexFetcher {

	private static final String CHARSET = "UTF-8";
	private static final String URL = "http://www.swpc.noaa.gov/wingkp/wingkp_list.txt";

	private final Timer fetchKpIndexTimer;
	private final Meter errorsMeter;

	public NationalWeatherServiceKpIndexFetcher(Duration minimumDurationBetweenUpdates, MetricRegistry metrics) {
		super(minimumDurationBetweenUpdates);
		fetchKpIndexTimer = createFetchKpIndexTimer(metrics);
		errorsMeter = createErrorsMeter(metrics);
	}

	private Timer createFetchKpIndexTimer(MetricRegistry metrics) {
		return metrics.timer(MetricRegistry.name(getClass(), "fetchKpIndex"));
	}

	private Meter createErrorsMeter(MetricRegistry metrics) {
		return metrics.meter(MetricRegistry.name(getClass(), "errors"));
	}

	@Override
	protected KpIndexHolder fetchKpIndex() throws FetchException {
		try (Timer.Context timerContext = fetchKpIndexTimer.time()) {
			URL url = new URL(URL);
			String urlContent = getUrlContent(url);
			float kpIndexValue = parseKpIndex(urlContent);
			long timestampMillis = System.currentTimeMillis();
			KpIndexHolder kpIndexHolder = new KpIndexHolder(kpIndexValue, timestampMillis);
			return kpIndexHolder;
		} catch (IOException e) {
			errorsMeter.mark();
			throw new FetchException(e);
		}
	}

	private String getUrlContent(URL url) throws IOException {
		InputStream stream = url.openStream();
		Scanner scanner = new Scanner(stream, CHARSET);
		scanner.useDelimiter("\\A");
		String content = scanner.hasNext() ? scanner.next() : "";
		scanner.close();
		return content;
	}

	private float parseKpIndex(final String content) throws FetchException {
		try {
			final String[] lines = content.split("\\n");
			final String lastLine = lines[lines.length - 1];
			final String[] lastLineSplit = lastLine.split("\\s+");
			final String kpIndexString = lastLineSplit[9];
			final float kpIndex = Float.parseFloat(kpIndexString);
			return kpIndex;
		} catch (RuntimeException e) {
			throw new FetchException(e);
		}
	}
}
