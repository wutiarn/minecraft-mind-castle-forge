package ru.wtrn.minecraft.mindpalace.config;


import net.minecraftforge.common.ForgeConfigSpec;

public class ModClientConfigs {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec.ConfigValue<Integer> IMAGES_CLEANUP_DELAY_SECONDS = BUILDER
            .comment("Delay before unused images are removed from memory in seconds")
            .define("images_cleanup_delay_seconds", 30);

    public static final ForgeConfigSpec SPEC = BUILDER.build();
}
