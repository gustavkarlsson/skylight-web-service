package se.gustavkarlsson.aurora_notifier.web_service.health;

import ru.vyarus.dropwizard.guice.module.installer.feature.health.NamedHealthCheck;

import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;

abstract class SupplierHealthCheck extends NamedHealthCheck {

	private final Supplier<?> supplier;

	SupplierHealthCheck(Supplier<?> supplier) {
		this.supplier = checkNotNull(supplier);
	}

	@Override
	protected Result check() throws Exception {
		supplier.get();
		return Result.healthy();
	}
}
