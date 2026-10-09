package spicato_admin.client;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.tabs.MenuTabBar;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import spicato_admin.SpicatoAdmin;
import spicato_admin.knobs.AdminCatalog;
import spicato_admin.knobs.AdminPage;
import spicato_admin.knobs.AdminSection;
import spicato_admin.pending.PendingPackState;
import spicato_admin.pending.ReflectiveConfigReloader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class PlayAdminScreen extends Screen {
	private final Screen lastScreen;
	private final PendingPackState pending;
	private int selectedTab;
	private AdminPage page = AdminPage.PLAY;
	private TabManager tabManager;
	private TabNavigationBar tabNavigationBar;
	private HeaderAndFooterLayout layout;
	private Button applyButton;

	public PlayAdminScreen(Screen lastScreen) {
		super(Component.translatable("spicato_admin.screen.play"));
		this.lastScreen = lastScreen;
		this.pending = PendingPackState.load(FabricLoader.getInstance().getConfigDir());
	}

	@Override
	protected void init() {
		this.tabManager = new TabManager(this::addRenderableWidget, this::removeWidget);
		List<AdminSection> sections = AdminCatalog.SECTIONS;
		Tab[] tabs = new Tab[sections.size()];
		for (int i = 0; i < sections.size(); i++) {
			final int index = i;
			AdminSection section = sections.get(i);
			tabs[i] = new PlaySectionTab(section, this.page, this.minecraft, this.font, this.pending, modId -> {
				this.pending.resetMod(modId);
				this.selectedTab = index;
				this.rebuildWidgets();
			});
		}
		this.tabNavigationBar = MenuTabBar.builder(this.tabManager, this.width).addTabs(tabs).build();
		this.addRenderableWidget(this.tabNavigationBar);

		this.layout = new HeaderAndFooterLayout(this, 0, 36);
		LinearLayout footer = LinearLayout.horizontal().spacing(8);
		footer.addChild(CycleButton.builder(
						(AdminPage value) -> Component.translatable(
								value == AdminPage.DEBUG ? "spicato_admin.page.debug" : "spicato_admin.page.play"
						),
						this.page
				)
				.withValues(AdminPage.PLAY, AdminPage.DEBUG)
				.create(0, 0, 100, 20, Component.translatable("spicato_admin.page"), (button, value) -> {
					this.page = value;
					this.rebuildWidgets();
				}));
		this.applyButton = Button.builder(Component.translatable("spicato_admin.button.apply"), button -> this.apply())
				.width(100)
				.build();
		footer.addChild(this.applyButton);
		footer.addChild(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose()).width(100).build());
		this.layout.addToFooter(footer);
		this.layout.visitWidgets(this::addRenderableWidget);

		this.pending.setOnChange(this::refreshApply);
		this.refreshApply();
		this.tabNavigationBar.selectTab(this.selectedTab, false);
		this.repositionElements();
	}

	private void refreshApply() {
		if (this.applyButton != null) {
			this.applyButton.active = this.pending.playCurvesValid();
		}
	}

	private void apply() {
		Path configDir = FabricLoader.getInstance().getConfigDir();
		Path snapshotDir = FabricLoader.getInstance().isDevelopmentEnvironment()
				? PendingPackState.developmentSnapshotDir(FabricLoader.getInstance().getGameDir())
				: null;
		try {
			this.pending.apply(configDir, snapshotDir, new ReflectiveConfigReloader());
			tellApplySaved(configDir, snapshotDir);
		} catch (IOException e) {
			SpicatoAdmin.LOGGER.error("Failed to apply pack admin changes", e);
		}
		this.refreshApply();
	}

	private void tellApplySaved(Path configDir, Path snapshotDir) {
		Component message = snapshotDir != null
				? Component.translatable("spicato_admin.apply.saved_dev", snapshotDir.toAbsolutePath().toString())
				: Component.translatable("spicato_admin.apply.saved", configDir.toAbsolutePath().toString());
		SpicatoAdmin.LOGGER.info("{}", message.getString());
		if (this.minecraft.player != null) {
			this.minecraft.player.sendSystemMessage(message);
		}
	}

	@Override
	protected void repositionElements() {
		if (this.tabNavigationBar == null || this.layout == null || this.tabManager == null) {
			return;
		}
		this.tabNavigationBar.arrangeElements(this.width);
		int tabBottom = this.tabNavigationBar.getRectangle().bottom();
		this.tabManager.setTabArea(new ScreenRectangle(
				0,
				tabBottom,
				this.width,
				Math.max(0, this.height - this.layout.getFooterHeight() - tabBottom)
		));
		this.layout.arrangeElements();
	}

	@Override
	public void onClose() {
		this.minecraft.gui.setScreen(this.lastScreen);
	}
}
