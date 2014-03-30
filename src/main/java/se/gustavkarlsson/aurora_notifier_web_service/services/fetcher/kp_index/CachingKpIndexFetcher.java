package se.gustavkarlsson.aurora_notifier_web_service.services.fetcher.kp_index;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import se.gustavkarlsson.aurora_notifier_web_service.domain.KpIndexHolder;
import se.gustavkarlsson.aurora_notifier_web_service.services.fetcher.FetchException;
import se.gustavkarlsson.aurora_notifier_web_service.services.fetcher.Fetcher;

public abstract class CachingKpIndexFetcher implements Fetcher<KpIndexHolder> {

	private final Duration minimumDurationBetweenUpdates;

	private KpIndexHolder cachedKpIndexHolder = null;
	private DateTime lastUpdatedTime = null;

	protected CachingKpIndexFetcher(Duration minimumDurationBetweenUpdates) {
		this.minimumDurationBetweenUpdates = minimumDurationBetweenUpdates;
	}

	@Override
	public synchronized KpIndexHolder fetch() throws FetchException {
		if (isUpdateNeeded()) {
			try {
				updateCache();
			} catch (FetchException e) {
				if (cachedKpIndexHolder == null) {
					throw new FetchException("Update failed and no cached value is available.", e);
				}
			}
		}
		return cachedKpIndexHolder;
	}

	private void updateCache() throws FetchException {
		cachedKpIndexHolder = fetchKpIndex();
		lastUpdatedTime = new DateTime(cachedKpIndexHolder.getTimestamp());
	}

	private boolean isUpdateNeeded() {
		if (cachedKpIndexHolder == null || lastUpdatedTime == null) {
			return true;
		}
		DateTime updateNeededTime = lastUpdatedTime.plus(minimumDurationBetweenUpdates);
		boolean updateNeeded = DateTime.now().isAfter(updateNeededTime);
		return updateNeeded;
	}

	protected abstract KpIndexHolder fetchKpIndex() throws FetchException;
}
