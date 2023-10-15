package ru.wtrn.minecraft.mindpalace.config;


import net.minecraftforge.common.ForgeConfigSpec;

public class ModCommonConfigs {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.ConfigValue<String> MCI_SERVER_URL = BUILDER.comment("MCI Server URL")
            .define("mci_url", "https://mci.wtrn.ru");

    public static final ForgeConfigSpec.ConfigValue<String> MCI_SECRET = BUILDER.comment("MCI Secret")
            .define("mci_secret", "TODO");

    public static final ForgeConfigSpec.ConfigValue<Integer> DEFAULT_IMAGE_WIDTH = BUILDER.comment("Default width for new images")
            .define("default_image_width", 3);

    public static final ForgeConfigSpec.ConfigValue<Double> FAST_RAILS_HIGH_SPEED = BUILDER.comment("Speed on long distances")
            .define("fast_rails_high_speed", 3.0);

    public static final ForgeConfigSpec.ConfigValue<Double> FAST_RAILS_LOW_SPEED = BUILDER.comment("Speed on short distances")
            .define("fast_rails_low_speed", 0.7);

    public static final ForgeConfigSpec.ConfigValue<Double> FAST_RAILS_BASE_SPEED = BUILDER.comment("Base speed for minecart. Should be 0.4 or less is recommended to avoid slopes and corners traverse issues.")
            .define("fast_rails_base_speed", 0.4);

    public static final ForgeConfigSpec.ConfigValue<Integer> FAST_RAILS_MIN_SPEEDUP_DISTANCE = BUILDER.comment("Count of straight rail blocks ahead required for speedup to start")
            .define("fast_rails_min_speedup_distance", 30);

    public static final ForgeConfigSpec.ConfigValue<Integer> FAST_RAILS_MAX_SPEEDUP_DISTANCE = BUILDER.comment("Count of straight rail blocks ahead required for speedup to reach max speed")
            .define("fast_rails_max_speedup_distance", 100);

    public static final ForgeConfigSpec SPEC = BUILDER.build();
}
