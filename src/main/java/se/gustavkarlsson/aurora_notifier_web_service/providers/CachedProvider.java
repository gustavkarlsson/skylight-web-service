package se.gustavkarlsson.aurora_notifier_web_service.providers;

public interface CachedProvider<T> extends Provider<T> {

	T getLastValue() throws CacheException;

}
