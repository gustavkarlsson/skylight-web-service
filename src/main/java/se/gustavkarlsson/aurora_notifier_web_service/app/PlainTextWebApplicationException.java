package se.gustavkarlsson.aurora_notifier_web_service.app;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class PlainTextWebApplicationException extends WebApplicationException {

	public PlainTextWebApplicationException(String message, Response.Status status) {
		super(Response.status(status).entity(message).type(MediaType.TEXT_PLAIN).build());
	}

	public PlainTextWebApplicationException(Throwable cause, Response.Status status) {
		this(getStackTraceAsAString(cause), status);
	}

	public PlainTextWebApplicationException(String message, Throwable cause, Response.Status status) {
		this(message + '\n' + getStackTraceAsAString(cause), status);
	}

	private static String getStackTraceAsAString(Throwable throwable) {
		try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
			throwable.printStackTrace(pw);
			String stackTrace = sw.toString();
			return stackTrace;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return throwable.getMessage();
	}
}
