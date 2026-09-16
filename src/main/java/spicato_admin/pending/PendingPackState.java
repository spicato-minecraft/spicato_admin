package spicato_admin.pending;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import spicato_admin.knobs.AdminCatalog;
import spicato_admin.knobs.AdminKnob;
import spicato_admin.knobs.AdminPage;
import spicato_admin.knobs.AdminWidget;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * In-memory pending copy of pack config JSON. Widgets mutate this; Apply writes
 * disk and reloads. Closing without Apply leaves files and live config alone.
 */
public final class PendingPackState {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

	private final Map<String, JsonObject> pending = new LinkedHashMap<>();
	private Runnable onChange = () -> {
	};

	public static PendingPackState load(Path configDir) {
		PendingPackState state = new PendingPackState();
		for (var section : AdminCatalog.SECTIONS) {
			state.pending.put(section.modId(), readOrShipped(configDir, section.modId()));
		}
		return state;
	}

	private static JsonObject readOrShipped(Path configDir, String modId) {
		Path path = configDir.resolve(modId + ".json");
		if (!Files.isRegularFile(path)) {
			return deepCopy(ShippedDefaults.forMod(modId));
		}
		try (Reader reader = Files.newBufferedReader(path)) {
			JsonElement parsed = JsonParser.parseReader(reader);
			if (parsed != null && parsed.isJsonObject()) {
				return parsed.getAsJsonObject();
			}
		} catch (IOException | RuntimeException ignored) {
		}
		return deepCopy(ShippedDefaults.forMod(modId));
	}

	public void setOnChange(Runnable onChange) {
		this.onChange = onChange == null ? () -> {
		} : onChange;
	}

	public JsonObject json(String modId) {
		return pending.computeIfAbsent(modId, ShippedDefaults::forMod);
	}

	public boolean getBoolean(AdminKnob knob) {
		return JsonPaths.getBoolean(json(knob.modId()), knob.key(), shippedBoolean(knob));
	}

	public void setBoolean(AdminKnob knob, boolean value) {
		JsonPaths.setBoolean(json(knob.modId()), knob.key(), value);
		onChange.run();
	}

	public double getNumber(AdminKnob knob) {
		return JsonPaths.getDouble(json(knob.modId()), knob.key(), shippedNumber(knob));
	}

	public double getNumber(AdminKnob knob, String subKey) {
		return JsonPaths.getDouble(json(knob.modId()), knob.key() + "." + subKey, shippedNumber(knob, subKey));
	}

	public void setNumber(AdminKnob knob, double value) {
		JsonPaths.setNumber(json(knob.modId()), knob.key(), value, isIntegerKnob(knob));
		onChange.run();
	}

	public void setNumber(AdminKnob knob, String subKey, double value) {
		JsonPaths.setNumber(json(knob.modId()), knob.key() + "." + subKey, value, false);
		onChange.run();
	}

	public void resetMod(String modId) {
		JsonObject shipped = ShippedDefaults.forMod(modId);
		JsonObject current = json(modId);
		for (AdminKnob knob : AdminCatalog.knobs(AdminPage.PLAY, modId)) {
			if (knob.widget() == AdminWidget.CURVE) {
				copyIfPresent(shipped, current, knob.key() + ".peak");
				copyIfPresent(shipped, current, knob.key() + ".min");
				copyIfPresent(shipped, current, knob.key() + ".max");
			} else {
				copyIfPresent(shipped, current, knob.key());
			}
		}
		onChange.run();
	}

	public boolean playCurvesValid() {
		for (AdminKnob knob : AdminCatalog.knobs(AdminPage.PLAY)) {
			if (knob.widget() != AdminWidget.CURVE) {
				continue;
			}
			double min = getNumber(knob, "min");
			double max = getNumber(knob, "max");
			double peak = getNumber(knob, "peak");
			if (!(min < max) || peak < min || peak > max) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Loom {@code run/} sits under the Gradle project. Snapshot JSON lives beside
	 * that folder so wiping {@code run/} does not drop a UAT pass.
	 */
	public static Path developmentSnapshotDir(Path gameDir) {
		if (gameDir == null || gameDir.getParent() == null) {
			return null;
		}
		return gameDir.getParent().resolve("applied-config");
	}

	public void apply(Path configDir, ConfigReloader reloader) throws IOException {
		apply(configDir, null, reloader);
	}

	public void apply(Path configDir, Path snapshotDir, ConfigReloader reloader) throws IOException {
		if (!playCurvesValid()) {
			return;
		}
		materializeCatalogKeys();
		for (var section : AdminCatalog.SECTIONS) {
			writeJson(configDir.resolve(section.modId() + ".json"), json(section.modId()));
			if (snapshotDir != null) {
				writeJson(snapshotDir.resolve(section.modId() + ".json"), json(section.modId()));
			}
			reloader.reload(section.modId());
		}
	}

	private void materializeCatalogKeys() {
		for (AdminKnob knob : AdminCatalog.knobs()) {
			JsonObject json = json(knob.modId());
			switch (knob.widget()) {
				case CHECKBOX -> JsonPaths.setBoolean(json, knob.key(), getBoolean(knob));
				case SLIDER -> JsonPaths.setNumber(json, knob.key(), getNumber(knob), isIntegerKnob(knob));
				case CURVE -> {
					JsonPaths.setNumber(json, knob.key() + ".peak", getNumber(knob, "peak"), false);
					JsonPaths.setNumber(json, knob.key() + ".min", getNumber(knob, "min"), false);
					JsonPaths.setNumber(json, knob.key() + ".max", getNumber(knob, "max"), false);
				}
			}
		}
	}

	private static void writeJson(Path path, JsonObject json) throws IOException {
		Files.createDirectories(path.getParent());
		try (Writer writer = Files.newBufferedWriter(path)) {
			GSON.toJson(json, writer);
		}
	}

	private static void copyIfPresent(JsonObject from, JsonObject to, String path) {
		JsonElement value = JsonPaths.get(from, path);
		if (value != null) {
			JsonPaths.set(to, path, value.deepCopy());
		}
	}

	private static JsonObject deepCopy(JsonObject source) {
		return source.deepCopy();
	}

	private static boolean isIntegerKnob(AdminKnob knob) {
		return knob.step() != null && knob.step() >= 1.0 - 1.0e-9;
	}

	private static boolean shippedBoolean(AdminKnob knob) {
		return JsonPaths.getBoolean(ShippedDefaults.forMod(knob.modId()), knob.key(), false);
	}

	private static double shippedNumber(AdminKnob knob) {
		return JsonPaths.getDouble(ShippedDefaults.forMod(knob.modId()), knob.key(), 0.0);
	}

	private static double shippedNumber(AdminKnob knob, String subKey) {
		return JsonPaths.getDouble(ShippedDefaults.forMod(knob.modId()), knob.key() + "." + subKey, 0.0);
	}
}
