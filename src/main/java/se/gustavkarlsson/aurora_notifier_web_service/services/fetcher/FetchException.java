package se.gustavkarlsson.aurora_notifier_web_service.services.fetcher;

public class FetchException extends Exception {

	public FetchException(String message) {
		super(message);
	}

	public FetchException(Throwable cause) {
		super(cause);
	}

	public FetchException(String message, Throwable cause) {
		super(message, cause);
	}
}
