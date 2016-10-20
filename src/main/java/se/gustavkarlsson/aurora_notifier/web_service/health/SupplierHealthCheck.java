package se.gustavkarlsson.aurora_notifier.web_service.health;

import ru.vyarus.dropwizard.guice.module.installer.feature.health.NamedHealthCheck;

import java.util.function.Supplier;

abstract class SupplierHealthCheck extends NamedHealthCheck {

	protected final Supplier<?> supplier;

	SupplierHealthCheck(Supplier<?> supplier) {
		this.supplier = supplier;
	}

	@Override
	protected Result check() throws Exception {
		supplier.get();
		return Result.healthy();
	}
}
