package se.gustavkarlsson.aurora_notifier_web_service.app.fetching;

import org.joda.time.Duration;
import se.gustavkarlsson.aurora_notifier_web_service.domain.KpIndexHolder;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class NationalWeatherServiceKpIndexFetcher extends KpIndexFetcher {

	private static final String CHARSET = "UTF-8";

	public static final String URL = "http://www.swpc.noaa.gov/wingkp/wingkp_list.txt";

	public NationalWeatherServiceKpIndexFetcher(Duration minimumDurationBetweenUpdates) {
		super(minimumDurationBetweenUpdates);
	}

	@Override
	protected KpIndexHolder fetchKpIndex() throws FetchException {
		try {
			URL url = new URL(URL);
			String urlContent = getUrlContent(url);
			float kpIndexValue = parseKpIndex(urlContent);
			long timestampMillis = System.currentTimeMillis();
			KpIndexHolder kpIndexHolder = new KpIndexHolder(kpIndexValue, timestampMillis);
			return kpIndexHolder;
		} catch (MalformedURLException e) {
			throw new FetchException(e);
		} catch (IOException e) {
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
