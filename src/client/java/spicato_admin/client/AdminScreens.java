package spicato_admin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public final class AdminScreens {
	private AdminScreens() {
	}

	public static boolean isLocalSingleplayer(Minecraft client) {
		return client != null && client.hasSingleplayerServer();
	}

	public static void openPlay(Minecraft client, Screen lastScreen) {
		if (!isLocalSingleplayer(client)) {
			return;
		}
		if (client.gui.screen() instanceof PlayAdminScreen) {
			return;
		}
		client.gui.setScreen(new PlayAdminScreen(lastScreen));
	}
}
