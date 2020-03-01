package se.gustavkarlsson.aurora_notifier.web_service;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.ClassRule;
import org.junit.Test;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import ru.vyarus.dropwizard.guice.GuiceBundle;
import se.gustavkarlsson.aurora_notifier.web_service.guice_annotations.Update;
import se.gustavkarlsson.aurora_notifier.web_service.resources.Timestamped;

import javax.inject.Singleton;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static org.assertj.core.api.Assertions.assertThat;

public class AuroraApplicationIntegrationTest {

	@ClassRule
	public static final DropwizardAppRule<AuroraConfiguration> RULE =
			new DropwizardAppRule<>(AuroraTestApplication.class, resourceFilePath("test.yml"));

	private static final Timestamped<Float> KP_INDEX = new Timestamped<>(2.0f, 1000);

	@Test
	public void getKpIndexSucceeds() throws Exception {
		try (CloseableHttpClient client = HttpClients.createDefault()) {
			HttpGet httpGet = new HttpGet(createBaseUri() + "/kp-index");
			try (CloseableHttpResponse response = client.execute(httpGet)) {
				assertThat(response.getStatusLine().getStatusCode()).isEqualTo(200);
				HttpEntity entity = response.getEntity();
				String content = EntityUtils.toString(entity);
				assertThat(content).isEqualTo("{\"value\":2.0,\"timestamp\":1000}");
			}
		}
	}

	@Test
	public void getKpIndexUsingRetrofitSucceeds() throws Exception {
		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(createBaseUri())
				.addConverterFactory(JacksonConverterFactory.create())
				.build();
		KpIndexService service = retrofit.create(KpIndexService.class);
		Response<Timestamped<Float>> response = service.get().execute();
		assertThat(response.isSuccessful()).isTrue();
		assertThat(response.body()).isEqualTo(KP_INDEX);
	}

	private static String createBaseUri() {
		return "http://localhost:" + RULE.getLocalPort() + "/";
	}

	public static class AuroraTestApplication extends AuroraApplication {
		@Override
		public void initialize(Bootstrap<AuroraConfiguration> bootstrap) {
			GuiceBundle<AuroraConfiguration> guiceBundle = GuiceBundle.<AuroraConfiguration>builder()
					.enableAutoConfig("se.gustavkarlsson.aurora_notifier.web_service")
					.modules(new AuroraTestModule())
					.build();
			bootstrap.addBundle(guiceBundle);
		}

		private static class AuroraTestModule extends AbstractModule {
			@Override
			protected void configure() {
				Multibinder<Supplier<Float>> kpIndexSuppliersBinder = Multibinder.newSetBinder(binder(), new TypeLiteral<Supplier<Float>>() {});
				kpIndexSuppliersBinder.addBinding().to(FixedTimestampedSupplier.class);
			}

			@Provides
			@Singleton
			@Update
			public Duration provideUpdateDelay() {
				return Duration.ofSeconds(5);
			}

			@Provides
			@Singleton
			public ScheduledExecutorService provideScheduledExecutorService() {
				return Executors.newSingleThreadScheduledExecutor();
			}

			@Provides
			@Singleton
			public Clock provideClock() {
				return Clock.fixed(Instant.ofEpochMilli(1000), ZoneOffset.UTC);
			}

			private static class FixedTimestampedSupplier implements Supplier<Float> {
				@Override
				public Float get() {
					return KP_INDEX.getValue();
				}
			}
		}
	}

}
