package se.gustavkarlsson.aurora_notifier.web_service.providers.kp_index;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import se.gustavkarlsson.aurora_notifier.common.domain.KpIndexReport;
import se.gustavkarlsson.aurora_notifier.web_service.providers.Provider;
import se.gustavkarlsson.aurora_notifier.web_service.providers.ProviderException;

import java.io.IOException;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

public class SpaceWeatherLiveKpIndexProvider implements Provider<KpIndexReport> {

	private static final String URL = "http://www.spaceweatherlive.com/en/auroral-activity";
	private static final String CSS_PATH = "body > div.body > div > div:nth-child(1) > div.col-sx-12.col-sm-8 > div:nth-child(6) > div:nth-child(1) > div > h5 > a:nth-child(4)";

	public static final Pattern pkIndexPattern = Pattern.compile("(0\\+?|[1-8](-|\\+)?|9-?)");

	private final Timer getValueTimer;
	private final Meter errorsMeter;

	public SpaceWeatherLiveKpIndexProvider(MetricRegistry metrics) {
		checkNotNull(metrics);
		getValueTimer = createGetValueTimer(metrics);
		errorsMeter = createErrorsMeter(metrics);
	}

	public SpaceWeatherLiveKpIndexProvider() {
		this(new MetricRegistry());
	}

	private Timer createGetValueTimer(MetricRegistry metrics) {
		return metrics.timer(MetricRegistry.name(getClass(), "getValue"));
	}

	private Meter createErrorsMeter(MetricRegistry metrics) {
		return metrics.meter(MetricRegistry.name(getClass(), "errors"));
	}

	@Override
	public KpIndexReport getValue() throws ProviderException {
		try (Timer.Context timerContext = getValueTimer.time()) {
			Connection connection = Jsoup.connect(URL);
			Document document = connection.get();
			Elements elements = document.select(CSS_PATH);
			String text = elements.text();
			float kpIndex = parseKpIndex(text);
			long timestampMillis = System.currentTimeMillis();
			KpIndexReport kpIndexReport = new KpIndexReport(kpIndex, timestampMillis);
			timerContext.stop();
			return kpIndexReport;
		} catch (IOException e) {
			errorsMeter.mark();
			throw new ProviderException(e);
		}
	}

	private float parseKpIndex(final String text) throws ProviderException {
		if (!pkIndexPattern.matcher(text).matches()) {
			throw new ProviderException("Invalid Kp index: '" + text + "'");
		}
		float whole = Float.valueOf(String.valueOf(text.charAt(0)));
		String suffix = text.substring(1);
		float extra;
		if ("-".equals(suffix)) {
			extra = -0.33f;
		} else if ("+".equals(suffix)) {
			extra = 0.33f;
		} else {
			extra = 0;
		}
		return whole + extra;
	}
}
