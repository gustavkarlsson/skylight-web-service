package se.gustavkarlsson.aurora_notifier.web_service.suppliers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Duration;
import java.util.function.Supplier;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CachingSupplierTest {

	@Mock
	private Supplier<String> mocked;

	@Test(expected = NullPointerException.class)
	public void nullSupplier_constructor_throwsNpe() {
		new CachingSupplier<Void>(null, Duration.ofMinutes(30));
	}

	@Test(expected = NullPointerException.class)
	public void nullDuration_constructor_throwsNpe() {
		new CachingSupplier<>(mocked, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void negativeDuration_constructor_throwsIae() {
		new CachingSupplier<>(mocked, Duration.ofMillis(-1));
	}

	@Test
	public void firstUse_get_cachedValueIsUsed() {
		CachingSupplier<?> supplier = new CachingSupplier<>(mocked, Duration.ofDays(1));

		supplier.get();

		verify(mocked, times(1)).get();
	}

	@Test
	public void usedOnceBeforeAndNotExpired_get_cachedValueIsUsed() {
		CachingSupplier<?> supplier = new CachingSupplier<>(mocked, Duration.ofDays(1));
		supplier.get();

		supplier.get();

		verify(mocked, times(1)).get();
	}

	@Test
	public void usedBeforeButExpired_get_cachedValueIsNotUsed() {
		CachingSupplier<?> supplier = new CachingSupplier<>(mocked, Duration.ofMillis(0));
		supplier.get();

		supplier.get();

		verify(mocked, times(2)).get();
	}

	@Test(expected = RuntimeException.class)
	public void innerSupplierThrowsException_get_throwsRuntimeException() {
		CachingSupplier<?> supplier = new CachingSupplier<>(mocked, Duration.ofMillis(0));
		when(mocked.get()).thenThrow(new RuntimeException("Boo!"));

		supplier.get();
	}
}
