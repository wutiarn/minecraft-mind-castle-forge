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

    public static final ForgeConfigSpec SPEC = BUILDER.build();
}
