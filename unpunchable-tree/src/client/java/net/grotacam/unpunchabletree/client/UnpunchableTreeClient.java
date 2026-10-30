package net.grotacam.unpunchabletree.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.grotacam.unpunchabletree.client.render.ThrownHatchetRenderer;
import net.grotacam.unpunchabletree.network.ServerRulesPayload;
import net.grotacam.unpunchabletree.registry.ModEntityTypes;
import net.grotacam.unpunchabletree.rules.ServerRules;
import net.minecraft.client.renderer.entity.EntityRenderers;

public class UnpunchableTreeClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(ServerRulesPayload.TYPE,
				(payload, context) -> ServerRules.setFromServer(payload.rules()));

		// Forget them on disconnect: a server without the mod never sends any.
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ServerRules.setFromServer(null));

		EntityRenderers.register(ModEntityTypes.THROWN_HATCHET, ThrownHatchetRenderer::new);
	}
}
