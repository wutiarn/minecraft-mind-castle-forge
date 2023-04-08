package ru.wtrn.minecraft.mindpalace.client.texture;

import java.util.function.Supplier;

public class CachedTextureSupplier implements Supplier<CachedTexture> {
    private CachedTexture cachedTexture;

    public CachedTextureSupplier(CachedTexture cachedTexture) {
        this.cachedTexture = cachedTexture;
    }

    @Override
    public CachedTexture get() {
        if (!cachedTexture.isActive()) {
            cachedTexture = TextureCache.get(cachedTexture.url);
        }
        return cachedTexture;
    }
}
