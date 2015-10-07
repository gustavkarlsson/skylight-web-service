package se.gustavkarlsson.aurora_notifier.web_service.resources;

import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import se.gustavkarlsson.aurora_notifier.common.domain.Timestamped;

import javax.ws.rs.core.GenericType;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class KpIndexResourceTest {

	private static final GenericType<Timestamped<Float>> KP_INDEX_TYPE = new GenericType<Timestamped<Float>>() {};
	private static final Supplier<Timestamped<Float>> supplier = mock(Supplier.class);
	private static final Timestamped<Float> kpIndex = new Timestamped<>(2.0f, 1000l);

	@ClassRule
	public static final ResourceTestRule resources = ResourceTestRule.builder()
			.addResource(new KpIndexResource(supplier)).build();

	@Before
	public void setup() {
		when(supplier.get()).thenReturn(kpIndex);
	}

	@Test
	public void get() {
		assertThat(resources.client().target("/kp-index").request()
				.get(KP_INDEX_TYPE).getValue()).isEqualTo(kpIndex.getValue());
		verify(supplier).get();
	}
}
