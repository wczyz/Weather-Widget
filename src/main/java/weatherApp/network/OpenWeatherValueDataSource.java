package weatherApp.network;

import com.google.gson.JsonElement;
import io.reactivex.netty.RxNetty;
import rx.Observable;
import weatherApp.event.*;

public class OpenWeatherValueDataSource extends WeatherDataSource {

    private static String URL = "http://api.openweathermap.org/data/2.5/weather?id=756135&appid=2b59649d25d2dac085262cdde51bfdfe";

    private String json_first_key = "main";
    private String json_second_key = "temp";

    private WeatherValueType weatherValueType;

    public OpenWeatherValueDataSource(WeatherValueType weatherValueType) {
        this.weatherValueType = weatherValueType;

        switch (weatherValueType) {
            case TEMPERATURE:
                json_first_key = "main";
                json_second_key = "temp";
                break;

            case CLOUDINESS:
                json_first_key = "weather";
                json_second_key = "description";
                break;

            case BAROMETER:
                json_first_key = "main";
                json_second_key = "pressure";
                break;

            case WIND_FORCE:
                json_first_key = "wind";
                json_second_key = "speed";
                break;

            case WIND_DIRECTION:
                json_first_key = "wind";
                json_second_key = "deg";
                break;

            case HUMIDITY:
                json_first_key = "main";
                json_second_key = "humidity";
                break;
        }
    }

    @Override
    protected Observable<WeatherValueEvent> makeRequest() {

        return RxNetty.createHttpRequest(JsonHelper.withJsonHeader(prepareHttpGETRequest(URL)))
                .compose(this::unpackResponse).map(JsonHelper::asJsonObject).map(jsonObject -> {

                    if (weatherValueType.equals(WeatherValueType.CLOUDINESS)) {
                        JsonElement element = jsonObject.get(json_first_key).getAsJsonArray().get(0).getAsJsonObject()
                                .get(json_second_key);

                        if (element.isJsonNull())
                            return new WeatherValueEvent(weatherValueType, "-");

                        return new WeatherValueEvent(weatherValueType, element.getAsString());
                    }

                    JsonElement element = jsonObject.get(json_first_key).getAsJsonObject().get(json_second_key);

                    if (element == null)
                        return new WeatherValueEvent(weatherValueType, "-");

                    if (weatherValueType.equals(WeatherValueType.TEMPERATURE)) {
                        float value = element.getAsFloat();
                        value -= 273.15;            // conversion from kelvin to celsius
                        return new WeatherValueEvent(weatherValueType, Float.toString(value));
                    }

                    return new WeatherValueEvent(weatherValueType, Float.toString(element.getAsFloat()));
                });

    }

}
