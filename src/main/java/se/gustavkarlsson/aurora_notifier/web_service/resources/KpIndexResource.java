package se.gustavkarlsson.aurora_notifier.web_service.resources;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;
import com.google.inject.Inject;
import se.gustavkarlsson.aurora_notifier.common.domain.Timestamped;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;

@Path("/kp-index")
@Produces(MediaType.APPLICATION_JSON)
public class KpIndexResource {

	private final Supplier<Timestamped<Float>> supplier;

	@Inject
	KpIndexResource(Supplier<Timestamped<Float>> supplier) {
		this.supplier = checkNotNull(supplier);
	}

	@GET
	@Timed
	@ExceptionMetered
	public Timestamped<Float> get() {
		synchronized (this) {
			return supplier.get();
		}
	}
}
