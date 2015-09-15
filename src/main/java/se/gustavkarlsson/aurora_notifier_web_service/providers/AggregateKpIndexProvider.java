package se.gustavkarlsson.aurora_notifier_web_service.providers;

import se.gustavkarlsson.aurora_notifier.common.domain.KpIndexWsReport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AggregateKpIndexProvider extends AggregateProvider<KpIndexWsReport> {

	private final List<Provider<KpIndexWsReport>> providers = new ArrayList<>();

	public AggregateKpIndexProvider(Provider<KpIndexWsReport>... providers) {
		super(providers);
	}

	public AggregateKpIndexProvider(Collection<Provider<KpIndexWsReport>> providers) {
		super(providers);
	}

	public AggregateKpIndexProvider() {
		super();
	}

	@Override
	public KpIndexWsReport getValue() throws ProviderException {
		List<KpIndexWsReport> values = getValues();
		if (values.isEmpty()) {
			throw new ProviderException("No values to provide aggregate result");
		}

		long latestTimestamp = 0;
		float kpIndexSum = 0;
		for (KpIndexWsReport report : values) {
			latestTimestamp = Math.max(latestTimestamp, report.getTimestamp());
			kpIndexSum += report.getKpIndex();
		}
		float kpIndexAverage = kpIndexSum / ((float) values.size());
		return new KpIndexWsReport(kpIndexAverage, latestTimestamp);
	}
}
