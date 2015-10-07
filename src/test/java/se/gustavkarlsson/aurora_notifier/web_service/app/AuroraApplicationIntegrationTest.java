package se.gustavkarlsson.aurora_notifier.web_service.app;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.squarespace.jersey2.guice.BootstrapUtils;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.AfterClass;
import org.junit.ClassRule;
import org.junit.Test;
import se.gustavkarlsson.aurora_notifier.common.domain.Timestamped;
import se.gustavkarlsson.aurora_notifier.web_service.config.AuroraConfiguration;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

public class AuroraApplicationIntegrationTest {

	@ClassRule
	public static final DropwizardAppRule<AuroraConfiguration> RULE =
			new DropwizardAppRule<>(AuroraTestApplication.class, null);

	@AfterClass
	public static void tearDown() {
		BootstrapUtils.reset();
	}

	@Test
	public void getKpIndexSucceeds() throws Exception {
		try (CloseableHttpClient client = HttpClients.createDefault()) {
			HttpGet httpGet = new HttpGet("http://localhost:" + RULE.getLocalPort() + "/kp-index");
			try (CloseableHttpResponse response = client.execute(httpGet)) {
				assertThat(response.getStatusLine().getStatusCode()).isEqualTo(200);
				HttpEntity entity = response.getEntity();
				String content = EntityUtils.toString(entity);
				assertThat(content).isEqualTo("{\"value\":2.0,\"timestamp\":1000}");
			}
		}
	}

	public static class AuroraTestApplication extends AuroraApplication {
		@Override
		protected Module getModule() {
			return new AuroraTestModule();
		}

		private static class AuroraTestModule extends AbstractModule {
			@Override
			protected void configure() {
				bind(new TypeLiteral<Supplier<Timestamped<Float>>>() {}).to(FixedTimestampedSupplier.class);
			}

			private static class FixedTimestampedSupplier implements Supplier<Timestamped<Float>> {
				@Override
				public Timestamped<Float> get() {
					return new Timestamped<>(2.0f, 1000);
				}
			}
		}
	}

}
