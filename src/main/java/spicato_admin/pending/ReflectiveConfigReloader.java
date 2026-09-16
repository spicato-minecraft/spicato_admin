package spicato_admin.pending;

import net.fabricmc.loader.api.FabricLoader;
import spicato_admin.SpicatoAdmin;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Invokes each gameplay mod's {@code reload()} without a compile dependency.
 */
public final class ReflectiveConfigReloader implements ConfigReloader {
	private static final Map<String, String> RELOAD_CLASSES = Map.of(
			"mob_loot_rebalance", "mob_loot_rebalance.config.ModConfig",
			"structure_loot_rebalance", "structure_loot_rebalance.config.PackConfig",
			"mob_size_diversity", "mob_size_diversity.config.SizeConfig",
			"useful_tnt", "useful_tnt.UsefulTntConfig",
			"functional_spyglass", "functional_spyglass.config.SpyglassConfig",
			"living_trees", "living_trees.config.LivingTreesConfig"
	);

	@Override
	public void reload(String modId) {
		if (!FabricLoader.getInstance().isModLoaded(modId)) {
			return;
		}
		String className = RELOAD_CLASSES.get(modId);
		if (className == null) {
			SpicatoAdmin.LOGGER.warn("No reload class registered for {}", modId);
			return;
		}
		try {
			Method reload = Class.forName(className).getMethod("reload");
			reload.invoke(null);
		} catch (ReflectiveOperationException e) {
			SpicatoAdmin.LOGGER.error("Failed to reload {}", modId, e);
		}
	}
}
