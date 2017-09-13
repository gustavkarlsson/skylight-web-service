package se.gustavkarlsson.aurora_notifier.web_service.repository;

import se.gustavkarlsson.aurora_notifier.common.domain.Timestamped;

import javax.inject.Singleton;

@Singleton
public class KpIndexRepository {
	private Timestamped<Float> lastKpIndex;

	public Timestamped<Float> getLastKpIndex() {
		return lastKpIndex;
	}

	public void setLastKpIndex(Timestamped<Float> lastKpIndex) {
		this.lastKpIndex = lastKpIndex;
	}
}
