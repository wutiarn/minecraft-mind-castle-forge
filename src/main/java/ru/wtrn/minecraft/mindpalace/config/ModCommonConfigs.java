package ru.wtrn.minecraft.mindpalace.config;


import net.minecraftforge.common.ForgeConfigSpec;

public class ModCommonConfigs {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<String> MCI_SERVER_URL;


    static {
        ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

        MCI_SERVER_URL = BUILDER.comment("MCI Server URL").define("mci_url", "https://mci.wtrn.ru");

        SPEC = BUILDER.build();
    }
}
