package spicato_admin.knobs;

import java.util.ArrayList;
import java.util.List;

final class LivingTreesKnobs {
	static final String[] SPECIES = {
			"oak", "spruce", "birch", "jungle", "acacia", "dark_oak", "mangrove", "cherry", "pale_oak"
	};

	private LivingTreesKnobs() {
	}

	static List<AdminKnob> all() {
		List<AdminKnob> knobs = new ArrayList<>();
		String mod = AdminCatalog.LIVING_TREES;
		knobs.add(AdminKnob.slider(mod, AdminPage.DEBUG, "writes_per_tick", "Writes per tick", 1.0, 5.0, 1.0));
		knobs.add(AdminKnob.slider(mod, AdminPage.DEBUG, "growth_radius_chunks", "Growth radius (chunks)", 1.0, 16.0, 1.0));
		for (String species : SPECIES) {
			String prefix = "species." + species + ".";
			String label = label(species);
			knobs.add(AdminKnob.slider(mod, AdminPage.PLAY, prefix + "apical_persist", label + " persist", 0.0, 1.0, 0.01));
			knobs.add(AdminKnob.slider(mod, AdminPage.PLAY, prefix + "persist_decay", label + " persist decay", 0.0, 1.0, 0.01));
			knobs.add(AdminKnob.slider(mod, AdminPage.PLAY, prefix + "fork_chance", label + " fork chance", 0.0, 1.0, 0.01));
			knobs.add(AdminKnob.slider(mod, AdminPage.PLAY, prefix + "fork_forced_at", label + " forced fork at", -1.0, 16.0, 1.0));
			knobs.add(AdminKnob.slider(mod, AdminPage.PLAY, prefix + "fork_children", label + " fork children", 1.0, 2.0, 1.0));
			knobs.add(AdminKnob.slider(mod, AdminPage.PLAY, prefix + "max_order", label + " max order", 0.0, 4.0, 1.0));
			knobs.add(AdminKnob.slider(mod, AdminPage.PLAY, prefix + "max_meristems", label + " max meristems", 1.0, 8.0, 1.0));
			knobs.add(AdminKnob.slider(mod, AdminPage.PLAY, prefix + "branch_after", label + " branch after", 0.0, 16.0, 1.0));
			knobs.add(AdminKnob.slider(mod, AdminPage.PLAY, prefix + "max_internodes", label + " max internodes", 1.0, 24.0, 1.0));
			knobs.add(AdminKnob.slider(mod, AdminPage.PLAY, prefix + "divergence", label + " divergence", 0.0, 1.0, 0.01));
			knobs.add(AdminKnob.slider(mod, AdminPage.PLAY, prefix + "fork_pitch", label + " fork pitch", -1.0, 1.0, 0.05));
			knobs.add(AdminKnob.slider(mod, AdminPage.PLAY, prefix + "tropism_up", label + " tropism up", 0.0, 1.0, 0.01));
			knobs.add(AdminKnob.slider(mod, AdminPage.PLAY, prefix + "tropism_out", label + " tropism out", 0.0, 1.0, 0.01));
			knobs.add(AdminKnob.slider(mod, AdminPage.PLAY, prefix + "branch_curve", label + " branch curve", 0.05, 2.0, 0.05));
			knobs.add(AdminKnob.slider(mod, AdminPage.PLAY, prefix + "height_max", label + " height max", 4.0, 64.0, 1.0));
			knobs.add(AdminKnob.slider(mod, AdminPage.PLAY, prefix + "nodal_release", label + " nodal release", 0.0, 1.0, 0.01));
			knobs.add(AdminKnob.slider(mod, AdminPage.PLAY, prefix + "canopy_span", label + " canopy span", 0.2, 1.0, 0.01));
			knobs.add(AdminKnob.slider(mod, AdminPage.PLAY, prefix + "dominance", label + " dominance", 0.0, 1.0, 0.01));
			knobs.add(AdminKnob.slider(mod, AdminPage.PLAY, prefix + "foliage_policy", label + " foliage policy", 0.0, 2.0, 1.0));
			knobs.add(AdminKnob.slider(mod, AdminPage.PLAY, prefix + "leaf_radius", label + " leaf radius", 1.0, 6.0, 1.0));
			knobs.add(AdminKnob.slider(mod, AdminPage.PLAY, prefix + "trunk_section", label + " trunk section", 1.0, 2.0, 1.0));
			knobs.add(AdminKnob.slider(mod, AdminPage.PLAY, prefix + "mature_cycles", label + " mature cycles", 0.0, 40.0, 1.0));
			knobs.add(AdminKnob.slider(mod, AdminPage.PLAY, prefix + "max_cycles", label + " max cycles", 1.0, 80.0, 1.0));
		}
		return knobs;
	}

	private static String label(String species) {
		return switch (species) {
			case "dark_oak" -> "Dark oak";
			case "pale_oak" -> "Pale oak";
			default -> Character.toUpperCase(species.charAt(0)) + species.substring(1);
		};
	}
}
