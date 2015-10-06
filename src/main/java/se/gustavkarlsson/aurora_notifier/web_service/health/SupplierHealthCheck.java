package se.gustavkarlsson.aurora_notifier.web_service.health;

import com.hubspot.dropwizard.guice.InjectableHealthCheck;

import java.util.function.Supplier;

public abstract class SupplierHealthCheck extends InjectableHealthCheck {

	protected final Supplier<?> supplier;

	public SupplierHealthCheck(Supplier<?> supplier) {
		this.supplier = supplier;
	}

	@Override
	protected Result check() throws Exception {
		supplier.get();
		return Result.healthy();
	}
}
