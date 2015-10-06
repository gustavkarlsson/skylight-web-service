package se.gustavkarlsson.aurora_notifier.web_service.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import se.gustavkarlsson.aurora_notifier.common.domain.Timestamped;

import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;

public class KpIndexTest {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	public static final Timestamped<Float> KP_INDEX = new Timestamped<>(1.0f, 1444033913849l);

	@Test
	public void serializesToJson() throws Exception {
		final String expected = MAPPER.writeValueAsString(
				MAPPER.readValue(fixture("fixtures/kp-index.json"), Timestamped.class));
		assertThat(MAPPER.writeValueAsString(KP_INDEX)).isEqualTo(expected);
	}
}
