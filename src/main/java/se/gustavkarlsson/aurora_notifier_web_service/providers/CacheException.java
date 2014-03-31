package se.gustavkarlsson.aurora_notifier_web_service.providers;

public class CacheException extends Exception {

	public CacheException(String message) {
		super(message);
	}

	public CacheException(Throwable cause) {
		super(cause);
	}

	public CacheException(String message, Throwable cause) {
		super(message, cause);
	}
}
