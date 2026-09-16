package spicato_admin.pending;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import spicato_admin.knobs.AdminCatalog;

final class ShippedDefaults {
	private ShippedDefaults() {
	}

	static JsonObject forMod(String modId) {
		return switch (modId) {
			case AdminCatalog.MOB_LOOT -> mobLoot();
			case AdminCatalog.STRUCTURE -> structure();
			case AdminCatalog.SIZE -> size();
			case AdminCatalog.TNT -> tnt();
			case AdminCatalog.SPYGLASS -> spyglass();
			case AdminCatalog.LIVING_TREES -> livingTrees();
			default -> new JsonObject();
		};
	}

	private static JsonObject mobLoot() {
		JsonObject root = new JsonObject();
		JsonObject xp = new JsonObject();
		xp.addProperty("enabled", false);
		xp.addProperty("multiplier", 1.35);
		root.add("xp", xp);
		JsonObject elites = new JsonObject();
		elites.addProperty("enabled", true);
		elites.addProperty("spawn_chance", 0.14);
		elites.addProperty("spawner_trial_spawn_chance", 0.02);
		elites.addProperty("ambient_particle", "minecraft:dust");
		elites.addProperty("particle_dust_color", 0xFFE8EEF8);
		elites.addProperty("particle_dust_scale", 0.5);
		elites.addProperty("particle_interval_ticks", 60);
		elites.addProperty("uat_glow", false);
		root.add("elites", elites);
		JsonObject eliteLoot = new JsonObject();
		eliteLoot.addProperty("enabled", true);
		eliteLoot.addProperty("consolation_weight", 15);
		eliteLoot.addProperty("books_weight", 23);
		eliteLoot.addProperty("potions_weight", 40);
		eliteLoot.addProperty("unique_weight", 22);
		root.add("elite_loot", eliteLoot);
		JsonObject killMethod = new JsonObject();
		killMethod.addProperty("lookback_ticks", 100);
		root.add("kill_method", killMethod);
		JsonObject combatPotion = new JsonObject();
		combatPotion.addProperty("allow_invisibility", false);
		root.add("combat_potion", combatPotion);
		JsonObject feedback = new JsonObject();
		feedback.addProperty("elite_kill_sound", "minecraft:block.amethyst_block.chime");
		root.add("feedback", feedback);
		return root;
	}

	private static JsonObject structure() {
		JsonObject root = new JsonObject();
		JsonObject expected = new JsonObject();
		expected.addProperty("tier_1", 2.5);
		expected.addProperty("tier_2", 20.0 / 3.0);
		expected.addProperty("tier_3", 10.0);
		root.add("primary_expected", expected);
		JsonObject weights = new JsonObject();
		weights.addProperty("efficiency", 4);
		weights.addProperty("unbreaking", 3);
		weights.addProperty("protection", 3);
		root.add("base_weights", weights);
		root.addProperty("theme_weight_multiplier", 2.0);
		return root;
	}

	private static JsonObject size() {
		JsonObject root = new JsonObject();
		root.addProperty("reach_scales_with_size", true);
		JsonObject presets = new JsonObject();
		presets.add("set_a", curve(1.1, -0.5, 0.95, 1.25, 0.08));
		presets.add("set_b", curve(1.4, -0.2, 1.35, 1.55, 0.08));
		presets.add("set_c", curve(1.1, 0.0, 0.95, 1.15, 0.08));
		root.add("presets", presets);
		root.add("rules", sizeRules());
		return root;
	}

	private static JsonArray sizeRules() {
		JsonArray rules = new JsonArray();
		rules.add(tagRule("mob_loot_rebalance:elite", "set_b"));
		rules.add(tagRule("minecraft:undead", "set_a"));
		rules.add(tagRule("minecraft:raiders", "set_a"));
		rules.add(tagRule("minecraft:illager", "set_a"));
		rules.add(tagRule("mob_size_diversity:set_a", "set_a"));
		JsonObject namespace = new JsonObject();
		namespace.addProperty("namespace", "minecraft");
		namespace.addProperty("preset", "set_c");
		rules.add(namespace);
		return rules;
	}

	private static JsonObject tagRule(String tag, String preset) {
		JsonObject rule = new JsonObject();
		rule.addProperty("tag", tag);
		rule.addProperty("preset", preset);
		return rule;
	}

	private static JsonObject curve(double peak, double skew, double min, double max, double centerQ) {
		JsonObject json = new JsonObject();
		json.addProperty("peak", peak);
		json.addProperty("skew", skew);
		json.addProperty("min", min);
		json.addProperty("max", max);
		json.addProperty("center_q", centerQ);
		json.addProperty("min_bound_q", 0.0);
		json.addProperty("max_bound_q", 0.0);
		return json;
	}

	private static JsonObject tnt() {
		JsonObject root = new JsonObject();
		root.addProperty("cheaperTntEnabled", true);
		root.addProperty("dropProtectionEnabled", true);
		root.addProperty("trailDropEnabled", true);
		root.addProperty("trailDropFuseTicks", 100);
		return root;
	}

	private static JsonObject spyglass() {
		JsonObject root = new JsonObject();
		JsonObject zoom = new JsonObject();
		zoom.addProperty("level_1_fov", 50.0);
		zoom.addProperty("level_2_fov", 16.0);
		zoom.addProperty("level_3_fov", 6.0);
		zoom.addProperty("look_curve", 0.5);
		zoom.addProperty("look_speed", 1.0);
		root.add("zoom", zoom);
		return root;
	}

