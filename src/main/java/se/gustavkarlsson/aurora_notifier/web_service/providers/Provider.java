package se.gustavkarlsson.aurora_notifier.web_service.providers;

public interface Provider<T> {

	T getValue() throws ProviderException;
}