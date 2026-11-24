package com.jeict;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Client settings, editable in game under Mods > CelesTech EMI Fix > Config, or in config/jeict-client.toml. */
public final class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue RESTYLE = BUILDER
            .comment("Draw EMI's recipe window in CelesTech's style while a CelesTech category is open.")
            .define("restyleRecipeWindow", true);

    public static final ModConfigSpec.DoubleValue MAX_SCALE = BUILDER
            .comment("CelesTech recipes in EMI are enlarged to fill the free space, up to this factor.",
                    "1.0 keeps EMI's normal size.")
            .defineInRange("maxRecipeScale", 3.0, 1.0, 4.0);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private Config() {}

    public static boolean restyle() {
        try {
            return RESTYLE.getAsBoolean();
        } catch (IllegalStateException notLoadedYet) {
            return RESTYLE.getDefault();
        }
    }

    public static double maxScale() {
        try {
            return MAX_SCALE.getAsDouble();
        } catch (IllegalStateException notLoadedYet) {
            return MAX_SCALE.getDefault();
        }
    }
}
