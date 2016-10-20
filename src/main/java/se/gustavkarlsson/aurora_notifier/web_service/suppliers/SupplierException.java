package se.gustavkarlsson.aurora_notifier.web_service.suppliers;

public class SupplierException extends RuntimeException {

	SupplierException(String message) {
		super(message);
	}

	public SupplierException(Throwable cause) {
		super(cause);
	}

	SupplierException(String message, Throwable cause) {
		super(message, cause);
	}
}
