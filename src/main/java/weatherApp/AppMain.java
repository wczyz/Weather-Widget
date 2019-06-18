package weatherApp;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.jfoenix.controls.*;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import rx.Observable;
import rx.Subscription;
import rx.observables.JavaFxObservable;
import weatherApp.control.TooltipProlongHelper;
import weatherApp.event.ValueEvent;
import weatherApp.event.SettingsRequestEvent;
import weatherApp.event.WeatherEvent;
import weatherApp.event.WeatherValueType;
import weatherApp.network.*;
import weatherApp.network.AirOpenWeatherDataSource;
import weatherApp.network.MeteoValueDataSource;
import weatherApp.network.OpenWeatherValueDataSource;

import static weatherApp.event.EventStream.eventStream;
import static weatherApp.event.EventStream.joinStream;

public class AppMain extends Application {
	private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AppMain.class);

	private static final String FXML_MAIN_FORM_TEMPLATE = "/fxml/weatherApp-main.fxml";
	private static final String FXML_CLOSE_DIALOG_TEMPLATE = "/fxml/close-dialog.fxml";
	private static final String FXML_SETTINGS_DIALOG_TEMPLATE = "/fxml/settings-dialog.fxml";

	private static final String FONT_CSS = "/css/jfoenix-fonts.css";
	private static final String MATERIAL_CSS = "/css/jfoenix-design.css";
	private static final String JFX_CSS = "/css/jfx.css";

	private class DialogControllerBase {
		@FXML
		JFXDialog dialog;

		@FXML
		Button acceptButton;

		@FXML
		Button cancelButton;

		void initialize() {
			JavaFxObservable.actionEventsOf(cancelButton).subscribe(ignore -> {
				dialog.close();
			});
		}

		void show(StackPane pane) {
			dialog.show(pane);
		}

	}

	private class CloseDialogController extends DialogControllerBase {
		@FXML
		void initialize() {
			super.initialize();

			JavaFxObservable.actionEventsOf(acceptButton).subscribe(ignore -> {
				log.info("Exiting...");
				AppMain.this.mainStage.close(); // This should terminate the
												// application
				System.exit(0); // Just for sure
			});
		}
	}

	private class SettingsDialogController extends DialogControllerBase {
		@FXML
		JFXTextField intervalField;

		@FXML
		VBox buttonBox;

		@FXML
		JFXRadioButton openWeatherButton;

		@FXML
		JFXRadioButton meteoButton;

		@FXML
		void initialize() {
			super.initialize();

			buttonBox.setAlignment(Pos.CENTER_LEFT);
			buttonBox.setSpacing(20);

			openWeatherButton.setSelected(true);
			ToggleGroup radioGroup = new ToggleGroup();
			openWeatherButton.setToggleGroup(radioGroup);
			meteoButton.setToggleGroup(radioGroup);



			intervalField.textProperty().addListener((control, newValue, oldValue) -> intervalField.validate());
			intervalField.setText(WeatherDataSource.getDefaultPollInterval().toString());
			acceptButton.disableProperty().bind(intervalField.getValidators().get(0).hasErrorsProperty());

			JavaFxObservable.actionEventsOf(acceptButton).subscribe(ignore -> {
				try {
					int interval = Integer.parseInt(intervalField.getText());

					AppMain.this.sourceStreams.stream().forEach(Subscription::unsubscribe);
					AppMain.this.sourceStreams.clear();

					RadioButton selectedRadioButton = (RadioButton) radioGroup.getSelectedToggle();

					switch (selectedRadioButton.getText()) {
						case "OpenWeatherMap":
							AppMain.this.setupOpenWeatherDataSources(interval);
							break;

						case "Meteo Warszawa":
							AppMain.this.setupMeteoDataSources(interval);
					}

				} finally {
					dialog.close();
				}
			});

		}

	}

	private DialogControllerBase closeDialogController;
	private DialogControllerBase settingsDialogController;
	private Stage mainStage;

	private List<Subscription> sourceStreams = new LinkedList<>();

	@Override
	public void start(Stage primaryStage) throws Exception {
		log.info("Starting Weather application...");

		mainStage = primaryStage;

		setupTooltipDuration();

		setupOpenWeatherDataSources(null);

		setupEventHandler();

		Parent pane = FXMLLoader.load(AppMain.class.getResource(FXML_MAIN_FORM_TEMPLATE));
		/*
		 * Transform the main stage (aka the main window) into an undecorated
		 * window
		 */
		JFXDecorator decorator = new JFXDecorator(mainStage, pane, false, false, true);
		ObservableList<Node> buttonsList = ((Pane) decorator.getChildren().get(0)).getChildren();
		buttonsList.get(buttonsList.size() - 1).getStyleClass().add("close-button"); // Style
																						// the
																						// close
																						// button
																						// differently

		decorator.setOnCloseButtonAction(this::onClose);

		Scene scene = new Scene(decorator);
		scene.setFill(null);

		scene.getStylesheets().addAll(AppMain.class.getResource(FONT_CSS).toExternalForm(),
				AppMain.class.getResource(MATERIAL_CSS).toExternalForm(),
				AppMain.class.getResource(JFX_CSS).toExternalForm());

		mainStage.setScene(scene);

		mainStage.setWidth(600);
		mainStage.setHeight(400);
		mainStage.setResizable(false);

		mainStage.show();

		log.info("Application's up and running!");
	}

	private void onClose() {
		log.info("onClose");

		if (closeDialogController == null) {
			closeDialogController = new CloseDialogController();
			createDialog(closeDialogController, FXML_CLOSE_DIALOG_TEMPLATE);
		}

		closeDialogController.show(getMainPane());
	}

	private void onSettingsRequested() {
		log.info("onSettingsRequested");

		if (settingsDialogController == null) {
			settingsDialogController = new SettingsDialogController();
			createDialog(settingsDialogController, FXML_SETTINGS_DIALOG_TEMPLATE);
		}

		settingsDialogController.show(getMainPane());
	}

	private StackPane getMainPane() {
		return (StackPane) mainStage.getScene().getRoot().lookup("#main");
	}

	private void createDialog(Object dialogController, String fxmlPath) {
		FXMLLoader loader = new FXMLLoader(AppMain.class.getResource(fxmlPath));
		loader.setController(dialogController);
		try {
			loader.load();
		} catch (IOException e) {
			log.error(e);
			throw new RuntimeException(e);
		}
	}

	private void setupOpenWeatherDataSources(Integer pollInterval) {
		WeatherDataSource[] sources = { new OpenWeatherValueDataSource(WeatherValueType.TEMPERATURE),
				new OpenWeatherValueDataSource(WeatherValueType.CLOUDINESS),
				new OpenWeatherValueDataSource(WeatherValueType.BAROMETER),
				new OpenWeatherValueDataSource(WeatherValueType.WIND_FORCE),
				new OpenWeatherValueDataSource(WeatherValueType.WIND_DIRECTION),
				new OpenWeatherValueDataSource(WeatherValueType.HUMIDITY),
				new AirOpenWeatherDataSource(WeatherValueType.PM25),
				new AirOpenWeatherDataSource(WeatherValueType.PM10) };

		for (WeatherDataSource source : sources) {
			sourceStreams.add(joinStream(source.dataSourceStream(pollInterval)));
		}
	}

	private void setupMeteoDataSources(Integer pollInterval) {
		WeatherDataSource[] sources = {new MeteoValueDataSource(WeatherValueType.TEMPERATURE),
				new MeteoValueDataSource(WeatherValueType.CLOUDINESS),
				new MeteoValueDataSource(WeatherValueType.BAROMETER),
				new MeteoValueDataSource(WeatherValueType.WIND_FORCE),
				new MeteoValueDataSource(WeatherValueType.WIND_DIRECTION),
				new MeteoValueDataSource(WeatherValueType.HUMIDITY),
				new AirOpenWeatherDataSource(WeatherValueType.PM25),
				new AirOpenWeatherDataSource(WeatherValueType.PM10) };

		for (WeatherDataSource source : sources) {
			sourceStreams.add(joinStream(source.dataSourceStream(pollInterval)));
		}
	}

	private void setupEventHandler() {
		Observable<WeatherEvent> events = eventStream().events();

		events.ofType(ValueEvent.class).subscribe(log::info);

		events.ofType(SettingsRequestEvent.class).subscribe(e -> onSettingsRequested());
	}

	private static void setupExceptionHandler() {
		Thread.setDefaultUncaughtExceptionHandler(
				(t, e) -> log.error("Uncaught exception in thread \'" + t.getName() + "\'", e));
	}

	private static void setupTooltipDuration() {
		TooltipProlongHelper.setTooltipDuration(Duration.millis(500), Duration.minutes(10), Duration.millis(500));
	}

    private static void setupTextRendering() {
        /*
         * A workaround for the font rendering issue on some platforms.
         * Consult: @link{https://stackoverflow.com/questions/18382969/can-the-
         * rendering-of-the-javafx-2-8-font-be-improved} and linked materials
         */
        System.setProperty("prism.text", "t2k");
        System.setProperty("prism.lcdtext", "true");
    }

	public static void main(String[] args) {
		setupExceptionHandler();

		setupTextRendering();

		Platform.setImplicitExit(true); // This should exit the application when
										// the main window gets closed
		Application.launch(AppMain.class, args);
	}
}
