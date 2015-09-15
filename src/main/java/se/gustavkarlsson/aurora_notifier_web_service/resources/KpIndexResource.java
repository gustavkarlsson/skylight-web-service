package se.gustavkarlsson.aurora_notifier_web_service.resources;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.Timed;
import se.gustavkarlsson.aurora_notifier.common.domain.KpIndexWsReport;
import se.gustavkarlsson.aurora_notifier.common.service.KpIndexService;
import se.gustavkarlsson.aurora_notifier_web_service.providers.Provider;
import se.gustavkarlsson.aurora_notifier_web_service.providers.ProviderException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

@Path("/kp-index")
@Produces(MediaType.APPLICATION_JSON)
public class KpIndexResource implements KpIndexService {

	private final Provider<KpIndexWsReport> provider;
	private final Meter errorsMeter;

	public KpIndexResource(Provider<KpIndexWsReport> provider, MetricRegistry metrics) {
		this.provider = provider;
		errorsMeter = createErrorsMeter(metrics);
	}

	@Override
	@GET
	@Timed
	public KpIndexWsReport getKpIndex() throws WebApplicationException {
		KpIndexWsReport kpIndexReport;
		try {
			kpIndexReport = provider.getValue();
		} catch (ProviderException pe) {
			errorsMeter.mark();
			// TODO log error
			throw new WebApplicationException();
		}
		return kpIndexReport;
	}

	private Meter createErrorsMeter(MetricRegistry metrics) {
		return metrics.meter(MetricRegistry.name(getClass(), "errors"));
	}
}
