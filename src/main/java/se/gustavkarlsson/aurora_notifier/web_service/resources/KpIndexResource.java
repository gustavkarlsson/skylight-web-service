package se.gustavkarlsson.aurora_notifier.web_service.resources;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;
import com.google.inject.Inject;
import se.gustavkarlsson.aurora_notifier.common.domain.Timestamped;
import se.gustavkarlsson.aurora_notifier.common.service.KpIndexService;
import se.gustavkarlsson.aurora_notifier.web_service.providers.Provider;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import static com.google.common.base.Preconditions.checkNotNull;

@Path("/kp-index")
@Produces(MediaType.APPLICATION_JSON)
public class KpIndexResource implements KpIndexService {

	private final Provider<Timestamped<Float>> provider;

	@Inject
	public KpIndexResource(Provider<Timestamped<Float>> provider) {
		this.provider = checkNotNull(provider);
	}

	@Override
	@GET
	@Timed
	@ExceptionMetered
	public Timestamped<Float> get() {
		synchronized (this) {
			return provider.getValue();
		}
	}
}
