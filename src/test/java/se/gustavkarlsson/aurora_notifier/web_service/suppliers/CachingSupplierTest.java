package se.gustavkarlsson.aurora_notifier.web_service.suppliers;

import org.joda.time.Duration;
import org.junit.Before;
import org.junit.Test;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class CachingSupplierTest {

	private Supplier<Object> mocked;

	@Before
	public void setup() {
		mocked = mock(Supplier.class);
	}

	@Test(expected = NullPointerException.class)
	public void nullSupplierThrowsNpe() {
		new CachingSupplier<Void>(null, Duration.standardMinutes(30));
	}

	@Test(expected = NullPointerException.class)
	public void nullDurationThrowsNpe() {
		new CachingSupplier<>(mocked, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void negativeDurationThrowsIae() {
		new CachingSupplier<>(mocked, Duration.millis(-1));
	}

	@Test
	public void cachedValueIsUsed() {
		CachingSupplier<?> supplier = new CachingSupplier<>(mocked, Duration.standardDays(1));
		supplier.get();
		verify(mocked, times(1)).get();
		supplier.get();
		verify(mocked, times(1)).get();
	}

	@Test
	public void cachedValueIsNotUsedIfExpired() {
		CachingSupplier<?> supplier = new CachingSupplier<>(mocked, Duration.millis(0));
		supplier.get();
		verify(mocked, times(1)).get();
		supplier.get();
		verify(mocked, times(2)).get();
	}

	@Test
	public void fallsBackOnCachedValueIfSupplierException() {
		CachingSupplier<?> supplier = new CachingSupplier<>(mocked, Duration.millis(0));
		when(mocked.get()).thenReturn("a");
		supplier.get();
		when(mocked.get()).thenThrow(new SupplierException("I'm not a!"));
		assertThat(supplier.get().getValue()).isEqualTo("a");
	}

	@Test(expected = SupplierException.class)
	public void exceptionAndNoFallbackThrowsSupplierException() {
		CachingSupplier<?> supplier = new CachingSupplier<>(mocked, Duration.millis(0));
		when(mocked.get()).thenThrow(new SupplierException("Boo!"));
		supplier.get();
	}
}
