package com.craftteams.client;

import com.craftteams.net.StatePayload;
import net.minecraft.client.Minecraft;

public final class ClientScreens {
    private ClientScreens() {}

    public static void show(StatePayload payload) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof TeamScreen screen) {
            screen.update(payload);
        } else if (payload.open()) {
            mc.setScreen(new TeamScreen(payload));
        }
    }
}
