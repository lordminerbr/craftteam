package com.craftteams;

import com.craftteams.net.Net;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slf4j.Logger;

@Mod(CraftTeams.MOD_ID)
public class CraftTeams {
    public static final String MOD_ID = "craftteams";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CraftTeams(IEventBus modBus) {
        modBus.addListener(Net::register);
        NeoForge.EVENT_BUS.addListener(this::onServerStarting);
        NeoForge.EVENT_BUS.addListener(this::onCommands);
    }

    private void onServerStarting(ServerStartingEvent event) {
        CraftConfig.load();
    }

    private void onCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal("craftteams")
                .requires(src -> src.hasPermission(2))
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    PacketDistributor.sendToPlayer(player, Net.buildState(player.getServer(), true));
                    return 1;
                })
        );
    }
}
