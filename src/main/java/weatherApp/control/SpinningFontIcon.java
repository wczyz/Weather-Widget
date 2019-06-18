package weatherApp.control;

import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.scene.effect.MotionBlur;
import javafx.util.Duration;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SpinningFontIcon extends FontIcon {

	private static final Duration duration = Duration.seconds(2);

	private RotateTransition transition;

	public SpinningFontIcon() {
		super();
		blurWorkaround();
		setUpAnimation();
	}

	public SpinningFontIcon(String iconCode) {
		super(iconCode);
		blurWorkaround();
		setUpAnimation();
	}

	public SpinningFontIcon(Ikon iconCode) {
		super(iconCode);
		blurWorkaround();
		setUpAnimation();
	}

	public void stop() {
		if (transition != null) {
			transition.stop();
		}
	}

	public void play() {
		if (transition == null) {
			setUpAnimation();
		}
	}

	protected void setUpAnimation() {
		stop();
		transition = new RotateTransition(duration, this);
		transition.setByAngle(360);
		transition.setFromAngle(0);
		transition.setInterpolator(Interpolator.EASE_IN);
		transition.setCycleCount(1);
		transition.setOnFinished(e -> {
			transition = new RotateTransition(duration, this);
			transition.setByAngle(360);
			transition.setFromAngle(0);
			transition.setInterpolator(Interpolator.LINEAR);
			transition.setCycleCount(-1);

			transition.play();
		});

		transition.play();
	}

	private void blurWorkaround() {
		/*
		 * Hack alert! There's some strange issue with JavaFX's GPU accelerated
		 * renderer, namely icons animated with RotateTransition wobble
		 * (vibrate, shake, look bad in general). This does not happen on
		 * software renderers. Interestingly enough, Chromium Embedded Framework
		 * exhibits the very same behavior (wobbles with D3D, works fine on
		 * software renderer). Switching to a non-accelerated renderer could be
		 * a solution but it would consume considerable amounts of CPU. Now, the
		 * funny part. As
		 * per @link{https://github.com/FortAwesome/Font-Awesome/issues/671} ,
		 * adding
		 *
		 * -webkit-filter: blur(0)
		 *
		 * to the icon style (CSS) removes the mentioned shaking in CEF. So
		 * here's a JavaFx version of the same hack for you.
		 */
		String pipelineName = getCurrentGraphicsPipelineName();
		if (pipelineName.endsWith("D3DPipeline") || pipelineName.endsWith("ES2Pipeline")) {
			this.setEffect(new MotionBlur(0, 0));
		}

	}

	private static String getCurrentGraphicsPipelineName() {
		try {
			Class<?> clazz = Class.forName("com.sun.prism.GraphicsPipeline");
			Method m = clazz.getDeclaredMethod("getPipeline");
			return m.invoke(null).getClass().getName();
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			return "";
		}
	}

}
