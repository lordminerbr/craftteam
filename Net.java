package com.craftteams.net;

import com.craftteams.CraftConfig;
import com.craftteams.CraftTeams;
import com.craftteams.client.ClientPayloadHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.ArrayList;
import java.util.List;

public final class Net {
    private Net() {}

    public static void register(RegisterPayloadHandlersEvent event) {
        // optional(): jogadores sem o mod no cliente ainda conseguem entrar no servidor.
        PayloadRegistrar reg = event.registrar("1").optional();
        reg.playToClient(StatePayload.TYPE, StatePayload.CODEC, ClientPayloadHandler::handleState);
        reg.playToServer(ActionPayload.TYPE, ActionPayload.CODEC, Net::handleAction);
    }

    public static StatePayload buildState(MinecraftServer server, boolean open) {
        List<String> mods = ModList.get().getMods().stream()
            .map(m -> m.getModId())
            .filter(id -> !id.equals("minecraft") && !id.equals("neoforge"))
            .sorted()
            .toList();

        List<String> teams = new ArrayList<>();
        List<String> teamMods = new ArrayList<>();
        List<String> members = new ArrayList<>();
        for (PlayerTeam t : server.getScoreboard().getPlayerTeams()) {
            String name = t.getName();
            teams.add(name);
            CraftConfig.modsOf(name).forEach(m -> teamMods.add(name + "|" + m));
            t.getPlayers().forEach(p -> members.add(name + "|" + p));
        }
        teams.sort(String::compareTo);
        return new StatePayload(open, mods, teams, teamMods, members);
    }

    private static void handleAction(ActionPayload p, IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer sp)) return;
        if (!sp.hasPermissions(2)) return;
        MinecraftServer server = sp.getServer();
        if (server == null) return;
        Scoreboard sb = server.getScoreboard();

        try {
            switch (p.action()) {
                case "create_team" -> {
                    String n = p.team().trim();
                    if (!n.isEmpty() && !n.contains("|") && sb.getPlayerTeam(n) == null) {
                        sb.addPlayerTeam(n);
                    }
                }
                case "delete_team" -> {
                    PlayerTeam t = sb.getPlayerTeam(p.team());
                    if (t != null) {
                        sb.removePlayerTeam(t);
                        CraftConfig.removeTeam(p.team());
                    }
                }
                case "toggle_mod" -> {
                    if (sb.getPlayerTeam(p.team()) != null) CraftConfig.toggle(p.team(), p.arg());
                }
                case "add_player" -> {
                    PlayerTeam t = sb.getPlayerTeam(p.team());
                    if (t != null) sb.addPlayerToTeam(p.arg(), t);
                }
                case "remove_player" -> {
                    PlayerTeam t = sb.getPlayerTeam(p.team());
                    if (t != null) sb.removePlayerFromTeam(p.arg(), t);
                }
                default -> { }
            }
        } catch (Exception e) {
            CraftTeams.LOGGER.warn("Acao invalida na tela de times: {}", p, e);
        }

        PacketDistributor.sendToPlayer(sp, buildState(server, false));
    }
}
