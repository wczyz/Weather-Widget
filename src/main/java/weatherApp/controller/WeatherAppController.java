package weatherApp.controller;

import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

import rx.Observable;
import rx.observables.JavaFxObservable;
import rx.schedulers.JavaFxScheduler;

import weatherApp.control.WeatherControl;
import weatherApp.event.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

import static weatherApp.event.EventStream.*;

public class WeatherAppController {


    private static final int ERROR_MSG_MAX_LENGTH = 400;
    private static final int ERROR_MSG_DURATION = 30; // Show error icon for 30
                                                        // seconds

	@FXML
	private Label title;

	@FXML
    private GridPane firstGrid;

	@FXML
    private  GridPane secondGrid;

	@FXML
    private WeatherControl temperatureValueControl;

    @FXML
    private WeatherControl cloudinessValueControl;

    @FXML
    private WeatherControl barometerValueControl;

    @FXML
    private WeatherControl windForceValueControl;

    @FXML
    private WeatherControl windDirectionValueControl;

    @FXML
    private WeatherControl humidityValueControl;

    @FXML
    private WeatherControl pm25ValueControl;

    @FXML
    private WeatherControl pm10ValueControl;

    @FXML
    private Node errorIcon;

    @FXML
    private Node workingIcon;

    @FXML
    private Button refreshButton;

    @FXML
    private Button settingsButton;

	@FXML
	private void initialize() {
		initializeStatus();
	    initializeTitle();
        initializeGridAlignment();
        setDefaultFormats();

        initalizeRefreshHandler();
        initializeSettingsHandler();

        initializeTooltips();
	}

	private void initializeTitle() {
		GridPane.setHalignment(title, HPos.CENTER);
		GridPane.setValignment(title, VPos.TOP);
		title.setPrefWidth(1000);
		title.setPrefHeight(100);
		title.setAlignment(Pos.CENTER);
        title.setTextFill(Color.web("black", 0.85));
    }

    private void initializeGridAlignment() {
	    GridPane.setHalignment(firstGrid, HPos.CENTER);
	    GridPane.setValignment(firstGrid, VPos.BOTTOM);

	    GridPane.setHalignment(secondGrid, HPos.CENTER);
	    GridPane.setValignment(secondGrid, VPos.BOTTOM);

	    for (Node element : secondGrid.getChildren()) {
	        GridPane.setValignment(element, VPos.BOTTOM);
        }
    }

    private void setDefaultFormats() {
	    windForceValueControl.setFormat("0.0");
    }

    public Observable<RawValueEvent> getTemperatureValue() {
        return getWeatherValueStream(WeatherValueType.TEMPERATURE);
    }

    public Observable<RawValueEvent> getCloudinessValue() {
        return getWeatherValueStream(WeatherValueType.CLOUDINESS);
    }

    public Observable<RawValueEvent> getBarometerValue() {
        return getWeatherValueStream(WeatherValueType.BAROMETER);
    }

    public Observable<RawValueEvent> getWindForceValue() {
        return getWeatherValueStream(WeatherValueType.WIND_FORCE);
    }

    public Observable<RawValueEvent> getWindDirectionValue() {
        return getWeatherValueStream(WeatherValueType.WIND_DIRECTION);
    }

    public Observable<RawValueEvent> getHumidityValue() {
        return getWeatherValueStream(WeatherValueType.HUMIDITY);
    }

    public Observable<RawValueEvent> getPM25Value() {
        return getWeatherValueStream(WeatherValueType.PM25);
    }

    public Observable<RawValueEvent> getPM10Value() {
        return getWeatherValueStream(WeatherValueType.PM10);
    }

    private void initalizeRefreshHandler() {
        joinStream(JavaFxObservable.actionEventsOf(refreshButton).map(e -> new RefreshRequestEvent()));
    }

    private void initializeSettingsHandler() {
        joinStream(JavaFxObservable.actionEventsOf(settingsButton).map(e -> new SettingsRequestEvent()));
    }

    private void initializeTooltips() {
        Tooltip.install(workingIcon, new Tooltip("Fetching data..."));

        Tooltip errorTooltip = new Tooltip();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        eventStream().eventsInFx().ofType(ErrorEvent.class).subscribe(e -> {
            ByteArrayOutputStream ostream = new ByteArrayOutputStream();
            e.getCause().printStackTrace(new PrintStream(ostream));
            String details = new String(ostream.toByteArray());
            if (details.length() > ERROR_MSG_MAX_LENGTH) {
                details = details.substring(0, ERROR_MSG_MAX_LENGTH) + "\u2026"; // Add
                // ellipsis
                // (...)
                // at
                // the
                // end
            }

            errorTooltip.setText(MessageFormat.format("An error has occurred ({0}):\n{1}",
                    e.getTimestamp().format(formatter), details));
        });
        Tooltip.install(errorIcon, errorTooltip);

    }

    private void initializeStatus() {
        Observable<WeatherEvent> events = eventStream().eventsInFx();

        // Basically, we keep track of the difference between issued requests
        // and completed requests
        // If this difference is > 0 we display the spinning icon...
        workingIcon.visibleProperty()
                .bind(binding(events.ofType(NetworkRequestIssuedEvent.class).map(e -> 1) // Every
                        // issued
                        // request
                        // contributes
                        // +1
                        .mergeWith(events.ofType(NetworkRequestFinishedEvent.class).map(e -> -1) // Every
                                // completed
                                // request
                                // contributes
                                // -1
                                .delay(2, TimeUnit.SECONDS, JavaFxScheduler.getInstance())) // We delay
                        // completion
                        // events for 2
                        // seconds so
                        // that the
                        // spinning icon
                        // is always
                        // displayed for
                        // at least 2
                        // seconds and
                        // it does not
                        // blink
                        .scan(0, (x, y) -> x + y).map(v -> v > 0))

                );

        /*
         * This should show the error icon when an error event arrives and hides
         * the icon after 30 seconds unless another error arrives
         */
        Observable<ErrorEvent> errors = events.ofType(ErrorEvent.class);
        errorIcon.visibleProperty()
                .bind(onEvent(errors, true).andOn(
                        errors.throttleWithTimeout(ERROR_MSG_DURATION, TimeUnit.SECONDS, JavaFxScheduler.getInstance()),
                        false).toBinding());
    }

    private Observable<RawValueEvent> getWeatherValueStream(WeatherValueType type) {
        return eventStream().eventsInFx().ofType(WeatherValueEvent.class).filter(e -> e.getWeatherType() == type)
                .map(e -> new RawValueEvent(e.getTimestamp(), e.getValue()));
    }

}
