package weatherApp.control;

import com.jfoenix.validation.base.ValidatorBase;
import javafx.scene.control.TextInputControl;

public class IntervalValidator extends ValidatorBase {
	private static final int SECONDS_IN_A_DAY = 24 * 60 * 60;

	@Override
	protected void eval() {
		TextInputControl control = (TextInputControl) srcControl.get();
		try {
			int i = Integer.parseInt(control.getText());
			hasErrors.set(i < 1 || i > SECONDS_IN_A_DAY);
		} catch (NumberFormatException e) {
			hasErrors.set(true);
		}
	}
}
