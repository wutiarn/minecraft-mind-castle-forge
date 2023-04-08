package ru.wtrn.minecraft.mindpalace.client.texture;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import ru.wtrn.minecraft.mindpalace.WtrnMindPalaceMod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class TextureCache {
    private static final ConcurrentHashMap<String, CachedTexture> cached = new ConcurrentHashMap<>();
    private static int cleanupTickCounter = 0;
    public static Supplier<CachedTexture> LOADING_TEXTURE = getSupplier(new ResourceLocation(WtrnMindPalaceMod.MOD_ID, "textures/loading.png").toString());
    public static Supplier<CachedTexture> ERROR_TEXTURE = getSupplier(new ResourceLocation(WtrnMindPalaceMod.MOD_ID, "textures/error.png").toString());

    public static CachedTexture get(String url) {
        if (url.startsWith("http")) {
            return cached.computeIfAbsent(url, CachedHttpTexture::new);
        }
        return cached.computeIfAbsent(url, CachedResourceTexture::new);
    }

    public static Supplier<CachedTexture> getSupplier(String url) {
        return new CachedTextureSupplier(get(url));
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) {
        if (event.getLevel().isClientSide()) {
            for (CachedTexture cache : cached.values())
                cache.cleanup();
            cached.clear();
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        cleanupTickCounter++;
        if (cleanupTickCounter < 100) {
            return;
        }
        cleanupTickCounter = 0;

        ArrayList<String> cleanedUpTextures = new ArrayList<>();

        for (Map.Entry<String, CachedTexture> entry : cached.entrySet()) {
            boolean cleanedUp = entry.getValue().tryCleanup();
            if (cleanedUp) {
                cleanedUpTextures.add(entry.getKey());
            }
        }

        if (!cleanedUpTextures.isEmpty()) {
            for (String key : cleanedUpTextures) {
                cached.remove(key);
            }
        }
    }
}
