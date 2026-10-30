package net.grotacam.unpunchabletree.registry;

import net.grotacam.unpunchabletree.UnpunchableTree;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public final class ModTags {
	/** Blocks that need an axe to drop, unless "punchable trees" is enabled. */
	public static final TagKey<Block> NEEDS_AXE =
			TagKey.create(Registries.BLOCK, UnpunchableTree.id("needs_axe"));

	/** Blocks that need a shovel to drop, when "punchable ground" is disabled. */
	public static final TagKey<Block> NEEDS_SHOVEL =
			TagKey.create(Registries.BLOCK, UnpunchableTree.id("needs_shovel"));

	/** Items that repair the Flint Hatchet in an anvil. */
	public static final TagKey<Item> FLINT_HATCHET_REPAIR_MATERIALS =
			TagKey.create(Registries.ITEM, UnpunchableTree.id("flint_hatchet_repair_materials"));

	private ModTags() {
	}
}
