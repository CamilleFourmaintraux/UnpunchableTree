package net.grotacam.unpunchabletree.rules;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * How the Flint Hatchet can be thrown.
 *
 * @param throwingAllowed       the hatchet can be thrown at all; the other options only matter if true
 * @param shieldOverThrowing    with a blocking item in the off hand, right click blocks instead of throwing
 * @param thrownDurabilityCost  durability lost when a thrown hatchet hits an entity
 */
public record HatchetRules(boolean throwingAllowed, boolean shieldOverThrowing, int thrownDurabilityCost) {
	public static final StreamCodec<ByteBuf, HatchetRules> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.BOOL, HatchetRules::throwingAllowed,
			ByteBufCodecs.BOOL, HatchetRules::shieldOverThrowing,
			ByteBufCodecs.VAR_INT, HatchetRules::thrownDurabilityCost,
			HatchetRules::new
	);
}
