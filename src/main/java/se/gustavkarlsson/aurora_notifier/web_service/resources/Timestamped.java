package se.gustavkarlsson.aurora_notifier.web_service.resources;

import javax.validation.constraints.NotNull;
import java.util.Objects;

public final class Timestamped<T> {
	private T value;
	private long timestamp;

	private Timestamped() {
	}

	public Timestamped(@NotNull T value, long timestamp) {
		this.value = Objects.requireNonNull(value);
		this.timestamp = timestamp;
	}

	@NotNull
	public T getValue() {
		return this.value;
	}

	public long getTimestamp() {
		return this.timestamp;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Timestamped<?> that = (Timestamped<?>) o;
		return timestamp == that.timestamp &&
				value.equals(that.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(value, timestamp);
	}

	@Override
	public String toString() {
		return "Timestamped{" +
				"value=" + value +
				", timestamp=" + timestamp +
				'}';
	}
}
