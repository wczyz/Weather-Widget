package weatherApp.control;

import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

public class TemperatureValueControl extends WeatherControl {

    public TemperatureValueControl() {
        noDataIcon.getStyleClass().remove("no-data");
        getChildren().remove(noDataIcon);
        noDataIcon.getStyleClass().add("temperature-label");
        getChildren().add(noDataIcon);
    }

    @Override
    protected void createContentControls() {

        getChildren().remove(noDataIcon);

        textControl = new Text();
        textControl.getStyleClass().add("temperature-label");

        prefixLabel = new Text();
        prefixLabel.textProperty().bind(prefixProperty);
        prefixLabel.getStyleClass().add("temperature-label");

        suffixLabel = new Text();
        suffixLabel.textProperty().bind(suffixProperty);
        suffixLabel.getStyleClass().add("temperature-label");

        innerContainer = new HBox();
        innerContainer.getStyleClass().add("value-container");
        innerContainer.getChildren().addAll(prefixLabel, textControl, suffixLabel);

        getChildren().add(innerContainer);

    }

}
