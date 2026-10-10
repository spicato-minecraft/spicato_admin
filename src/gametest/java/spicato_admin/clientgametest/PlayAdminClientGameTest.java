package spicato_admin.clientgametest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.GameType;
import org.lwjgl.glfw.GLFW;
import spicato_admin.client.PlayAdminScreen;
import spicato_admin.knobs.AdminCatalog;
import spicato_admin.knobs.AdminKnob;
import spicato_admin.pending.PendingPackState;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * Unpublished local worlds can open Pack Admin. A world opened to LAN cannot.
 * Closing the screen drops edits; Apply writes the config and tells the player.
 */
public class PlayAdminClientGameTest implements FabricClientGameTest {
	private static final String PACK_ADMIN = "spicato_admin.button.pack_admin";
	private static final String ELITES = "elites.enabled";
	private static final String OPEN_PLAY = "key.spicato_admin.open_play";

	@Override
	public void runTest(ClientGameTestContext context) {
		List<String> failures = new ArrayList<>();
		runOne(context, failures, "pauseButtonOpensPlayAdmin", () -> pauseButtonOpensPlayAdmin(context));
		runOne(context, failures, "pKeyOpensAdmin", () -> pKeyOpensAdmin(context));
		runOne(context, failures, "publishedServerHidesAdmin", () -> publishedServerHidesAdmin(context));
		runOne(context, failures, "applyWritesAndDiscardDoesNot", () -> applyWritesAndDiscardDoesNot(context));
		if (!failures.isEmpty()) {
			throw new AssertionError(String.join("\n", failures));
		}
	}

	private void pauseButtonOpensPlayAdmin(ClientGameTestContext context) {
		try (var world = context.worldBuilder().create()) {
			world.getClientLevel().waitForChunksDownload();
			assertUnpublishedLocalWorld(context);
			openPause(context);
			if (!pauseHasPackAdmin(context)) {
				throw new AssertionError("Pause is missing the Pack Admin button. Widgets: " + widgetText(context));
			}
			context.clickScreenButton(PACK_ADMIN);
			context.waitForScreen(PlayAdminScreen.class);
		}
	}

	private void pKeyOpensAdmin(ClientGameTestContext context) {
		try (var world = context.worldBuilder().create()) {
			world.getClientLevel().waitForChunksDownload();
			assertUnpublishedLocalWorld(context);
			context.runOnClient(client -> {
				if (client.screen != null) {
					throw new AssertionError("Expected no screen before pressing P, found " + client.screen.getClass().getName());
				}
				KeyMapping mapping = KeyMapping.get(OPEN_PLAY);
				if (mapping == null) {
					throw new AssertionError("Pack Admin key is not registered");
				}
				if (mapping.getDefaultKey().getValue() != GLFW.GLFW_KEY_P) {
					throw new AssertionError("Pack Admin default key is not P");
				}
			});
			context.getInput().pressKey(GLFW.GLFW_KEY_P);
			context.waitForScreen(PlayAdminScreen.class);
		}
	}

	private void publishedServerHidesAdmin(ClientGameTestContext context) throws IOException {
		try (var world = context.worldBuilder().create()) {
			world.getClientLevel().waitForChunksDownload();
			publishToLan(context);
			openPause(context);
			if (pauseHasPackAdmin(context)) {
				throw new AssertionError("Published server still shows Pack Admin. Widgets: " + widgetText(context));
			}
			context.setScreen(() -> null);
			context.waitFor(client -> client.screen == null);
			context.getInput().pressKey(GLFW.GLFW_KEY_P);
			context.waitTicks(5);
			context.runOnClient(client -> {
				if (client.screen instanceof PlayAdminScreen) {
					throw new AssertionError("P opened Pack Admin on a server published to LAN");
				}
			});
		}
	}

	private void applyWritesAndDiscardDoesNot(ClientGameTestContext context) throws IOException {
		try (var world = context.worldBuilder().create()) {
			world.getClientLevel().waitForChunksDownload();
			Path configDir = FabricLoader.getInstance().getConfigDir();
			Path lootFile = configDir.resolve("mob_loot_rebalance.json");
			AdminKnob elites = elitesKnob();
			boolean before = PendingPackState.load(configDir).getBoolean(elites);
			Map<String, byte[]> snapshot = snapshotConfig(configDir);

			openAdmin(context);
			pressCycle(context, "Elites");
			context.clickScreenButton("gui.done");
			context.waitFor(client -> !(client.screen instanceof PlayAdminScreen));
			assertConfigUnchanged(snapshot, snapshotConfig(configDir));

			openAdmin(context);
			pressCycle(context, "Elites");
			context.clickScreenButton("spicato_admin.button.apply");
			context.waitTick();

			boolean after = readElitesEnabled(lootFile);
			if (after == before) {
				throw new AssertionError("Apply left elites.enabled at " + after + " in " + lootFile);
			}
			if (PendingPackState.load(configDir).getBoolean(elites) != after) {
				throw new AssertionError("Reloaded config does not match the file written by Apply");
			}
			String expected = savedMessage(configDir);
			String chat = context.computeOnClient(PlayAdminClientGameTest::chatText);
			if (!chat.contains(expected)) {
				throw new AssertionError("Player did not see the saved message.\nExpected: " + expected + "\nChat:\n" + chat);
			}
		}
	}

