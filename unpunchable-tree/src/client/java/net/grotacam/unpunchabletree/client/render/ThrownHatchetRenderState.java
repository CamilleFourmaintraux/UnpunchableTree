package net.grotacam.unpunchabletree.client.render;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;

public class ThrownHatchetRenderState extends EntityRenderState {
	public final ItemStackRenderState item = new ItemStackRenderState();
	public float yRot;
	public float xRot;
	public boolean stuck;
}
