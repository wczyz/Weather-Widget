package weatherApp.event;

import java.time.LocalDateTime;

public class ValueEvent extends WeatherEvent {
    private final LocalDateTime timestamp;

    public ValueEvent() {
        timestamp = LocalDateTime.now();
    }

    public LocalDateTime getTimestamp() {
        return this.timestamp;
    }

    @Override
    public String toString() {
        return "ValueEvent(timestamp=" + this.getTimestamp() + ")";
    }

}
