package se.gustavkarlsson.aurora_notifier.web_service.providers;

import se.gustavkarlsson.aurora_notifier.common.domain.KpIndexReport;

import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.*;

public class AggregateKpIndexProvider extends AggregateProvider<KpIndexReport> {

	public AggregateKpIndexProvider(Collection<Provider<KpIndexReport>> providers) {
		super(checkNotNull(providers));
	}

	@Override
	public KpIndexReport getValue() throws ProviderException {
		List<KpIndexReport> values = getValues();
		if (values.isEmpty()) {
			throw new ProviderException("No values to provide result");
		}

		long latestTimestamp = 0;
		float kpIndexSum = 0;
		for (KpIndexReport report : values) {
			latestTimestamp = Math.max(latestTimestamp, report.getTimestamp());
			kpIndexSum += report.getKpIndex();
		}
		float kpIndexAverage = kpIndexSum / ((float) values.size());
		return new KpIndexReport(kpIndexAverage, latestTimestamp);
	}
}
