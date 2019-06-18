package weatherApp.network;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import rx.Observable;
import weatherApp.event.*;

import java.io.IOException;

public class MeteoValueDataSource extends WeatherDataSource {

    private static String URL = "http://www.meteo.waw.pl/";

    private WeatherValueType weatherValueType;
    private String cssPath;

    public MeteoValueDataSource(WeatherValueType weatherValueType) {
        this.weatherValueType = weatherValueType;

        switch (weatherValueType) {
            case TEMPERATURE:
                cssPath = "span[id=\"PARAM_0_TA\"]";
                break;

            case BAROMETER:
                cssPath = "span[id=\"PARAM_0_PR\"]";
                break;

            case WIND_FORCE:
                cssPath = "span[id=\"PARAM_0_WV\"]";
                break;

            case WIND_DIRECTION:
                cssPath = "strong[id=\"PARAM_WD\"]";
                break;

            case HUMIDITY:
                cssPath = "span[id=\"PARAM_0_RH\"]";
                break;
        }
    }



    @Override
    protected Observable<WeatherValueEvent> makeRequest() {

        String text;

        if (weatherValueType.equals(WeatherValueType.CLOUDINESS)) {
            text = "-";

            Observable<WeatherValueEvent> observable =
                    Observable.just(new WeatherValueEvent(weatherValueType, text));

            return observable;
        }

        try {
            Document doc = Jsoup.connect(URL).get();
            Element element = doc.select(cssPath).first();
            text = element.text().replace(',', '.');

        } catch (IOException io) {
            text = "-";
        }

        Observable<WeatherValueEvent> observable =
                Observable.just(new WeatherValueEvent(weatherValueType, text));

        return observable;
    }

}