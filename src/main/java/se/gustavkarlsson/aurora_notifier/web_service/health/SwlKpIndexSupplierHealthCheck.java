package se.gustavkarlsson.aurora_notifier.web_service.health;

import se.gustavkarlsson.aurora_notifier.web_service.suppliers.SwlKpIndexSupplier;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
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
