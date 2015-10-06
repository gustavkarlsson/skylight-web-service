package se.gustavkarlsson.aurora_notifier.web_service.suppliers;

public class SupplierException extends RuntimeException {

	public SupplierException(String message) {
		super(message);
	}

	public SupplierException(Throwable cause) {
		super(cause);
	}
}
