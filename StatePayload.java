package com.craftteams.net;

import com.craftteams.CraftTeams;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/** Servidor -> cliente: estado completo para a tela de administracao. */
public record StatePayload(boolean open, List<String> mods, List<String> teams,
                           List<String> teamMods, List<String> members) implements CustomPacketPayload {

    public static final Type<StatePayload> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(CraftTeams.MOD_ID, "state"));

    private static final StreamCodec<ByteBuf, List<String>> STRINGS =
        ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list());

    public static final StreamCodec<ByteBuf, StatePayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL, StatePayload::open,
        STRINGS, StatePayload::mods,
        STRINGS, StatePayload::teams,
        STRINGS, StatePayload::teamMods,
        STRINGS, StatePayload::members,
        StatePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
