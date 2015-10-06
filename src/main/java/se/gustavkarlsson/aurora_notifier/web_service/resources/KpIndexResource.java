package se.gustavkarlsson.aurora_notifier.web_service.resources;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;
import com.google.inject.Inject;
import se.gustavkarlsson.aurora_notifier.common.domain.Timestamped;
import se.gustavkarlsson.aurora_notifier.common.service.KpIndexService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;

@Path("")
@Produces(MediaType.APPLICATION_JSON)
public class KpIndexResource implements KpIndexService {

	private final Supplier<Timestamped<Float>> supplier;

	@Inject
	public KpIndexResource(Supplier<Timestamped<Float>> supplier) {
		this.supplier = checkNotNull(supplier);
	}

	@Override
	@GET
	@Path("/kp-index")
	@Timed
	@ExceptionMetered
	public Timestamped<Float> get() {
		synchronized (this) {
			return supplier.get();
		}
	}
}
