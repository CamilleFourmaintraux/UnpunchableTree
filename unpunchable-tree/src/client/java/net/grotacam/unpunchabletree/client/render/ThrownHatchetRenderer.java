package net.grotacam.unpunchabletree.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.grotacam.unpunchabletree.entity.ThrownHatchet;
import net.grotacam.unpunchabletree.registry.ModItems;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Renders the thrown hatchet with its 3D item model, tumbling end over end while it flies and
 * resting blade first once it is stuck in a block.
 */
public class ThrownHatchetRenderer extends EntityRenderer<ThrownHatchet, ThrownHatchetRenderState> {
	/** Size of the item model, 1 being a full block. */
	private static final float SCALE = 0.8F;
	/** Tumbling speed, in degrees per tick (one full turn every 8 ticks). */
	private static final float SPIN_DEGREES_PER_TICK = 45.0F;
	/** Size of one pixel of the hatchet texture, in blocks. */
	private static final float TEXTURE_PIXEL = SCALE / 16.0F;
	/** How far the model is pulled back out of the block when stuck, so only the blade sinks in. */
	private static final float STUCK_PULL_BACK = 1.5F * TEXTURE_PIXEL;

	private final ItemModelResolver itemModelResolver;
	// The thrown stack itself is not synced to clients (like the trident), only whether it glints.
	private final ItemStack plainHatchet = new ItemStack(ModItems.FLINT_HATCHET);
	private final ItemStack glintingHatchet = new ItemStack(ModItems.FLINT_HATCHET);

	public ThrownHatchetRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.itemModelResolver = context.getItemModelResolver();
		this.glintingHatchet.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
	}

	@Override
	public ThrownHatchetRenderState createRenderState() {
		return new ThrownHatchetRenderState();
	}

	@Override
	public void extractRenderState(ThrownHatchet entity, ThrownHatchetRenderState state, float partialTick) {
		super.extractRenderState(entity, state, partialTick);
		state.yRot = entity.getYRot(partialTick);
		state.xRot = entity.getXRot(partialTick);
		state.stuck = entity.isStuck();
		this.itemModelResolver.updateForNonLiving(state.item, entity.isFoil() ? glintingHatchet : plainHatchet,
				ItemDisplayContext.NONE, entity);
	}

	@Override
	public void submit(ThrownHatchetRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
		poseStack.pushPose();

		// Same orientation as arrows: local +X points along the direction of travel, +Y up.
		poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot - 90.0F));
		poseStack.mulPose(Axis.ZP.rotationDegrees(state.xRot));

		if (state.stuck) {
			poseStack.translate(-STUCK_PULL_BACK, 0.0F, 0.0F);
		} else {
			// Tumble forward: the top of the hatchet moves towards the direction of travel.
			poseStack.mulPose(Axis.ZP.rotationDegrees(-state.ageInTicks * SPIN_DEGREES_PER_TICK));
		}

		poseStack.scale(SCALE, SCALE, SCALE);
		// The texture has the handle bottom left and the blade facing up left. Mirror it and turn
		// it so that, at rest, the blade faces forward and the handle points down, like a hatchet
		// held ready to throw. This is also the pose it has when stuck.
		poseStack.mulPose(Axis.ZP.rotationDegrees(-45.0F));
		poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

		state.item.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
		poseStack.popPose();

		super.submit(state, poseStack, collector, camera);
	}
}
