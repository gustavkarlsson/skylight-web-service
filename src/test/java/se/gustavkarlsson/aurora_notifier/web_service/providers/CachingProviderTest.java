package se.gustavkarlsson.aurora_notifier.web_service.providers;

import org.joda.time.Duration;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class CachingProviderTest {

	private DummyProvider mocked;

	@Before
	public void setup() {
		mocked = mock(DummyProvider.class);
	}

	@Test(expected = NullPointerException.class)
	public void nullProviderThrowsNpe() {
		new CachingProvider<Void>(null, Duration.standardMinutes(30));
	}

	@Test(expected = NullPointerException.class)
	public void nullDurationThrowsNpe() {
		new CachingProvider<>(new DummyProvider(), null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void negativeDurationThrowsIae() {
		new CachingProvider<>(new DummyProvider(), Duration.millis(-1));
	}

	@Test
	public void cachedValueIsUsed() {
		CachingProvider<?> provider = new CachingProvider<>(mocked, Duration.standardDays(1));
		provider.getValue();
		verify(mocked, times(1)).getValue();
		provider.getValue();
		verify(mocked, times(1)).getValue();
	}

	@Test
	public void cachedValueIsNotUsedIfExpired() {
		CachingProvider<?> provider = new CachingProvider<>(mocked, Duration.millis(0));
		provider.getValue();
		verify(mocked, times(1)).getValue();
		provider.getValue();
		verify(mocked, times(2)).getValue();
	}

	@Test
	public void fallsBackOnCachedValueIfProviderException() {
		CachingProvider<?> provider = new CachingProvider<>(mocked, Duration.millis(0));
		when(mocked.getValue()).thenReturn("a");
		provider.getValue();
		when(mocked.getValue()).thenThrow(new ProviderException("I'm not a!"));
		assertThat(provider.getValue().getValue()).isEqualTo("a");
	}

	@Test(expected = ProviderException.class)
	public void exceptionAndNoFallbackThrowsProviderException() {
		CachingProvider<?> provider = new CachingProvider<>(mocked, Duration.millis(0));
		when(mocked.getValue()).thenThrow(new ProviderException("Boo!"));
		provider.getValue();
	}
}
