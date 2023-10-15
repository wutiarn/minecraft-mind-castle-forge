package ru.wtrn.minecraft.mindpalace.http;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerMemosTokensHolder {
    public static final PlayerMemosTokensHolder INSTANCE = new PlayerMemosTokensHolder();
    private ConcurrentHashMap<UUID, String> tokensByPlayer = new ConcurrentHashMap<>();

    private PlayerMemosTokensHolder() {
    }

    public void setToken(UUID playerUuid, String token) {
        tokensByPlayer.put(playerUuid, token);
    }

    public void removeToken(UUID playerUuid) {
        tokensByPlayer.remove(playerUuid);
    }

    public String getToken(UUID playerUuid) {
        return tokensByPlayer.get(playerUuid);
    }
}
