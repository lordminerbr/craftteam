package com.craftteams.client;

import com.craftteams.net.StatePayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ClientPayloadHandler {
    private ClientPayloadHandler() {}

    public static void handleState(StatePayload payload, IPayloadContext ctx) {
        ClientScreens.show(payload);
    }
}
