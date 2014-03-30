package se.gustavkarlsson.aurora_notifier_web_service.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public class KpIndexHolder {

	private float kpIndex;
	private long timestamp;

	public KpIndexHolder() {
		// Jackson deserialization
	}

	public KpIndexHolder(float kpIndex, long timestamp) {
		this.kpIndex = kpIndex;
		this.timestamp = timestamp;
	}

	@JsonProperty
	public float getKpIndex() {
		return kpIndex;
	}

	@JsonProperty
	public long getTimestamp() {
		return timestamp;
	}
}