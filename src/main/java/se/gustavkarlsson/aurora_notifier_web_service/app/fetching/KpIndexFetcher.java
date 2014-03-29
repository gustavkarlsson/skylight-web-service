package se.gustavkarlsson.aurora_notifier_web_service.app.fetching;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import se.gustavkarlsson.aurora_notifier_web_service.domain.KpIndexHolder;

public abstract class KpIndexFetcher implements Fetcher<KpIndexHolder> {

	private final Duration minimumDurationBetweenUpdates;

	private KpIndexHolder lastKpIndexHolder = null;
	private DateTime lastUpdatedTime = null;

	public KpIndexFetcher(Duration minimumDurationBetweenUpdates) {
		this.minimumDurationBetweenUpdates = minimumDurationBetweenUpdates;
	}

	@Override
	public synchronized KpIndexHolder fetch() throws FetchException {
		if (isUpdateNeeded()) {
			update();
		}
		return lastKpIndexHolder;
	}

	@Override
	public synchronized KpIndexHolder forceFetch() throws FetchException {
		update();
		return lastKpIndexHolder;
	}

	@Override
	public synchronized void update() throws FetchException {
		lastKpIndexHolder = fetchKpIndex();
		lastUpdatedTime = new DateTime(lastKpIndexHolder.getTimestamp());
	}

	private boolean isUpdateNeeded() {
		if (lastKpIndexHolder == null || lastUpdatedTime == null) {
			return true;
		}
		DateTime updateNeededTime = lastUpdatedTime.plus(minimumDurationBetweenUpdates);
		boolean updateNeeded = DateTime.now().isAfter(updateNeededTime);
		return updateNeeded;
	}

	protected abstract KpIndexHolder fetchKpIndex() throws FetchException;
}
