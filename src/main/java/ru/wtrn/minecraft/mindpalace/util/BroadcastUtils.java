package ru.wtrn.minecraft.mindpalace.util;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

public class BroadcastUtils {
    public static void broadcastMessage(MinecraftServer server, String message) {
        server.getPlayerList().broadcastSystemMessage(Component.literal(message), false);
    }
}
