package se.gustavkarlsson.aurora_notifier.web_service.suppliers;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class RaceSupplierTest {

	@Test(expected = IllegalArgumentException.class)
	public void emptySuppliers_constructor_throwsIae() {
		new RaceSupplier<Void>(emptySet());
	}

	@Test(expected = NullPointerException.class)
	public void nullSuppliers_constructor_throwsNpe() {
		new RaceSupplier<Void>(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void oneNullSupplier_constructor_throwsIae() {
		new RaceSupplier<>(asList(fastSupplier(), null));
	}

	@Test
	public void manySlowSuppliersAndOneFast_get_fastestSupplierIsPicked() {
		Set<Supplier<String>> suppliers = new HashSet<>();
		for (int i = 0; i < 50; i++) {
			suppliers.add(slowSupplier());
		}
		suppliers.add(fastSupplier());
		for (int i = 0; i < 50; i++) {
			suppliers.add(slowSupplier());
		}
		RaceSupplier<String> raceSupplier = new RaceSupplier<>(suppliers);

		Object winner = raceSupplier.get();

		assertThat(winner).isEqualTo("fast");
	}

	@Test(expected = SupplierException.class)
	public void onlyOneSupplierThatThrowsException_get_throwsSupplierException() {
		RaceSupplier<String> raceSupplier = new RaceSupplier<>(singletonList(exceptionThrowingSupplier()));

		raceSupplier.get();
	}

	private static Supplier<String> slowSupplier() {
		return () -> {
			try {
				Thread.sleep(5_000);
			} catch (InterruptedException ignored) {
			}
			return "slow";
		};
	}

	private static Supplier<String> fastSupplier() {
		return () -> "fast";
	}

	private static Supplier<String> exceptionThrowingSupplier() {
		return () -> {
			throw new RuntimeException("Error!");
		};
	}

}
