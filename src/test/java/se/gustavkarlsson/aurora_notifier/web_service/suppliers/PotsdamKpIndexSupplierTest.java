package se.gustavkarlsson.aurora_notifier.web_service.suppliers;

import com.codahale.metrics.MetricRegistry;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URL;

import static com.google.common.io.Resources.getResource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class PotsdamKpIndexSupplierTest {
	private static URL DUMMY_URL;

	@BeforeClass
	public static void setUp() throws Exception {
		DUMMY_URL = new URL("http://somewhere.com/somefile.txt");
	}

	@Test(expected = NullPointerException.class)
	public void nullMetricsRegistry_constructor_throwsNpe() throws Exception {
		new PotsdamKpIndexSupplier(null, DUMMY_URL);
	}

	@Test(expected = NullPointerException.class)
	public void nullUrl_constructor_throwsNpe() throws Exception {
		new PotsdamKpIndexSupplier(new MetricRegistry(), null);
	}

	@Test
	public void validArgument_constructor_succeeds() throws Exception {
		new PotsdamKpIndexSupplier(new MetricRegistry());
	}

	@Test
	public void correctlyFormattedPage_get_returnsValue() throws Exception {
		PotsdamKpIndexSupplier supplier = new PotsdamKpIndexSupplier(new MetricRegistry(), getResource("potsdam_report.txt"));

		Float value = supplier.get();

		assertThat(value).isCloseTo(1.66f, within(0.01f));
	}

	@Test
	public void correctlyFormattedPage2_get_returnsValue() throws Exception {
		PotsdamKpIndexSupplier supplier = new PotsdamKpIndexSupplier(new MetricRegistry(), getResource("potsdam_report2.txt"));

		Float value = supplier.get();

		assertThat(value).isCloseTo(1f, within(0.01f));
	}

	@Test
	public void corruptPage_get_returnsNull() throws Exception {
		PotsdamKpIndexSupplier supplier = new PotsdamKpIndexSupplier(new MetricRegistry(), getResource("corrupt_potsdam_report.txt"));

		Float value = supplier.get();

		assertThat(value).isNull();
	}

	@Test
	public void missingPage_get_returnsNull() throws Exception {
		PotsdamKpIndexSupplier supplier = new PotsdamKpIndexSupplier(new MetricRegistry(), DUMMY_URL);

		Float value = supplier.get();

		assertThat(value).isNull();
	}
}
