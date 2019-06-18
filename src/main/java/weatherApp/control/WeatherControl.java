package weatherApp.control;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.weathericons.WeatherIcons;
import rx.Observable;
import weatherApp.event.RawValueEvent;

import java.text.DecimalFormat;

public class WeatherControl extends Pane {

    private String formatPattern = "0";
    private DecimalFormat format = new DecimalFormat(formatPattern);

    private ObjectProperty<Observable<RawValueEvent>> sourceProperty = new SimpleObjectProperty<>();

    FontIcon noDataIcon = new FontIcon(WeatherIcons.NA);

    Text prefixLabel;
    Text suffixLabel;
    Text textControl;

    HBox innerContainer;

    StringProperty prefixProperty = new SimpleStringProperty();
    StringProperty suffixProperty = new SimpleStringProperty();
    private StringProperty titleProperty = new SimpleStringProperty("-");

    public Observable<RawValueEvent> getSource() {
        return sourceProperty.get();
    }

    public static boolean isNumeric(String str) {
        return str.matches("^(0|[1-9]\\d*)($|\\.\\d+$)");
    }

    public void setSource(Observable<RawValueEvent> source) {
        source.subscribe(e -> {
            if (innerContainer == null) {
                createContentControls();
            }

            if (isNumeric(e.getValue())) {
                textControl.setText(format.format(Float.parseFloat(e.getValue())));
            }
            else {
                textControl.setText(e.getValue());
            }
        });
        sourceProperty.set(source);
    }

    public String getFormat() {
        return formatPattern;
    }

    public void setFormat(String pattern) {
        formatPattern = pattern;
        format = new DecimalFormat(pattern);
    }

    public String getPrefix() {
        return prefixProperty.get();
    }

    public void setPrefix(String prefix) {
        prefixProperty.set(prefix);
    }

    public String getSuffix() {
        return suffixProperty.get();
    }

    public void setSuffix(String suffix) {
        suffixProperty.set(suffix);
    }

    public String getTitle() {
        return titleProperty.get();
    }

    public void setTitle(String title) {
        titleProperty.set(title);
    }

    public WeatherControl() {
        noDataIcon.getStyleClass().add("no-data");
        getChildren().add(noDataIcon);
    }

    protected void createContentControls() {

        getChildren().remove(noDataIcon);

        textControl = new Text();
        textControl.getStyleClass().add("weather-value");

        prefixLabel = new Text();
        prefixLabel.textProperty().bind(prefixProperty);
        prefixLabel.getStyleClass().add("helper-label");

        suffixLabel = new Text();
        suffixLabel.textProperty().bind(suffixProperty);
        suffixLabel.getStyleClass().add("helper-label");

        innerContainer = new HBox();
        innerContainer.getStyleClass().add("value-container");
        innerContainer.getChildren().addAll(prefixLabel, textControl, suffixLabel);

        getChildren().add(innerContainer);

    }

    @Override
    protected void layoutChildren() {
        /* Custom children positioning */
        super.layoutChildren();

        if (noDataIcon.isVisible()) {
            noDataIcon.relocate((getWidth() - noDataIcon.getLayoutBounds().getWidth()) / 2,
                    (getHeight() - noDataIcon.getLayoutBounds().getHeight()) / 2);
        }

        if (innerContainer != null) {
            innerContainer.relocate((getWidth() - innerContainer.getLayoutBounds().getWidth()) / 2,
                    (getHeight() - innerContainer.getLayoutBounds().getHeight()) / 2);
        }
    }
}
