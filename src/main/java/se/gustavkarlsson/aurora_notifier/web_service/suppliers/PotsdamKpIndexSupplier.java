package se.gustavkarlsson.aurora_notifier.web_service.suppliers;

import com.codahale.metrics.MetricRegistry;
import se.gustavkarlsson.aurora_notifier.web_service.security.SslSecurityOverrider;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.URL;
import java.util.Arrays;

@Singleton
public class PotsdamKpIndexSupplier extends WebScrapingKpIndexSupplier {
	private static final String URL = "http://www-app3.gfz-potsdam.de/kp_index/qlyymm.tab";

	static {
		SslSecurityOverrider.override();
	}

	@Inject
	PotsdamKpIndexSupplier(MetricRegistry metrics) {
		this(metrics, parseUrl(URL));
	}

	PotsdamKpIndexSupplier(MetricRegistry metrics, URL url) {
		super(metrics, url);
	}

	@Override
	protected float parseKpIndex(final String urlContent) {
		String text = findLastKpIndex(urlContent);
		float whole = parseWhole(text);
		float extra = parseExtra(text);
		return whole + extra;
	}

	private static String findLastKpIndex(String urlContent) {
		return Arrays.stream(urlContent.split("\\n"))
				.flatMap(it -> Arrays.stream(it.split("\\s+"))
						.skip(1)
						.limit(8)
				)
				.map(String::trim)
				.filter(it -> !it.isEmpty())
				.filter(it -> it.matches("[0-9]([o+\\-])"))
				.reduce((first, second) -> second)
				.orElseThrow(() -> {
					String message = "Could not find Kp index in string: '" + urlContent + "'";
					return new IllegalArgumentException(message);
				});
	}

	private static float parseWhole(String text) {
		String firstChar = text.substring(0, 1);
		return Float.valueOf(firstChar);
	}

	private static float parseExtra(String text) {
		String ending = text.substring(1);
		switch (ending) {
			case "-":
				return -0.33f;
			case "+":
				return 0.33f;
			case "o":
				return 0f;
			default:
				throw new IllegalArgumentException("Invalid Kp index format: text");
		}
	}
}
