package se.gustavkarlsson.aurora_notifier_web_service.providers;

public class ProviderException extends Exception {

	public ProviderException(String message) {
		super(message);
	}

	public ProviderException(Throwable cause) {
		super(cause);
	}

	public ProviderException(String message, Throwable cause) {
		super(message, cause);
	}
}
