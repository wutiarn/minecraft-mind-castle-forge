package ru.wtrn.minecraft.mindpalace.client.texture;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import ru.wtrn.minecraft.mindpalace.WtrnMindPalaceMod;

import java.util.concurrent.ConcurrentHashMap;

public class TextureCache {
    private static final ConcurrentHashMap<String, CachedTexture> cached = new ConcurrentHashMap<>();
    public static CachedTexture LOADING_TEXTURE = get(new ResourceLocation(WtrnMindPalaceMod.MOD_ID, "textures/loading.png"));
    public static CachedTexture ERROR_TEXTURE = get(new ResourceLocation(WtrnMindPalaceMod.MOD_ID, "textures/error.png"));

    public static CachedTexture get(String url) {
        return cached.computeIfAbsent(url, CachedHttpTexture::new);
    }

    public static CachedTexture get(ResourceLocation resourceLocation) {
        String url = resourceLocation.toString();
        return cached.computeIfAbsent(url, CachedResourceTexture::new);
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
