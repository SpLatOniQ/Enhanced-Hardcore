package xyz.splatoniq.hardcore;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue RESPAWN_AT_GRAVE;
    public static final ModConfigSpec.ConfigValue<String> REVIVAL_ITEM;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        RESPAWN_AT_GRAVE = builder
                .comment("Should revived people respawn at their grave?")
                .comment("default: false")
                .define("enableRespawningAtGrave", false);
        REVIVAL_ITEM = builder
                .comment("Item used to revive people")
                .comment("default: minecraft:enchanted_golden_apple")
                .define("revivalItem", "minecraft:enchanted_golden_apple");

        SPEC = builder.build();
    }
}
