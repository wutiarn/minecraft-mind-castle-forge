package ru.wtrn.minecraft.mindpalace.util;

import java.time.Duration;
import java.util.function.Supplier;

public class CachedAction<T> {

    private final Supplier<T> delegate;
    private long lastExecutionTimestamp = 0;
    private final long interval;
    T cachedResult;

    public CachedAction(Duration cacheTime, Supplier<T> delegate) {
        this.interval = cacheTime.toMillis();
        this.delegate = delegate;
    }

    public synchronized T invoke() {
        long now = System.currentTimeMillis();
        if (now - lastExecutionTimestamp < interval) {
            return cachedResult;
        }
        lastExecutionTimestamp = now;

        cachedResult = delegate.get();
        return cachedResult;
    }
}
