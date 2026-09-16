package spicato_admin.client;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

final class KnobSlider extends AbstractSliderButton {
	private final Component label;
	private final double min;
	private final double max;
	private final double step;
	private final boolean integer;
	private final DoubleConsumer setter;

	KnobSlider(
			int width,
			Component label,
			double min,
			double max,
			double step,
			boolean integer,
			DoubleSupplier getter,
			DoubleConsumer setter
	) {
		super(0, 0, width, 20, Component.empty(), toSlider(min, max, getter.getAsDouble()));
		this.label = label;
		this.min = min;
		this.max = max;
		this.step = step;
		this.integer = integer;
		this.setter = setter;
		this.updateMessage();
	}

	@Override
	protected void updateMessage() {
		double snapped = snap(fromSlider(this.value));
		String formatted = this.integer ? Integer.toString((int) Math.round(snapped)) : trim(snapped);
		this.setMessage(this.label.copy().append(": ").append(formatted));
	}

	@Override
	protected void applyValue() {
		double snapped = snap(fromSlider(this.value));
		this.value = toSlider(this.min, this.max, snapped);
		this.setter.accept(snapped);
	}

	private double fromSlider(double slider) {
		return this.min + Mth.clamp(slider, 0.0, 1.0) * (this.max - this.min);
	}

	private double snap(double raw) {
		double snapped = Math.round(raw / this.step) * this.step;
		snapped = Mth.clamp(snapped, this.min, this.max);
		if (this.integer) {
			return Math.round(snapped);
		}
		return snapped;
	}

	private static double toSlider(double min, double max, double value) {
		if (max <= min) {
			return 0.0;
		}
		return Mth.clamp((value - min) / (max - min), 0.0, 1.0);
	}

	private static String trim(double value) {
		String text = String.format(java.util.Locale.ROOT, "%.2f", value);
		if (text.indexOf('.') >= 0) {
			text = text.replaceAll("0+$", "").replaceAll("\\.$", "");
		}
		return text;
	}
}
