package se.gustavkarlsson.aurora_notifier_web_service.resources;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.Timed;
import se.gustavkarlsson.aurora_notifier_web_service.providers.CacheException;
import se.gustavkarlsson.aurora_notifier_web_service.providers.CachedProvider;
import se.gustavkarlsson.aurora_notifier_web_service.providers.ProviderException;
import se.gustavkarlsson.aurora_notifier.common.domain.KpIndexReport;
import se.gustavkarlsson.aurora_notifier.common.service.KpIndexService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/kp-index")
@Produces(MediaType.APPLICATION_JSON)
public class KpIndexResource implements KpIndexService {

	private final CachedProvider<KpIndexReport> provider;
	private final Meter errorsMeter;

	public KpIndexResource(CachedProvider<KpIndexReport> provider, MetricRegistry metrics) {
		this.provider = provider;
		errorsMeter = createErrorsMeter(metrics);
	}

	@Override
	@GET
	@Timed
	public KpIndexReport getKpIndex() throws WebApplicationException {
		KpIndexReport kpIndexReport;
		try {
			kpIndexReport = provider.getValue();
		} catch (ProviderException pe) {
			try {
				kpIndexReport = provider.getLastValue();
			} catch (CacheException ce) {
				ce.addSuppressed(pe);
				errorsMeter.mark();
				throw new PlainTextWebApplicationException(ce, Response.Status.INTERNAL_SERVER_ERROR);
			}
		}
		return kpIndexReport;
	}

	private Meter createErrorsMeter(MetricRegistry metrics) {
		return metrics.meter(MetricRegistry.name(getClass(), "errors"));
	}
}
