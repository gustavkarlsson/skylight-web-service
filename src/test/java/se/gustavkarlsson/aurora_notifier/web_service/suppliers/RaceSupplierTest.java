package se.gustavkarlsson.aurora_notifier.web_service.suppliers;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RaceSupplierTest {

	private Supplier<String> mockSlowSupplier() {
		Supplier<String> supplier = mock(Supplier.class);
		when(supplier.get()).then(i -> {
			Thread.sleep(5_000);
			return "slow";
		});
		return supplier;
	}

	private Supplier<String> mockFastSupplier() {
		Supplier<String> supplier = mock(Supplier.class);
		when(supplier.get()).thenReturn("fast");
		return supplier;
	}

	@Test(expected = IllegalArgumentException.class)
	public void emptySuppliersThrowsIae() {
		new RaceSupplier<Void>(Collections.emptySet());
	}

	@Test(expected = NullPointerException.class)
	public void nullSuppliersThrowsNpe() {
		new RaceSupplier<Void>(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void nullSupplierThrowsIae() {
		new RaceSupplier<>(new HashSet<>(Arrays.asList(mockFastSupplier(), null)));
	}

	@Test
	public void picksFastestImplementation() {
		Set<Supplier<String>> suppliers = new HashSet<>();
		suppliers.add(mockFastSupplier());
		for (int i = 0; i < 100; i++) {
			suppliers.add(mockSlowSupplier());
		}
		RaceSupplier<String> raceSupplier = new RaceSupplier<>(suppliers);
		Object winner = raceSupplier.get();
		assertThat(winner).isEqualTo("fast");
	}

}
