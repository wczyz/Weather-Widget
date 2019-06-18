package weatherApp.event;

import java.time.LocalDateTime;

public final class RawValueEvent extends WeatherEvent {
	private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RawValueEvent.class);

	private final LocalDateTime timestamp;
	private final String value;

	public RawValueEvent(final LocalDateTime timestamp, final String value) {
		this.timestamp = timestamp;
		this.value = value;

		log.info(this);
	}

	public LocalDateTime getTimestamp() {
		return this.timestamp;
	}

	public String getValue() {
		return this.value;
	}

	@Override
	public String toString() {
		return "RawValueEvent(timestamp=" + this.getTimestamp() + ", value=" + this.getValue() + ")";
	}

}
