package se.gustavkarlsson.aurora_notifier.web_service.suppliers.kp_index;

import com.codahale.metrics.MetricRegistry;
import org.junit.BeforeClass;
import org.junit.Test;
import se.gustavkarlsson.aurora_notifier.web_service.suppliers.SupplierException;

import java.net.URL;

import static com.google.common.io.Resources.getResource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class SwlKpIndexSupplierTest {
	private static URL DUMMY_URL;

	@BeforeClass
	public static void setUp() throws Exception {
		DUMMY_URL = new URL("http://somewhere.com/somefile.txt");
	}

	@Test(expected = NullPointerException.class)
	public void nullMetricsRegistry_constructor_throwsNpe() throws Exception {
		new SwlKpIndexSupplier(null, DUMMY_URL);
	}

	@Test(expected = NullPointerException.class)
	public void nullUrl_constructor_throwsNpe() throws Exception {
		new SwlKpIndexSupplier(new MetricRegistry(), null);
	}

	@Test
	public void validArgument_constructor_succeeds() throws Exception {
		new SwlKpIndexSupplier(new MetricRegistry());
	}

	@Test
	public void correctlyFormattedPage_get_returnsValue() throws Exception {
		SwlKpIndexSupplier supplier = new SwlKpIndexSupplier(new MetricRegistry(), getResource("swl_report.html"));

		Float value = supplier.get();

		assertThat(value).isCloseTo(4.33f, within(0.005f));
	}

	@Test(expected = SupplierException.class)
	public void corruptPage_get_throwsSupplierException() throws Exception {
		SwlKpIndexSupplier supplier = new SwlKpIndexSupplier(new MetricRegistry(), getResource("corrupt_swl_report.html"));

		supplier.get();
	}

	@Test(expected = SupplierException.class)
	public void missingPage_get_throwsSupplierException() throws Exception {
		SwlKpIndexSupplier supplier = new SwlKpIndexSupplier(new MetricRegistry(), DUMMY_URL);

		supplier.get();
	}
}
