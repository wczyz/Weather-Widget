package weatherApp.event;

public final class WeatherValueEvent extends ValueEvent {
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(WeatherValueEvent.class);

    private final WeatherValueType weatherValueType;
    private final String value;

    public WeatherValueEvent(final WeatherValueType weatherValueType, final String value) {
        this.weatherValueType = weatherValueType;
        this.value = value;

        log.info(this);
    }

    public WeatherValueType getWeatherType() {
        return this.weatherValueType;
    }

    public String getValue() {
        return this.value;
    }

    public String toString() {
        return "WeatherValueEvent(value=" + this.getValue() + ")";
    }
}
