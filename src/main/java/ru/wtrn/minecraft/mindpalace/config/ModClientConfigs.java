package ru.wtrn.minecraft.mindpalace.config;


import net.minecraftforge.common.ForgeConfigSpec;

public class ModClientConfigs {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec.ConfigValue<Integer> IMAGES_CLEANUP_DELAY_SECONDS = BUILDER
            .comment("Delay before unused images are removed from memory in seconds")
            .define("images_cleanup_delay_seconds", 60);

    public static final ForgeConfigSpec.ConfigValue<Integer> IMAGES_WORKER_THREADS_COUNT = BUILDER
            .define("images_worker_threads_count", 10);

    public static final ForgeConfigSpec.ConfigValue<Integer> IMAGES_RENDER_DISTANCE = BUILDER
            .define("images_render_distance", 300);

    public static final ForgeConfigSpec.ConfigValue<Integer> IMAGES_LOAD_DISTANCE = BUILDER
            .define("images_load_distance", 300);

    public static final ForgeConfigSpec.ConfigValue<Boolean> IMAGES_RENDER_BOTH_SIDES = BUILDER
            .define("images_render_both_sides", false);

    public static final ForgeConfigSpec SPEC = BUILDER.build();
}
