package com.craftteams.net;

import com.craftteams.CraftTeams;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Cliente -> servidor: uma acao feita na tela (criar time, liberar mod etc). */
public record ActionPayload(String action, String team, String arg) implements CustomPacketPayload {

    public static final Type<ActionPayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(CraftTeams.MOD_ID, "action"));

    public static final StreamCodec<ByteBuf, ActionPayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, ActionPayload::action,
        ByteBufCodecs.STRING_UTF8, ActionPayload::team,
        ByteBufCodecs.STRING_UTF8, ActionPayload::arg,
        ActionPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
