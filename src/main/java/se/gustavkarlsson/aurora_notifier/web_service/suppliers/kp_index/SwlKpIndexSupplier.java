package se.gustavkarlsson.aurora_notifier.web_service.suppliers.kp_index;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import se.gustavkarlsson.aurora_notifier.web_service.security.SslSecurityOverrider;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.regex.Pattern;

public class SwlKpIndexSupplier extends WebBasedKpIndexSupplier {
	private static final String URL = "https://www.spaceweatherlive.com/en/auroral-activity/the-kp-index";
	private static final String CSS_PATH = "body > div.body > div > div > div.col-sx-12.col-sm-8 > h5 > a:nth-child(1)";
	private static final Pattern PK_INDEX_PATTERN = Pattern.compile("(0\\+?|[1-8](-|\\+)?|9-?)");

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
		if (!PK_INDEX_PATTERN.matcher(text).matches()) {
			throw new IllegalArgumentException("Invalid Kp index: '" + text + "'");
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
