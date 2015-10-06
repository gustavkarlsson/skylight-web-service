package se.gustavkarlsson.aurora_notifier.web_service.providers;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RaceProviderTest {

	private DummyProvider mockSlowProvider() {
		DummyProvider fastProvider = mock(DummyProvider.class);
		when(fastProvider.getValue()).then(i -> {
			Thread.sleep(5_000);
			return "slow";
		});
		return fastProvider;
	}

	private DummyProvider mockFastProvider() {
		DummyProvider fastProvider = mock(DummyProvider.class);
		when(fastProvider.getValue()).thenReturn("fast");
		return fastProvider;
	}

	@Test(expected = IllegalArgumentException.class)
	public void noProvidersThrowsIae() {
		new RaceProvider<Void>(Collections.emptySet());
	}

	@Test(expected = NullPointerException.class)
	public void nullProvidersThrowsNpe() {
		new RaceProvider<Void>(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void nullProviderThrowsIae() {
		new RaceProvider<>(new HashSet<>(Arrays.asList(new DummyProvider(), null)));
	}

	@Test
	public void picksFastestImplementation() {
		Set<Provider<Object>> providers = new HashSet<>();
		providers.add(mockFastProvider());
		for (int i = 0; i < 100; i++) {
			providers.add(mockSlowProvider());
		}
		RaceProvider<Object> raceProvider = new RaceProvider<>(providers);
		Object winner = raceProvider.getValue();
		assertThat(winner).isEqualTo("fast");
	}

}
