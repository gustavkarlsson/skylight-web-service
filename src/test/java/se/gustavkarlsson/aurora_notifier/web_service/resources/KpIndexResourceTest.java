package se.gustavkarlsson.aurora_notifier.web_service.resources;

import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.core.GenericType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class KpIndexResourceTest {

	private static final GenericType<Timestamped<Float>> KP_INDEX_TYPE = new GenericType<Timestamped<Float>>() {};
	private static final KpIndexRepository mockRepository = mock(KpIndexRepository.class);
	private static final Timestamped<Float> kpIndex = new Timestamped<>(2.0f, 1000l);

	@ClassRule
	public static final ResourceTestRule resources = ResourceTestRule.builder()
			.addResource(new KpIndexResource(mockRepository)).build();

	@Before
	public void setup() {
		when(mockRepository.getLastKpIndex()).thenReturn(kpIndex);
	}

	@Test
	public void get() {
		assertThat(resources.client().target("/kp-index").request()
				.get(KP_INDEX_TYPE).getValue()).isEqualTo(kpIndex.getValue());
	}
}
