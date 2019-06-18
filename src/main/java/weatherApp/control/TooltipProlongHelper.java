package weatherApp.control;

import javafx.scene.control.Tooltip;
import javafx.util.Duration;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class TooltipProlongHelper {
	private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TooltipProlongHelper.class);

	/*
	 * Some hardcore stuff here. JavaFX is surprisingly inflexible when it comes
	 * to displaying tooltips. It does have a builtin class for that, namely
	 * javafx.scene.control.Tooltip but the class contains hardcoded constants
	 * that, for instance, specify the duration for which all tooltips remain
	 * visible. When the timer elapses tooltips autohide themselves. This
	 * behavior cannot be easily altered as the mechanism is private to
	 * javafx.scene.control.Tooltip . The code below accesses that private data.
	 * This is ugly and fragile, no doubt about it. But the alternative way is
	 * to write our own implementation of tooltips which is a lot of code
	 * (javafx.scene.control.Tooltip is just shy of 1k lines of code).
	 *
	 * Kids, don't try this at home!
	 */
	public static void setTooltipDuration(Duration openDelay, Duration visibleDuration, Duration closeDelay) {
		try {
			Field field = Tooltip.class.getDeclaredField("BEHAVIOR");
			field.setAccessible(true);

			Class<?> behaviorClass = findBehaviorClass();
			if (behaviorClass == null) {
				log.warn("Can't find tooltip behavior class");
				return;
			}

			Constructor<?> behaviorConstructor = behaviorClass.getDeclaredConstructor(Duration.class, Duration.class,
					Duration.class, boolean.class);
			if (behaviorConstructor == null) {
				log.warn("Can't find proper behavior constructor");
				return;
			}

			behaviorConstructor.setAccessible(true);
			field.set(null, behaviorConstructor.newInstance(openDelay, visibleDuration, closeDelay, false));

		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException
				| NoSuchMethodException | InstantiationException | InvocationTargetException e) {
			log.fatal(e);
			return;
		}
	}

	private static Class<?> findBehaviorClass() {
		for (Class<?> clazz : Tooltip.class.getDeclaredClasses()) {
			if (clazz.getName().endsWith("TooltipBehavior")) {
				return clazz;
			}
		}
		return null;
	}
}
