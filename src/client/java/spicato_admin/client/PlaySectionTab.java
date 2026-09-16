package spicato_admin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.ScrollableLayout;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import spicato_admin.knobs.AdminCatalog;
import spicato_admin.knobs.AdminKnob;
import spicato_admin.knobs.AdminPage;
import spicato_admin.knobs.AdminSection;
import spicato_admin.pending.PendingPackState;

import java.util.List;
import java.util.function.Consumer;

/**
 * One Play section: knobs scroll; section actions stay pinned above Apply/Done.
 */
final class PlaySectionTab implements Tab {
	static final int ROW_WIDTH = 310;
	static final int ACTION_ROW_HEIGHT = 28;

	private final Component title;
	private final LinearLayout root;
	private final ScrollableLayout scroll;

	PlaySectionTab(
			AdminSection section,
			AdminPage page,
			Minecraft minecraft,
			Font font,
			PendingPackState pending,
			Consumer<String> resetMod
	) {
		this.title = Component.literal(section.title());
		LinearLayout knobs = LinearLayout.vertical().spacing(4);
		knobs.defaultCellSetting().alignHorizontallyCenter();

		List<AdminKnob> catalogKnobs = AdminCatalog.knobs(page, section.modId());
		if (catalogKnobs.isEmpty()) {
			String emptyKey = page == AdminPage.DEBUG ? "spicato_admin.debug.empty" : "spicato_admin.play.empty";
			knobs.addChild(new StringWidget(Component.translatable(emptyKey), font));
		}
		for (AdminKnob knob : catalogKnobs) {
			addKnob(knobs, font, pending, knob);
		}

		this.scroll = new ScrollableLayout(minecraft, knobs, 80);
		this.scroll.setMinWidth(ROW_WIDTH);

		this.root = LinearLayout.vertical().spacing(8);
		this.root.defaultCellSetting().alignHorizontallyCenter();
		this.root.addChild(this.scroll);
		this.root.addChild(Button.builder(
				Component.translatable("spicato_admin.button.reset_mod"),
				button -> resetMod.accept(section.modId())
		).width(150).build());
	}

	static int knobViewportHeight(int tabAreaHeight) {
		return Math.max(0, tabAreaHeight - ACTION_ROW_HEIGHT);
	}

	@Override
	public Component getTabTitle() {
		return this.title;
	}

	@Override
	public Component getTabExtraNarration() {
		return Component.empty();
	}

	@Override
	public void visitChildren(Consumer<AbstractWidget> consumer) {
		this.root.visitWidgets(consumer);
	}

	@Override
	public void doLayout(ScreenRectangle rectangle) {
		this.scroll.setMaxHeight(knobViewportHeight(rectangle.height()));
		this.root.arrangeElements();
		FrameLayout.alignInRectangle(this.root, rectangle, 0.5F, 0.0F);
	}

	private static void addKnob(LinearLayout rows, Font font, PendingPackState pending, AdminKnob knob) {
		switch (knob.widget()) {
			case CHECKBOX -> rows.addChild(CycleButton.onOffBuilder(pending.getBoolean(knob))
					.create(0, 0, ROW_WIDTH, 20, Component.literal(knob.label()), (button, value) -> pending.setBoolean(knob, value)));
			case SLIDER -> rows.addChild(new KnobSlider(
					ROW_WIDTH,
					Component.literal(knob.label()),
					knob.min(),
					knob.max(),
					knob.step(),
					isInteger(knob),
					() -> pending.getNumber(knob),
					value -> pending.setNumber(knob, value)
			));
			case CURVE -> {
				rows.addChild(new StringWidget(Component.literal(knob.label()), font));
				addCurveSlider(rows, pending, knob, "peak", "Peak");
				addCurveSlider(rows, pending, knob, "min", "Min");
				addCurveSlider(rows, pending, knob, "max", "Max");
			}
		}
	}

	private static void addCurveSlider(
			LinearLayout rows,
			PendingPackState pending,
			AdminKnob knob,
			String subKey,
			String label
	) {
		rows.addChild(new KnobSlider(
				ROW_WIDTH,
				Component.literal(label),
				knob.min(),
				knob.max(),
				knob.step(),
				false,
				() -> pending.getNumber(knob, subKey),
				value -> pending.setNumber(knob, subKey, value)
		));
	}

	private static boolean isInteger(AdminKnob knob) {
		return knob.step() != null && knob.step() >= 1.0 - 1.0e-9;
	}
}
