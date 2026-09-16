package spicato_admin.knobs;

/**
 * One admin-facing field. {@link #key()} is a JSON path under that mod's config
 * file (dot-separated). {@link #min()}, {@link #max()}, and {@link #step()} are
 * required for {@link AdminWidget#SLIDER}; for {@link AdminWidget#CURVE} they
 * bound the peak/min/max sliders. Unused bounds are {@code null}.
 */
public record AdminKnob(
		String modId,
		AdminPage page,
		String key,
		String label,
		AdminWidget widget,
		Double min,
		Double max,
		Double step
) {
	public static AdminKnob checkbox(String modId, AdminPage page, String key, String label) {
		return new AdminKnob(modId, page, key, label, AdminWidget.CHECKBOX, null, null, null);
	}

	public static AdminKnob slider(
			String modId,
			AdminPage page,
			String key,
			String label,
			double min,
			double max,
			double step
	) {
		return new AdminKnob(modId, page, key, label, AdminWidget.SLIDER, min, max, step);
	}

	public static AdminKnob curve(
			String modId,
			AdminPage page,
			String key,
			String label,
			double min,
			double max,
			double step
	) {
		return new AdminKnob(modId, page, key, label, AdminWidget.CURVE, min, max, step);
	}
}
