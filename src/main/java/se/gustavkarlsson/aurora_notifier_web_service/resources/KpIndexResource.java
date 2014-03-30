package se.gustavkarlsson.aurora_notifier_web_service.resources;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.Timed;
import se.gustavkarlsson.aurora_notifier_web_service.domain.KpIndexHolder;
import se.gustavkarlsson.aurora_notifier_web_service.services.fetcher.FetchException;
import se.gustavkarlsson.aurora_notifier_web_service.services.fetcher.kp_index.CachingKpIndexFetcher;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/kp-index")
@Produces(MediaType.APPLICATION_JSON)
public class KpIndexResource {

	private final CachingKpIndexFetcher fetcher;
	private final Meter errorsMeter;

	public KpIndexResource(CachingKpIndexFetcher fetcher, MetricRegistry metrics) {
		this.fetcher = fetcher;
		errorsMeter = createErrorsMeter(metrics);
	}

	@GET
	@Timed
	public KpIndexHolder getKpIndex() throws WebApplicationException {
		try {
			KpIndexHolder kpIndexHolder = fetcher.fetch();
			return kpIndexHolder;
		} catch (FetchException e) {
			// TODO Mark meter
			throw new PlainTextWebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}

	private Meter createErrorsMeter(MetricRegistry metrics) {
		return metrics.meter(MetricRegistry.name(getClass(), "errors"));
	}
}
