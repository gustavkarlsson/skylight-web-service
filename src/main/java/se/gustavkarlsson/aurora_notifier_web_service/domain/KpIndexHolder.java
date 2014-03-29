package se.gustavkarlsson.aurora_notifier_web_service.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;

public class KpIndexHolder {

	@DecimalMin("0")
	@DecimalMax("10")
	private float kpIndex;

	@DecimalMin("1388534400000") // 1 Jan 2014 00:00
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