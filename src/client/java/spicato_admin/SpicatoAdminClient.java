package spicato_admin;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import spicato_admin.client.AdminScreens;

public class SpicatoAdminClient implements ClientModInitializer {
	private static KeyMapping openPlay;

	@Override
	public void onInitializeClient() {
		openPlay = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.spicato_admin.open_play",
				GLFW.GLFW_KEY_P,
				KeyMapping.Category.MISC
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openPlay.consumeClick()) {
				AdminScreens.openPlay(client, client.gui.screen());
			}
		});

		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (!(screen instanceof PauseScreen pause) || !pause.showsPauseMenu()) {
				return;
			}
			if (!AdminScreens.isLocalSingleplayer(client)) {
				return;
			}
			int width = 204;
			Screens.getWidgets(screen).add(Button.builder(
					Component.translatable("spicato_admin.button.pack_admin"),
					button -> AdminScreens.openPlay(client, screen)
			).bounds((scaledWidth - width) / 2, scaledHeight - 28, width, 20).build());
		});

		SpicatoAdmin.LOGGER.info("Spicato Admin client loaded");
	}
}