	private static void runOne(ClientGameTestContext context, List<String> failures, String name, ClientCheck check) {
		try {
			check.run();
		} catch (Throwable failure) {
			failures.add(name + " failed: " + failure.getMessage());
			failure.printStackTrace();
			recoverToTitle(context);
		}
	}

	private static void recoverToTitle(ClientGameTestContext context) {
		try {
			boolean inWorld = context.computeOnClient(client -> client.level != null);
			if (!inWorld) {
				context.setScreen(net.minecraft.client.gui.screens.TitleScreen::new);
				return;
			}
			context.runOnClient(client -> {
				if (client.level != null) {
					client.level.disconnect(Component.translatable("menu.savingLevel"));
					client.disconnect(new net.minecraft.client.gui.screens.GenericMessageScreen(Component.translatable("menu.savingLevel")), false);
				}
			});
			context.waitFor(client -> client.level == null, 200);
			context.setScreen(net.minecraft.client.gui.screens.TitleScreen::new);
		} catch (RuntimeException ignored) {
			// The next case reports its own setup failure if the client is stuck.
		}
	}

	private static void openAdmin(ClientGameTestContext context) {
		context.runOnClient(client -> {
			if (client.screen != null) {
				client.setScreen(null);
			}
		});
		context.waitFor(client -> client.screen == null);
		context.getInput().pressKey(GLFW.GLFW_KEY_P);
		context.waitForScreen(PlayAdminScreen.class);
	}

	private static void openPause(ClientGameTestContext context) {
		context.runOnClient(client -> {
			if (client.screen != null) {
				client.setScreen(null);
			}
			client.pauseGame(false);
		});
		context.waitForScreen(PauseScreen.class);
	}

	private static void assertUnpublishedLocalWorld(ClientGameTestContext context) {
		context.runOnClient(client -> {
			if (!client.isSingleplayer()) {
				throw new AssertionError("Expected an unpublished local world");
			}
		});
	}

	private static void publishToLan(ClientGameTestContext context) throws IOException {
		int port = freePort();
		context.runOnClient(client -> {
			IntegratedServer server = client.getSingleplayerServer();
			if (server == null || !server.publishServer(GameType.SURVIVAL, false, port) || !server.isPublished()) {
				throw new AssertionError("Could not publish the integrated server to LAN on port " + port);
			}
			if (client.isSingleplayer()) {
				throw new AssertionError("Client still treats the published server as unpublished singleplayer");
			}
		});
	}

	private static int freePort() throws IOException {
		try (ServerSocket socket = new ServerSocket(0)) {
			return socket.getLocalPort();
		}
	}

	private static void pressCycle(ClientGameTestContext context, String label) {
		context.runOnClient(client -> {
			CycleButton<?> knob = findCycle(client.screen, label);
			if (knob == null) {
				throw new AssertionError("Missing " + label + " control. Widgets: " + describe(client.screen));
			}
			knob.onPress(new MouseButtonInfo(GLFW.GLFW_MOUSE_BUTTON_LEFT, 0));
		});
	}

	private static CycleButton<?> findCycle(Screen screen, String label) {
		if (screen == null) {
			return null;
		}
		List<AbstractWidget> widgets = new ArrayList<>();
		collectWidgets(screen, widgets, new HashSet<>());
		for (AbstractWidget widget : widgets) {
			if (widget instanceof CycleButton<?> cycle && label.equals(cycleName(cycle))) {
				return cycle;
			}
		}
		return null;
	}

	private static void collectWidgets(GuiEventListener listener, List<AbstractWidget> widgets, Set<GuiEventListener> seen) {
		if (listener == null || !seen.add(listener)) {
			return;
		}
		if (listener instanceof AbstractWidget widget) {
			widgets.add(widget);
		}
		if (listener instanceof ContainerEventHandler container) {
			for (GuiEventListener child : container.children()) {
				collectWidgets(child, widgets, seen);
			}
		}
	}

	private static String cycleName(CycleButton<?> cycle) {
		try {
			Field name = CycleButton.class.getDeclaredField("name");
			name.setAccessible(true);
			return ((Component) name.get(cycle)).getString();
		} catch (ReflectiveOperationException e) {
			throw new AssertionError("Could not read cycle button name", e);
		}
	}

