package spicato_admin.pending;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import spicato_admin.knobs.AdminCatalog;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PendingPackStateTest {

	@Test
	void jsonPathsRoundTripNestedValues() {
		JsonObject root = new JsonObject();
		JsonPaths.setBoolean(root, "elites.enabled", true);
		JsonPaths.setNumber(root, "elites.spawn_chance", 0.5, false);
		assertTrue(JsonPaths.getBoolean(root, "elites.enabled", false));
		assertEquals(0.5, JsonPaths.getDouble(root, "elites.spawn_chance", 0.0), 0.0001);
	}

	@Test
	void applyWritesDiskAndReloadsWithoutTouchingOnDiscard(@TempDir Path tempDir) throws IOException {
		PendingPackState state = PendingPackState.load(tempDir);
		var spawn = AdminCatalog.knobs().stream()
				.filter(knob -> "elites.spawn_chance".equals(knob.key()))
				.findFirst()
				.orElseThrow();
		state.setNumber(spawn, 0.77);

		AtomicInteger reloads = new AtomicInteger();
		state.apply(tempDir, modId -> reloads.incrementAndGet());

		assertEquals(6, reloads.get());
		String lootJson = Files.readString(tempDir.resolve("mob_loot_rebalance.json"));
		assertTrue(lootJson.contains("0.77"));
		String sizeJson = Files.readString(tempDir.resolve("mob_size_diversity.json"));
		assertTrue(sizeJson.contains("minecraft:undead"));
		assertTrue(sizeJson.contains("\"skew\""));

		PendingPackState discarded = PendingPackState.load(tempDir);
		var fuse = AdminCatalog.knobs().stream()
				.filter(knob -> "trailDropFuseTicks".equals(knob.key()))
				.findFirst()
				.orElseThrow();
		discarded.setNumber(fuse, 12);
		PendingPackState reread = PendingPackState.load(tempDir);
		assertEquals(100.0, reread.getNumber(fuse), 0.0001);
	}

	@Test
	void applyCopiesSnapshotAndFillsMissingCatalogKeys(@TempDir Path tempDir) throws IOException {
		Files.writeString(tempDir.resolve("functional_spyglass.json"), """
				{
				  "zoom": {
				    "level_1_fov": 42.0,
				    "level_2_fov": 16.0,
				    "level_3_fov": 6.0
				  }
				}
				""");
		PendingPackState state = PendingPackState.load(tempDir);
		Path snapshot = tempDir.resolve("applied-config");
		state.apply(tempDir, snapshot, modId -> {
		});

		String instance = Files.readString(tempDir.resolve("functional_spyglass.json"));
		String copied = Files.readString(snapshot.resolve("functional_spyglass.json"));
		assertTrue(instance.contains("42.0"));
		assertTrue(instance.contains("look_curve"));
		assertTrue(instance.contains("look_speed"));
		assertEquals(instance, copied);
		assertEquals(snapshot, PendingPackState.developmentSnapshotDir(tempDir.resolve("run")));
	}

	@Test
	void resetModRestoresShippedKnobValues(@TempDir Path tempDir) {
		PendingPackState state = PendingPackState.load(tempDir);
		var spawn = AdminCatalog.knobs().stream()
				.filter(knob -> "elites.spawn_chance".equals(knob.key()))
				.findFirst()
				.orElseThrow();
		state.setNumber(spawn, 1.0);
		state.resetMod(AdminCatalog.MOB_LOOT);
		assertEquals(0.14, state.getNumber(spawn), 0.0001);
	}

	@Test
	void invalidSizeCurveDisablesApply(@TempDir Path tempDir) {
		PendingPackState state = PendingPackState.load(tempDir);
		var setA = AdminCatalog.knobs().stream()
				.filter(knob -> "presets.set_a".equals(knob.key()))
				.findFirst()
				.orElseThrow();
		assertTrue(state.playCurvesValid());
		state.setNumber(setA, "peak", 2.4);
		state.setNumber(setA, "min", 0.5);
		state.setNumber(setA, "max", 0.6);
		assertFalse(state.playCurvesValid());
	}
}
