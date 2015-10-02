package se.gustavkarlsson.aurora_notifier.web_service.resources;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.Timed;
import se.gustavkarlsson.aurora_notifier.common.domain.KpIndexReport;
import se.gustavkarlsson.aurora_notifier.common.service.KpIndexService;
import se.gustavkarlsson.aurora_notifier.web_service.providers.Provider;
import se.gustavkarlsson.aurora_notifier.web_service.providers.ProviderException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import static com.google.common.base.Preconditions.checkNotNull;

@Path("/kp-index")
@Produces(MediaType.APPLICATION_JSON)
public class KpIndexResource implements KpIndexService {

	private final Provider<KpIndexReport> provider;
	private final Meter errorsMeter;

	public KpIndexResource(Provider<KpIndexReport> provider, MetricRegistry metrics) {
		this.provider = checkNotNull(provider);
		this.errorsMeter = createErrorsMeter(checkNotNull(metrics));
	}

	@Override
	@GET
	@Timed
	public KpIndexReport getKpIndex() throws WebApplicationException {
		try {
			return provider.getValue();
		} catch (ProviderException pe) {
			errorsMeter.mark();
			throw new WebApplicationException();
		}
	}

	private Meter createErrorsMeter(MetricRegistry metrics) {
		return metrics.meter(MetricRegistry.name(getClass(), "errors"));
	}
}
