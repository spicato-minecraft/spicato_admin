package spicato_admin.knobs;

import java.util.ArrayList;
import java.util.List;

/**
 * V1 knob list. Screens (Phase 3+) should read this table rather than inventing
 * a second layout. Later mods append a {@link AdminSection} and {@link AdminKnob}
 * rows — they do not add a UI library or a new screen class.
 *
 * <p>Each {@code modId} must already expose {@code config/<modId>.json},
 * {@code reload()}, and {@code save()} on the gameplay side.
 */
public final class AdminCatalog {

	public static final String MOB_LOOT = "mob_loot_rebalance";
	public static final String STRUCTURE = "structure_loot_rebalance";
	public static final String SIZE = "mob_size_diversity";
	public static final String TNT = "useful_tnt";
	public static final String SPYGLASS = "functional_spyglass";
	public static final String LIVING_TREES = "living_trees";

	public static final List<AdminSection> SECTIONS = List.of(
			new AdminSection(MOB_LOOT, "Mob Loot"),
			new AdminSection(STRUCTURE, "Structure Loot"),
			new AdminSection(SIZE, "Size"),
			new AdminSection(TNT, "Useful TNT"),
			new AdminSection(SPYGLASS, "Spyglass"),
			new AdminSection(LIVING_TREES, "Living Trees")
	);

	private static final List<AdminKnob> KNOBS = concat(
			AdminKnob.checkbox(MOB_LOOT, AdminPage.PLAY, "elites.enabled", "Elites"),
			AdminKnob.slider(MOB_LOOT, AdminPage.PLAY, "elites.spawn_chance", "Field spawn chance", 0.0, 1.0, 0.01),
			AdminKnob.slider(MOB_LOOT, AdminPage.PLAY, "elites.spawner_trial_spawn_chance", "Spawner / trial chance", 0.0, 1.0, 0.01),
			AdminKnob.checkbox(MOB_LOOT, AdminPage.PLAY, "elite_loot.enabled", "Elite loot"),
			AdminKnob.slider(MOB_LOOT, AdminPage.PLAY, "elite_loot.consolation_weight", "Consolation weight", 0.0, 100.0, 1.0),
			AdminKnob.slider(MOB_LOOT, AdminPage.PLAY, "elite_loot.books_weight", "Enchantment slot weight", 0.0, 100.0, 1.0),
			AdminKnob.slider(MOB_LOOT, AdminPage.PLAY, "elite_loot.potions_weight", "Potion slot weight", 0.0, 100.0, 1.0),
			AdminKnob.slider(MOB_LOOT, AdminPage.PLAY, "elite_loot.unique_weight", "Unique slot weight", 0.0, 100.0, 1.0),
			AdminKnob.checkbox(MOB_LOOT, AdminPage.PLAY, "xp.enabled", "XP uplift"),
			AdminKnob.slider(MOB_LOOT, AdminPage.PLAY, "xp.multiplier", "XP multiplier", 0.0, 5.0, 0.05),
			AdminKnob.slider(STRUCTURE, AdminPage.PLAY, "primary_expected.tier_1", "Tier 1 books / clear", 0.0, 20.0, 0.1),
			AdminKnob.slider(STRUCTURE, AdminPage.PLAY, "primary_expected.tier_2", "Tier 2 books / clear", 0.0, 20.0, 0.1),
			AdminKnob.slider(STRUCTURE, AdminPage.PLAY, "primary_expected.tier_3", "Tier 3 books / clear", 0.0, 20.0, 0.1),
			AdminKnob.slider(STRUCTURE, AdminPage.PLAY, "base_weights.efficiency", "Efficiency weight", 0.0, 20.0, 1.0),
			AdminKnob.slider(STRUCTURE, AdminPage.PLAY, "base_weights.unbreaking", "Unbreaking weight", 0.0, 20.0, 1.0),
			AdminKnob.slider(STRUCTURE, AdminPage.PLAY, "base_weights.protection", "Protection weight", 0.0, 20.0, 1.0),
			AdminKnob.slider(STRUCTURE, AdminPage.PLAY, "theme_weight_multiplier", "Theme weight multiplier", 0.5, 4.0, 0.1),
			AdminKnob.checkbox(SIZE, AdminPage.PLAY, "reach_scales_with_size", "Reach scales with size"),
			AdminKnob.curve(SIZE, AdminPage.PLAY, "presets.set_a", "Set A", 0.5, 2.5, 0.01),
			AdminKnob.curve(SIZE, AdminPage.PLAY, "presets.set_b", "Set B", 0.5, 2.5, 0.01),
			AdminKnob.curve(SIZE, AdminPage.PLAY, "presets.set_c", "Set C", 0.5, 2.5, 0.01),
			AdminKnob.checkbox(TNT, AdminPage.PLAY, "dropProtectionEnabled", "Drop protection"),
			AdminKnob.checkbox(TNT, AdminPage.PLAY, "trailDropEnabled", "Trail drop"),
			AdminKnob.slider(TNT, AdminPage.PLAY, "trailDropFuseTicks", "Trail fuse ticks", 1.0, 200.0, 1.0),
			AdminKnob.checkbox(MOB_LOOT, AdminPage.DEBUG, "elites.uat_glow", "UAT glow"),
			AdminKnob.slider(MOB_LOOT, AdminPage.DEBUG, "elites.particle_interval_ticks", "Particle interval ticks", 0.0, 100.0, 1.0),
			AdminKnob.slider(SPYGLASS, AdminPage.DEBUG, "zoom.level_1_fov", "Level 1 FOV", 1.0, 70.0, 0.5),
			AdminKnob.slider(SPYGLASS, AdminPage.DEBUG, "zoom.level_2_fov", "Level 2 FOV", 1.0, 70.0, 0.5),
			AdminKnob.slider(SPYGLASS, AdminPage.DEBUG, "zoom.level_3_fov", "Level 3 FOV", 1.0, 70.0, 0.5),
			AdminKnob.slider(SPYGLASS, AdminPage.DEBUG, "zoom.look_curve", "Look curve", 0.0, 1.0, 0.05),
			AdminKnob.slider(SPYGLASS, AdminPage.DEBUG, "zoom.look_speed", "Look speed", 0.25, 2.0, 0.05)
	);

	private AdminCatalog() {
	}

	private static List<AdminKnob> concat(AdminKnob... head) {
		List<AdminKnob> knobs = new ArrayList<>(List.of(head));
		knobs.addAll(LivingTreesKnobs.all());
		return List.copyOf(knobs);
	}

	public static List<AdminKnob> knobs() {
		return KNOBS;
	}

	public static List<AdminKnob> knobs(AdminPage page) {
		return KNOBS.stream().filter(knob -> knob.page() == page).toList();
	}

	public static List<AdminKnob> knobs(AdminPage page, String modId) {
		return KNOBS.stream()
				.filter(knob -> knob.page() == page && knob.modId().equals(modId))
				.toList();
	}
}