	private static String describe(Screen screen) {
		if (screen == null) {
			return "(no screen)";
		}
		List<AbstractWidget> widgets = new ArrayList<>();
		collectWidgets(screen, widgets, new HashSet<>());
		List<String> labels = new ArrayList<>();
		for (AbstractWidget widget : widgets) {
			String name = widget instanceof CycleButton<?> cycle ? cycleName(cycle) : widget.getMessage().getString();
			labels.add(widget.getClass().getSimpleName() + " '" + name + "'");
		}
		return labels.toString();
	}

	private static boolean pauseHasPackAdmin(ClientGameTestContext context) {
		String label = Component.translatable(PACK_ADMIN).getString();
		return context.computeOnClient(client -> {
			if (!(client.screen instanceof PauseScreen pause) || !pause.showsPauseMenu()) {
				throw new AssertionError("Expected the pause menu");
			}
			for (AbstractWidget widget : Screens.getWidgets(client.screen)) {
				if (label.equals(widget.getMessage().getString())) {
					return true;
				}
			}
			return false;
		});
	}

	private static String widgetText(ClientGameTestContext context) {
		return context.computeOnClient(client -> {
			Screen screen = client.screen;
			if (screen == null) {
				return "(no screen)";
			}
			List<String> labels = new ArrayList<>();
			for (AbstractWidget widget : Screens.getWidgets(screen)) {
				labels.add(widget.getClass().getSimpleName() + " '" + widget.getMessage().getString() + "'");
			}
			return labels.toString();
		});
	}

	private static AdminKnob elitesKnob() {
		return AdminCatalog.knobs().stream()
				.filter(knob -> ELITES.equals(knob.key()))
				.findFirst()
				.orElseThrow(() -> new AssertionError("Catalog is missing elites.enabled"));
	}

	private static boolean readElitesEnabled(Path lootFile) throws IOException {
		if (!Files.isRegularFile(lootFile)) {
			throw new AssertionError("Apply did not write " + lootFile);
		}
		JsonObject root = JsonParser.parseString(Files.readString(lootFile)).getAsJsonObject();
		JsonObject elites = root.getAsJsonObject("elites");
		if (elites == null || !elites.has("enabled")) {
			throw new AssertionError("Applied mob loot file is missing elites.enabled: " + Files.readString(lootFile));
		}
		return elites.get("enabled").getAsBoolean();
	}

	private static String savedMessage(Path configDir) {
		Path snapshotDir = FabricLoader.getInstance().isDevelopmentEnvironment()
				? PendingPackState.developmentSnapshotDir(FabricLoader.getInstance().getGameDir())
				: null;
		if (snapshotDir != null) {
			return Component.translatable("spicato_admin.apply.saved_dev", snapshotDir.toAbsolutePath().toString()).getString();
		}
		return Component.translatable("spicato_admin.apply.saved", configDir.toAbsolutePath().toString()).getString();
	}

	private static String chatText(Minecraft client) {
		try {
			Field field = net.minecraft.client.gui.components.ChatComponent.class.getDeclaredField("allMessages");
			field.setAccessible(true);
			@SuppressWarnings("unchecked")
			List<GuiMessage> messages = (List<GuiMessage>) field.get(client.gui.getChat());
			StringBuilder text = new StringBuilder();
			for (GuiMessage message : messages) {
				text.append(message.content().getString()).append('\n');
			}
			return text.toString();
		} catch (ReflectiveOperationException e) {
			throw new AssertionError("Could not read the player's chat", e);
		}
	}

	private static Map<String, byte[]> snapshotConfig(Path configDir) throws IOException {
		Map<String, byte[]> files = new TreeMap<>();
		if (!Files.isDirectory(configDir)) {
			return files;
		}
		try (Stream<Path> listing = Files.list(configDir)) {
			for (Path path : listing.filter(Files::isRegularFile).sorted(Comparator.comparing(Path::getFileName)).toList()) {
				files.put(path.getFileName().toString(), Files.readAllBytes(path));
			}
		}
		return files;
	}

	private static void assertConfigUnchanged(Map<String, byte[]> before, Map<String, byte[]> after) {
		if (before.size() != after.size() || !before.keySet().equals(after.keySet())) {
			throw new AssertionError("Closing without Apply changed config files from " + before.keySet() + " to " + after.keySet());
		}
		for (Map.Entry<String, byte[]> entry : before.entrySet()) {
			if (!Arrays.equals(entry.getValue(), after.get(entry.getKey()))) {
				throw new AssertionError("Closing without Apply changed " + entry.getKey());
			}
		}
	}

	@FunctionalInterface
	private interface ClientCheck {
		void run() throws Exception;
	}
}
