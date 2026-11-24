package com.craftteams.mixin;

import com.craftteams.CraftPolicy;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Todo slot de resultado de craft (mesa, craft 2x2 do inventario e bancadas de mods
 * que usam ResultSlot) passa por mayPickup. Aqui a gente barra o item se o time
 * do jogador nao tem o mod liberado.
 */
@Mixin(Slot.class)
public abstract class SlotMixin {
    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    private void craftteams$blockCraft(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (!((Object) this instanceof ResultSlot)) return;
        if (!(player instanceof ServerPlayer sp)) return;
        ItemStack result = ((Slot) (Object) this).getItem();
        if (!CraftPolicy.canCraft(sp, result)) {
            CraftPolicy.warn(sp, result);
            cir.setReturnValue(false);
        }
    }
}
