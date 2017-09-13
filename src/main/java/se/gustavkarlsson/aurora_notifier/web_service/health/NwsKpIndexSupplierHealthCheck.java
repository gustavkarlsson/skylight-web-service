package se.gustavkarlsson.aurora_notifier.web_service.health;

import se.gustavkarlsson.aurora_notifier.web_service.suppliers.NwsKpIndexSupplier;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
class NwsKpIndexSupplierHealthCheck extends SupplierHealthCheck {

	@Inject
	NwsKpIndexSupplierHealthCheck(NwsKpIndexSupplier supplier) {
		super(supplier);
	}

	@Override
	public String getName() {
		return "nwsKpIndex";
	}
}
