package se.gustavkarlsson.aurora_notifier.web_service.suppliers;

import com.codahale.metrics.MetricRegistry;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import se.gustavkarlsson.aurora_notifier.web_service.security.SslSecurityOverrider;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
public class SwlKpIndexSupplier extends WebScrapingKpIndexSupplier {
	private static final String URL = "https://www.spaceweatherlive.com";
	private static final String CSS_PATH = "div#Kp_gauge h4";
	private static final Pattern KP_INDEX_PATTERN = Pattern.compile(".*(\\d).*");

	static {
		SslSecurityOverrider.override();
	}

	@Inject
	SwlKpIndexSupplier(MetricRegistry metrics) {
		this(metrics, parseUrl(URL));
	}

	SwlKpIndexSupplier(MetricRegistry metrics, URL url) {
		super(metrics, url);
	}

	@Override
	protected float parseKpIndex(final String urlContent) {
		Document document = Jsoup.parse(urlContent);
		Elements elements = document.select(CSS_PATH);
		String text = elements.text();
		Matcher matcher = KP_INDEX_PATTERN.matcher(text);
		if (!matcher.matches()) {
			throw new IllegalArgumentException("Invalid Kp index: '" + text + "'");
		}
		return Float.parseFloat(matcher.group(1));
	}
}
