package spicato_admin.pending;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public final class JsonPaths {
	private JsonPaths() {
	}

	public static JsonElement get(JsonObject root, String path) {
		if (root == null || path == null || path.isBlank()) {
			return null;
		}
		String[] parts = path.split("\\.");
		JsonElement current = root;
		for (String part : parts) {
			if (current == null || !current.isJsonObject() || !current.getAsJsonObject().has(part)) {
				return null;
			}
			current = current.getAsJsonObject().get(part);
		}
		return current;
	}

	public static void set(JsonObject root, String path, JsonElement value) {
		String[] parts = path.split("\\.");
		JsonObject cursor = root;
		for (int i = 0; i < parts.length - 1; i++) {
			JsonElement next = cursor.get(parts[i]);
			if (next == null || !next.isJsonObject()) {
				JsonObject created = new JsonObject();
				cursor.add(parts[i], created);
				cursor = created;
			} else {
				cursor = next.getAsJsonObject();
			}
		}
		cursor.add(parts[parts.length - 1], value);
	}

	public static boolean getBoolean(JsonObject root, String path, boolean fallback) {
		JsonElement element = get(root, path);
		return element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean()
				? element.getAsBoolean()
				: fallback;
	}

	public static double getDouble(JsonObject root, String path, double fallback) {
		JsonElement element = get(root, path);
		return element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()
				? element.getAsDouble()
				: fallback;
	}

	public static void setBoolean(JsonObject root, String path, boolean value) {
		set(root, path, new JsonPrimitive(value));
	}

	public static void setNumber(JsonObject root, String path, double value, boolean integer) {
		if (integer) {
			set(root, path, new JsonPrimitive((int) Math.round(value)));
		} else {
			set(root, path, new JsonPrimitive(value));
		}
	}
}
