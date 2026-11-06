package dev.donut.client.render;

import net.minecraft.client.Minecraft;

public final class EntityEspRules {
    private EntityEspRules() { }

    public static boolean throughWallsAllowed(Minecraft client) {
        var server = client.getSingleplayerServer();
        return server != null && !server.isPublished();
    }
}