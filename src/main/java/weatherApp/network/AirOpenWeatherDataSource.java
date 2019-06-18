package weatherApp.network;

import com.google.gson.JsonElement;
import io.reactivex.netty.RxNetty;
import rx.Observable;
import weatherApp.event.WeatherValueEvent;
import weatherApp.event.WeatherValueType;

public class AirOpenWeatherDataSource extends WeatherDataSource {

    private static String[] URLS = {"http://api.gios.gov.pl/pjp-api/rest/data/getData/3585",
                                  "http://api.gios.gov.pl/pjp-api/rest/data/getData/3584"};

    private static String JSON_SINGLE_KEY = "value";
    private static String JSON_MULTIPLE_KEY = "values";

    private WeatherValueType weatherValueType;
    private String url;

    public AirOpenWeatherDataSource(WeatherValueType weatherValueType) {

        this.weatherValueType = weatherValueType;

        switch (weatherValueType) {
            case PM25:
                url = URLS[0];
                break;

            case PM10:
                url = URLS[1];
                break;
        }
    }

    @Override
    protected Observable<WeatherValueEvent> makeRequest() {

        return RxNetty.createHttpRequest(JsonHelper.withJsonHeader(prepareHttpGETRequest(url)))
                .compose(this::unpackResponse).map(JsonHelper::asJsonObject).map(jsonObject -> {

                    int index = 0;
                    String value;
                    JsonElement element;

                    do {
                        element = jsonObject.get(JSON_MULTIPLE_KEY).getAsJsonArray().get(index).getAsJsonObject()
                                .get(JSON_SINGLE_KEY);
                        index++;

                    } while (element.isJsonNull());

                    value = element.getAsString();
                    return new WeatherValueEvent(weatherValueType, value);
                });

    }
}
