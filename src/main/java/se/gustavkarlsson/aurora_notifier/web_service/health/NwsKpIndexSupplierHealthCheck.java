package se.gustavkarlsson.aurora_notifier.web_service.health;

import com.google.inject.Inject;
import se.gustavkarlsson.aurora_notifier.web_service.suppliers.kp_index.NwsKpIndexSupplier;

public class NwsKpIndexSupplierHealthCheck extends SupplierHealthCheck {

	@Inject
	public NwsKpIndexSupplierHealthCheck(NwsKpIndexSupplier supplier) {
		super(supplier);
	}

	@Override
	public String getName() {
		return "nwsKpIndex";
	}
}
