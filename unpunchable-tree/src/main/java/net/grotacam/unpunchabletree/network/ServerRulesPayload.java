package net.grotacam.unpunchabletree.network;

import io.netty.buffer.ByteBuf;
import net.grotacam.unpunchabletree.UnpunchableTree;
import net.grotacam.unpunchabletree.rules.ServerRules;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server to client: the rules the server is using. */
public record ServerRulesPayload(ServerRules rules) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<ServerRulesPayload> TYPE =
			new CustomPacketPayload.Type<>(UnpunchableTree.id("server_rules"));

	public static final StreamCodec<ByteBuf, ServerRulesPayload> CODEC =
			ServerRules.STREAM_CODEC.map(ServerRulesPayload::new, ServerRulesPayload::rules);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
