package spicato_admin;

import net.fabricmc.api.ClientModInitializer;

public class SpicatoAdminClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		SpicatoAdmin.LOGGER.info("Spicato Admin client loaded");
	}
}
