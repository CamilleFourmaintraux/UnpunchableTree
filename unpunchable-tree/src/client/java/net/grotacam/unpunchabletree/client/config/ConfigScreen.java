package net.grotacam.unpunchabletree.client.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.api.Requirement;
import me.shedaniel.clothconfig2.gui.entries.BooleanListEntry;
import net.grotacam.unpunchabletree.UnpunchableTree;
import net.grotacam.unpunchabletree.config.UnpunchableTreeConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

/** Cloth Config screen. Only referenced when Cloth Config is installed. */
public final class ConfigScreen {
	private static final String KEY = "config." + UnpunchableTree.MOD_ID + ".";

	public static Screen create(Screen parent) {
		UnpunchableTreeConfig config = UnpunchableTreeConfig.get();

		ConfigBuilder builder = ConfigBuilder.create()
				.setParentScreen(parent)
				.setTitle(Component.translatable(KEY + "title"))
				.setSavingRunnable(ConfigScreen::onSave);
		ConfigEntryBuilder entries = builder.entryBuilder();
		ConfigCategory general = builder.getOrCreateCategory(Component.translatable(KEY + "category.general"));

		general.addEntry(entries.startTextDescription(Component.translatable(KEY + "server_note")).build());

		general.addEntry(entries
				.startBooleanToggle(Component.translatable(KEY + "punchable_trees"), config.punchableTrees)
				.setDefaultValue(UnpunchableTreeConfig.DEFAULT_PUNCHABLE_TREES)
				.setTooltip(Component.translatable(KEY + "punchable_trees.tooltip"))
				.setSaveConsumer(value -> config.punchableTrees = value)
				.build());

		general.addEntry(entries
				.startBooleanToggle(Component.translatable(KEY + "punchable_ground"), config.punchableGround)
				.setDefaultValue(UnpunchableTreeConfig.DEFAULT_PUNCHABLE_GROUND)
				.setTooltip(Component.translatable(KEY + "punchable_ground.tooltip"))
				.setSaveConsumer(value -> config.punchableGround = value)
				.build());

		ConfigCategory hatchet = builder.getOrCreateCategory(Component.translatable(KEY + "category.hatchet"));
		hatchet.addEntry(entries.startTextDescription(Component.translatable(KEY + "server_note")).build());

		BooleanListEntry throwing = entries
				.startBooleanToggle(Component.translatable(KEY + "hatchet_throwing"), config.hatchetThrowing)
				.setDefaultValue(UnpunchableTreeConfig.DEFAULT_HATCHET_THROWING)
				.setTooltip(Component.translatable(KEY + "hatchet_throwing.tooltip"))
				.setSaveConsumer(value -> config.hatchetThrowing = value)
				.build();
		hatchet.addEntry(throwing);
		// The other hatchet options only matter when throwing is allowed: greyed out otherwise.
		Requirement throwingAllowed = Requirement.isTrue(throwing);

		hatchet.addEntry(entries
				.startBooleanToggle(Component.translatable(KEY + "shield_over_throwing"), config.shieldOverThrowing)
				.setDefaultValue(UnpunchableTreeConfig.DEFAULT_SHIELD_OVER_THROWING)
				.setTooltip(Component.translatable(KEY + "shield_over_throwing.tooltip"))
				.setSaveConsumer(value -> config.shieldOverThrowing = value)
				.setRequirement(throwingAllowed)
				.build());

		hatchet.addEntry(entries
				.startIntField(Component.translatable(KEY + "thrown_hatchet_durability_cost"), config.thrownHatchetDurabilityCost)
				.setMin(0)
				.setDefaultValue(UnpunchableTreeConfig.DEFAULT_THROWN_HATCHET_DURABILITY_COST)
				.setTooltip(Component.translatable(KEY + "thrown_hatchet_durability_cost.tooltip"))
				.setSaveConsumer(value -> config.thrownHatchetDurabilityCost = value)
				.setRequirement(throwingAllowed)
				.build());

		return builder.build();
	}

	private static void onSave() {
		UnpunchableTreeConfig.save();

		// In singleplayer (or when hosting a LAN world) the server runs in this game:
		// push the new rules to every player, the host included.
		MinecraftServer server = Minecraft.getInstance().getSingleplayerServer();
		if (server != null) {
			server.execute(() -> UnpunchableTree.syncRules(server));
		}
	}

	private ConfigScreen() {
	}
}
