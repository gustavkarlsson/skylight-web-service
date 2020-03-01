package se.gustavkarlsson.aurora_notifier.web_service.health;

import se.gustavkarlsson.aurora_notifier.web_service.suppliers.PotsdamKpIndexSupplier;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
class PotsdamKpIndexSupplierHealthCheck extends SupplierHealthCheck {

	@Inject
	PotsdamKpIndexSupplierHealthCheck(PotsdamKpIndexSupplier supplier) {
		super(supplier);
	}

	@Override
	public String getName() {
		return "potsdamKpIndex";
	}
}
