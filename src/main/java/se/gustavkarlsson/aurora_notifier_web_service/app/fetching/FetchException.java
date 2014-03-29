package se.gustavkarlsson.aurora_notifier_web_service.app.fetching;

import se.gustavkarlsson.aurora_notifier_web_service.app.PlainTextWebApplicationException;

import javax.ws.rs.core.Response;

public class FetchException extends PlainTextWebApplicationException {
	private static final Response.Status STATUS = Response.Status.INTERNAL_SERVER_ERROR;
	public FetchException(String message) {
		super(message, STATUS);
	}

	public FetchException(Throwable cause) {
		super(cause, STATUS);
	}

	public FetchException(String message, Throwable cause) {
		super(message, cause, STATUS);
	}
}
