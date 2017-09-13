package se.gustavkarlsson.aurora_notifier.web_service.security;

import org.slf4j.Logger;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import static org.slf4j.LoggerFactory.getLogger;

public class SslSecurityOverrider {
	private static final Logger logger = getLogger(SslSecurityOverrider.class);

	private SslSecurityOverrider() {
	}

	public static void override() {
		final TrustManager[] trustAllCertificates = new TrustManager[]{
				new X509TrustManager() {
					@Override
					public X509Certificate[] getAcceptedIssuers() {
						return null; // Not relevant.
					}

					@Override
					public void checkClientTrusted(X509Certificate[] certs, String authType) {
						// Do nothing. Just allow them all.
					}

					@Override
					public void checkServerTrusted(X509Certificate[] certs, String authType) {
						// Do nothing. Just allow them all.
					}
				}
		};

		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCertificates, new SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			logger.warn("SSL certificate checks have been disabled for HTTP clients");
		} catch (GeneralSecurityException e) {
			throw new ExceptionInInitializerError(e);
		}
	}
}