	private static JsonObject livingTrees() {
		JsonObject root = new JsonObject();
		root.addProperty("writes_per_tick", 1);
		root.addProperty("growth_radius_chunks", 8);
		JsonObject species = new JsonObject();
		species.add("oak", wood("minecraft:oak_log", "minecraft:oak_leaves", 0.92, 0.04, 0.12, -1, 1, 2, 4, 4, 12, 0.16, 0.15, 0.88, 0.12, 0.86, 0.7, 12, 0.35, 0.42, 0, 2, 1, 8, 24));
		species.add("spruce", wood("minecraft:spruce_log", "minecraft:spruce_leaves", 0.98, 0.04, 0.4, -1, 1, 2, 4, 3, 2, 0.15, 0.0, 0.9, 0.2, 0.85, 0.9, 28, 0.35, 0.82, 2, 1, 1, 10, 40));
		species.add("birch", wood("minecraft:birch_log", "minecraft:birch_leaves", 0.94, 0.04, 0.1, -1, 1, 2, 4, 4, 10, 0.16, 0.15, 0.88, 0.12, 0.86, 0.7, 12, 0.35, 0.42, 0, 2, 1, 8, 24));
		species.add("jungle", wood("minecraft:jungle_log", "minecraft:jungle_leaves", 0.92, 0.04, 0.12, -1, 1, 2, 4, 4, 16, 0.16, 0.15, 0.88, 0.12, 0.86, 0.7, 26, 0.18, 0.4, 0, 2, 1, 8, 32));
		species.add("acacia", wood("minecraft:acacia_log", "minecraft:acacia_leaves", 0.92, 0.04, 0.12, 4, 1, 2, 4, 4, 8, 0.7, 0.15, 0.45, 0.55, 0.5, 0.35, 12, 0.35, 0.45, 1, 3, 1, 8, 24));
		species.add("dark_oak", wood("minecraft:dark_oak_log", "minecraft:dark_oak_leaves", 0.92, 0.04, 0.12, -1, 1, 2, 2, 4, 12, 0.16, 0.15, 0.88, 0.12, 0.86, 0.7, 12, 0.35, 0.5, 0, 2, 2, 8, 24));
		species.add("mangrove", wood("minecraft:mangrove_log", "minecraft:mangrove_leaves", 0.92, 0.04, 0.12, -1, 1, 2, 4, 4, 12, 0.16, 0.15, 0.88, 0.12, 0.86, 0.7, 12, 0.35, 0.42, 0, 2, 1, 8, 24));
		species.add("cherry", wood("minecraft:cherry_log", "minecraft:cherry_leaves", 0.88, 0.04, 0.08, 3, 2, 2, 4, 4, 6, 0.85, 0.2, 0.4, 0.7, 0.5, 0.28, 12, 0.85, 0.42, 1, 3, 1, 6, 20));
		species.add("pale_oak", wood("minecraft:pale_oak_log", "minecraft:pale_oak_leaves", 0.92, 0.04, 0.12, -1, 1, 2, 4, 4, 12, 0.16, 0.15, 0.88, 0.12, 0.86, 0.7, 12, 0.35, 0.42, 0, 2, 1, 8, 24));
		root.add("species", species);
		return root;
	}

	private static JsonObject wood(
			String log,
			String leaves,
			double persist,
			double persistDecay,
			double forkChance,
			int forkForcedAt,
			int forkChildren,
			int maxOrder,
			int maxMeristems,
			int branchAfter,
			int maxInternodes,
			double divergence,
			double forkPitch,
			double tropismUp,
			double tropismOut,
			double dominance,
			double branchCurve,
			int heightMax,
			double nodalRelease,
			double canopySpan,
			int foliagePolicy,
			int leafRadius,
			int trunkSection,
			int matureCycles,
			int maxCycles
	) {
		JsonObject json = new JsonObject();
		json.addProperty("apical_persist", persist);
		json.addProperty("persist_decay", persistDecay);
		json.addProperty("fork_chance", forkChance);
		json.addProperty("fork_forced_at", forkForcedAt);
		json.addProperty("fork_children", forkChildren);
		json.addProperty("max_order", maxOrder);
		json.addProperty("max_meristems", maxMeristems);
		json.addProperty("branch_after", branchAfter);
		json.addProperty("max_internodes", maxInternodes);
		json.addProperty("divergence", divergence);
		json.addProperty("fork_pitch", forkPitch);
		json.addProperty("tropism_up", tropismUp);
		json.addProperty("tropism_out", tropismOut);
		json.addProperty("dominance", dominance);
		json.addProperty("branch_curve", branchCurve);
		json.addProperty("height_max", heightMax);
		json.addProperty("nodal_release", nodalRelease);
		json.addProperty("canopy_span", canopySpan);
		json.addProperty("foliage_policy", foliagePolicy);
		json.addProperty("leaf_radius", leafRadius);
		json.addProperty("trunk_section", trunkSection);
		json.addProperty("mature_cycles", matureCycles);
		json.addProperty("max_cycles", maxCycles);
		json.addProperty("log_block", log);
		json.addProperty("leaf_block", leaves);
		return json;
	}
}
