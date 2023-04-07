package ru.wtrn.minecraft.mindpalace.client.texture;

import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.concurrent.ConcurrentHashMap;

public class TextureCache {
    private static final ConcurrentHashMap<String, CachedTexture> cached = new ConcurrentHashMap<>();

    public static CachedTexture get(String url) {
        return cached.computeIfAbsent(url, CachedTexture::new);
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) {
        if (event.getLevel().isClientSide()) {
            for (CachedTexture cache : cached.values())
                cache.cleanup();
            cached.clear();
        }
    }
}
