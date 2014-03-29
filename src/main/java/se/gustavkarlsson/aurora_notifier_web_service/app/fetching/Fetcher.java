package se.gustavkarlsson.aurora_notifier_web_service.app.fetching;

public interface Fetcher<T> {

	T fetch() throws FetchException;

	T forceFetch() throws FetchException;

	void update() throws FetchException;

}
