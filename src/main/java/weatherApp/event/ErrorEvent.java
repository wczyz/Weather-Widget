package weatherApp.event;

import java.time.LocalDateTime;

public final class ErrorEvent extends WeatherEvent {
	private final LocalDateTime timestamp;
	private final Throwable cause;

	public ErrorEvent(Throwable cause) {
		this.timestamp = LocalDateTime.now();
		this.cause = cause;
	}

	public LocalDateTime getTimestamp() {
		return this.timestamp;
	}

	public Throwable getCause() {
		return this.cause;
	}

	@Override
	public String toString() {
		return "ErrorEvent(timestamp=" + this.getTimestamp() + ", cause=" + this.getCause() + ")";
	}

}