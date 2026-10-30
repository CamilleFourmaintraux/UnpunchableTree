package net.grotacam.unpunchabletree.rules;

import io.netty.buffer.ByteBuf;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.grotacam.unpunchabletree.config.UnpunchableTreeConfig;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;

/**
 * All the rules decided by the server's config. The server sends them to every player on join,
 * because the client predicts mining speed and item use and must agree with the server.
 */
public record ServerRules(HarvestRules harvest, HatchetRules hatchet) {
	public static final StreamCodec<ByteBuf, ServerRules> STREAM_CODEC = StreamCodec.composite(
			HarvestRules.STREAM_CODEC, ServerRules::harvest,
			HatchetRules.STREAM_CODEC, ServerRules::hatchet,
			ServerRules::new
	);

	/**
	 * What a client assumes on a server that doesn't have the mod (and so never sends rules):
	 * plain vanilla harvesting, so it never mines slower than the server expects.
	 */
	private static final ServerRules UNMODDED_SERVER = new ServerRules(
			new HarvestRules(true, true),
			new HatchetRules(false, true, 0));

	/** Rules received from the server this client is connected to, null when not connected. */
	private static volatile ServerRules fromServer;

	/** Pass null when disconnecting. */
	public static void setFromServer(ServerRules rules) {
		fromServer = rules;
	}

	/** Server side: this game's config. Client side: what the server sent. */
	public static ServerRules forLevel(Level level) {
		if (!level.isClientSide()) {
			return UnpunchableTreeConfig.rules();
		}
		ServerRules rules = fromServer;
		return rules != null ? rules : UNMODDED_SERVER;
	}

	/**
	 * For code that has no level at hand, such as block state properties.
	 * <ul>
	 *   <li>Dedicated server: its config.</li>
	 *   <li>Client connected to a server with the mod: what that server sent. In singleplayer and
	 *   on a LAN world this game hosts, that is this game's own config, so its integrated server
	 *   thread and its client thread agree.</li>
	 *   <li>Client on a server without the mod: plain vanilla rules.</li>
	 * </ul>
	 */
	public static ServerRules current() {
		ServerRules rules = fromServer;
		if (rules != null) {
			return rules;
		}
		return FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER
				? UnpunchableTreeConfig.rules()
				: UNMODDED_SERVER;
	}
}
