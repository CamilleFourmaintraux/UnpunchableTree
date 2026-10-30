package net.grotacam.unpunchabletree.rules;

import io.netty.buffer.ByteBuf;
import net.grotacam.unpunchabletree.registry.ModTags;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Which block groups can still be harvested by hand.
 *
 * @param punchableTrees  blocks in {@code #unpunchabletree:needs_axe} drop without an axe
 * @param punchableGround blocks in {@code #unpunchabletree:needs_shovel} drop without a shovel
 */
public record HarvestRules(boolean punchableTrees, boolean punchableGround) {
	public static final StreamCodec<ByteBuf, HarvestRules> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.BOOL, HarvestRules::punchableTrees,
			ByteBufCodecs.BOOL, HarvestRules::punchableGround,
			HarvestRules::new
	);

	/** Whether harvesting this block requires the correct tool under these rules. */
	public boolean needsProperTool(BlockState state) {
		return (!punchableTrees && state.is(ModTags.NEEDS_AXE))
				|| (!punchableGround && state.is(ModTags.NEEDS_SHOVEL));
	}
}
