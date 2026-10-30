package net.grotacam.unpunchabletree.registry;

import java.util.function.Function;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.grotacam.unpunchabletree.UnpunchableTree;
import net.grotacam.unpunchabletree.item.FlintHatchetItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ToolMaterial;

public final class ModItems {
	/**
	 * Wood tier, slightly faster to mine with than wood (2.5 vs 2.0), but half the durability
	 * (30 vs 59) and about half the enchantability (8 vs 15).
	 */
	public static final ToolMaterial FLINT = new ToolMaterial(
			BlockTags.INCORRECT_FOR_WOODEN_TOOL,
			30,
			2.5F,
			0.0F,
			8,
			ModTags.FLINT_HATCHET_REPAIR_MATERIALS
	);

	/**
	 * A full axe (stripping, scraping, shield disabling, axe enchantments) that fights halfway
	 * between the wooden sword and the wooden axe, and can be thrown.
	 * See {@link FlintHatchetItem} for its combat stats.
	 */
	public static final Item FLINT_HATCHET = register(
			"flint_hatchet",
			properties -> new FlintHatchetItem(FLINT, properties),
			new Item.Properties()
	);

	private static <T extends Item> T register(String name, Function<Item.Properties, T> factory, Item.Properties properties) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, UnpunchableTree.id(name));
		T item = factory.apply(properties.setId(key));
		Registry.register(BuiltInRegistries.ITEM, key, item);
		return item;
	}

	public static void initialize() {
		// Like vanilla axes, it is both a tool and a weapon.
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
				.register(entries -> entries.addBefore(Items.WOODEN_SHOVEL, FLINT_HATCHET));
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT)
				.register(entries -> entries.addBefore(Items.WOODEN_AXE, FLINT_HATCHET));
	}

	private ModItems() {
	}
}
