package com.craftteams;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.Team;

public final class CraftPolicy {
    private CraftPolicy() {}

    public static String modOf(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace();
    }

    /** Decide se o jogador pode pegar o resultado deste craft. */
    public static boolean canCraft(ServerPlayer player, ItemStack result) {
        if (result.isEmpty()) return true;
        if (player.isCreative()) return true;
        String modId = modOf(result);
        if (CraftConfig.isAlwaysAllowed(modId)) return true;
        Team team = player.getTeam();
        if (team == null) return false;
        return CraftConfig.teamAllows(team.getName(), modId);
    }

    public static void warn(ServerPlayer player, ItemStack result) {
        player.displayClientMessage(
            Component.literal("Seu time nao pode craftar itens do mod: " + modOf(result)), true);
    }
}
