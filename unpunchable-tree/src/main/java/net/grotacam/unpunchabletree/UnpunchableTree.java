package net.grotacam.unpunchabletree;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.grotacam.unpunchabletree.config.UnpunchableTreeConfig;
import net.grotacam.unpunchabletree.network.ServerRulesPayload;
import net.grotacam.unpunchabletree.registry.ModEntityTypes;
import net.grotacam.unpunchabletree.registry.ModItems;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnpunchableTree implements ModInitializer {
	public static final String MOD_ID = "unpunchabletree";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		UnpunchableTreeConfig.load();
		ModItems.initialize();
		ModEntityTypes.initialize();

		// The client predicts mining speed and item use, so it must apply the same rules as the server.
		PayloadTypeRegistry.playS2C().register(ServerRulesPayload.TYPE, ServerRulesPayload.CODEC);
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			if (ServerPlayNetworking.canSend(handler, ServerRulesPayload.TYPE)) {
				sender.sendPacket(new ServerRulesPayload(UnpunchableTreeConfig.rules()));
			}
		});
	}

	/**
	 * Sends the current rules to every connected player, e.g. after the config was edited
	 * in singleplayer or on a LAN world.
	 */
	public static void syncRules(MinecraftServer server) {
		ServerRulesPayload payload = new ServerRulesPayload(UnpunchableTreeConfig.rules());
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			if (ServerPlayNetworking.canSend(player, ServerRulesPayload.TYPE)) {
				ServerPlayNetworking.send(player, payload);
			}
		}
	}
}
