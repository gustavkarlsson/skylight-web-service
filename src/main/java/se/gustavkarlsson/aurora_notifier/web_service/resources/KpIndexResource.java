package se.gustavkarlsson.aurora_notifier.web_service.resources;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;
import se.gustavkarlsson.aurora_notifier.common.domain.Timestamped;
import se.gustavkarlsson.aurora_notifier.web_service.repository.KpIndexRepository;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import static com.google.common.base.Preconditions.checkNotNull;

@Singleton
@Path("/kp-index")
@Produces(MediaType.APPLICATION_JSON)
public class KpIndexResource {
	private final KpIndexRepository repository;

	@Inject
	KpIndexResource(KpIndexRepository repository) {
		this.repository = checkNotNull(repository);
	}

	@GET
	@Timed
	@ExceptionMetered
	public Timestamped<Float> get() {
		return repository.getLastKpIndex();
	}
}
