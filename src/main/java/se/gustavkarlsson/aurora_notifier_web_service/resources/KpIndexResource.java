package se.gustavkarlsson.aurora_notifier_web_service.resources;

import com.codahale.metrics.annotation.Timed;
import se.gustavkarlsson.aurora_notifier_web_service.app.fetching.FetchException;
import se.gustavkarlsson.aurora_notifier_web_service.app.fetching.KpIndexFetcher;
import se.gustavkarlsson.aurora_notifier_web_service.domain.KpIndexHolder;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/kp-index")
@Produces(MediaType.APPLICATION_JSON)
public class KpIndexResource {

	private final KpIndexFetcher fetcher;

	public KpIndexResource(KpIndexFetcher fetcher) {
		this.fetcher = fetcher;
	}

	@GET
	@Timed
	public KpIndexHolder getKpIndex() throws FetchException {
		KpIndexHolder kpIndexHolder = fetcher.fetch();
		return kpIndexHolder;
	}
}
