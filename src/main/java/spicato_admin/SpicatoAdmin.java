package spicato_admin;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpicatoAdmin implements ModInitializer {
	public static final String MOD_ID = "spicato_admin";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Spicato Admin loaded; screens register on the client");
	}
}
