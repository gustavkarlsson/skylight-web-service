package se.gustavkarlsson.aurora_notifier_web_service.services.fetcher;

public interface Fetcher<T> {

	T fetch() throws FetchException;

}
