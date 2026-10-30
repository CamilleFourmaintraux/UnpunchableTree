package net.grotacam.unpunchabletree.mixin;

import net.grotacam.unpunchabletree.rules.ServerRules;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Flags logs (and, if enabled, shovel blocks) as "requires the correct tool for drops", the vanilla
 * property of stone and ores. Vanilla then does the rest through {@code Player#hasCorrectToolForDrops}:
 * without an item whose tool component can harvest the block, mining is slow (divisor 100 instead of
 * 30) and drops nothing. Explosions and other non-player sources don't check it, so they still drop
 * the block.
 * <p>
 * Because the property itself changes, mods that display it, such as Jade, show the right result.
 */
@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin {
	@Inject(method = "requiresCorrectToolForDrops", at = @At("RETURN"), cancellable = true)
	private void unpunchabletree$requireProperTool(CallbackInfoReturnable<Boolean> cir) {
		if (!cir.getReturnValueZ()
				&& ServerRules.current().harvest().needsProperTool((BlockState) (Object) this)) {
			cir.setReturnValue(true);
		}
	}
}
