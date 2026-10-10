package spicato_admin.knobs;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminCatalogTest {

	@Test
	void playAndDebugStaySegregatedByMod() {
		assertEquals(
				Set.of(
						"mob_loot_rebalance",
						"structure_loot_rebalance",
						"mob_size_diversity",
						"useful_tnt",
						"functional_spyglass",
						"living_trees"
				),
				AdminCatalog.SECTIONS.stream().map(AdminSection::modId).collect(Collectors.toSet())
		);
		assertEquals(6, AdminCatalog.SECTIONS.size());

		Set<String> playKeys = keys(AdminPage.PLAY);
		assertTrue(playKeys.contains("elites.spawn_chance"));
		assertTrue(playKeys.contains("elite_loot.consolation_weight"));
		assertTrue(playKeys.contains("primary_expected.tier_1"));
		assertTrue(playKeys.contains("base_weights.efficiency"));
		assertTrue(playKeys.contains("presets.set_a"));
		assertTrue(playKeys.contains("species.oak.apical_persist"));
		assertTrue(playKeys.contains("species.cherry.divergence"));
		assertTrue(playKeys.contains("zoom.look_speed"));
		assertTrue(playKeys.contains("zoom.look_min"));
		assertTrue(playKeys.contains("zoom.look_max"));
		assertTrue(playKeys.stream().noneMatch(key -> key.contains("uat_glow")));

		Set<String> debugKeys = keys(AdminPage.DEBUG);
		assertEquals(
				Set.of(
						"elites.uat_glow",
						"elites.particle_interval_ticks",
						"zoom.level_1_fov",
						"zoom.level_2_fov",
						"zoom.level_3_fov",
						"writes_per_tick",
						"growth_radius_chunks"
				),
				debugKeys
		);
		assertTrue(AdminCatalog.knobs(AdminPage.DEBUG, AdminCatalog.SIZE).isEmpty());
		assertTrue(AdminCatalog.knobs(AdminPage.DEBUG, AdminCatalog.TNT).isEmpty());
		assertEquals(4, AdminCatalog.knobs(AdminPage.PLAY, AdminCatalog.SPYGLASS).size());
		assertEquals(3, AdminCatalog.knobs(AdminPage.DEBUG, AdminCatalog.SPYGLASS).size());
	}

	@Test
	void visibleSectionsRequireLoadedModAndKnobsOnThatPage() {
		var pack = java.util.Set.of(
				"mob_loot_rebalance",
				"structure_loot_rebalance",
				"mob_size_diversity",
				"useful_tnt",
				"functional_spyglass"
		);
		assertEquals(
				java.util.List.of(
						AdminCatalog.MOB_LOOT,
						AdminCatalog.STRUCTURE,
						AdminCatalog.SIZE,
						AdminCatalog.TNT,
						AdminCatalog.SPYGLASS
				),
				AdminCatalog.visibleSections(AdminPage.PLAY, pack::contains).stream().map(AdminSection::modId).toList()
		);
		assertEquals(
				java.util.List.of(AdminCatalog.MOB_LOOT, AdminCatalog.SPYGLASS),
				AdminCatalog.visibleSections(AdminPage.DEBUG, pack::contains).stream().map(AdminSection::modId).toList()
		);
	}

	@Test
	void slidersAndCurvesDeclareBounds() {
		for (AdminKnob knob : AdminCatalog.knobs()) {
			if (knob.widget() == AdminWidget.CHECKBOX) {
				continue;
			}
			assertTrue(knob.min() != null && knob.max() != null && knob.step() != null, knob.key());
			assertTrue(knob.min() < knob.max(), knob.key());
			assertTrue(knob.step() > 0.0, knob.key());
		}
	}

	private static Set<String> keys(AdminPage page) {
		return AdminCatalog.knobs(page).stream().map(AdminKnob::key).collect(Collectors.toSet());
	}
}
