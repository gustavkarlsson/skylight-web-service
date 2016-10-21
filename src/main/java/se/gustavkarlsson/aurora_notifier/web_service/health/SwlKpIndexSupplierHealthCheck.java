package se.gustavkarlsson.aurora_notifier.web_service.health;

import com.google.inject.Inject;
import se.gustavkarlsson.aurora_notifier.web_service.suppliers.kp_index.SwlKpIndexSupplier;

class SwlKpIndexSupplierHealthCheck extends SupplierHealthCheck {

	@Inject
	SwlKpIndexSupplierHealthCheck(SwlKpIndexSupplier supplier) {
		super(supplier);
	}

	@Override
	public String getName() {
		return "swlKpIndex";
	}
}
