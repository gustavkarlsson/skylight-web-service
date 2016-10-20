package se.gustavkarlsson.aurora_notifier.web_service.suppliers.kp_index;

import com.codahale.metrics.MetricRegistry;
import org.junit.BeforeClass;
import org.junit.Test;
import se.gustavkarlsson.aurora_notifier.web_service.suppliers.SupplierException;

import java.net.URL;

import static com.google.common.io.Resources.getResource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class NwsKpIndexSupplierTest {
	private static URL DUMMY_URL;

	@BeforeClass
	public static void setUp() throws Exception {
		DUMMY_URL = new URL("http://somewhere.com/somefile.txt");
	}

	@Test(expected = NullPointerException.class)
	public void nullMetricsRegistry_constructor_throwsNpe() throws Exception {
		new NwsKpIndexSupplier(null, DUMMY_URL);
	}

	@Test(expected = NullPointerException.class)
	public void nullUrl_constructor_throwsNpe() throws Exception {
		new NwsKpIndexSupplier(new MetricRegistry(), null);
	}

	@Test
	public void validArgument_constructor_succeeds() throws Exception {
		new NwsKpIndexSupplier(new MetricRegistry());
	}

	@Test
	public void correctlyFormattedPage_get_returnsLastValue() throws Exception {
		NwsKpIndexSupplier supplier = new NwsKpIndexSupplier(new MetricRegistry(), getResource("nws_report.txt"));

		Float value = supplier.get();

		assertThat(value).isCloseTo(0.35f, within(0.005f));
	}

	@Test(expected = SupplierException.class)
	public void corruptPage_get_throwsSupplierException() throws Exception {
		NwsKpIndexSupplier supplier = new NwsKpIndexSupplier(new MetricRegistry(), getResource("corrupt_nws_report.txt"));

		supplier.get();
	}

	@Test(expected = SupplierException.class)
	public void missingPage_get_throwsSupplierException() throws Exception {
		NwsKpIndexSupplier supplier = new NwsKpIndexSupplier(new MetricRegistry(), new URL("file:///does/not/exist/for/sure.txt"));

		supplier.get();
	}
}
