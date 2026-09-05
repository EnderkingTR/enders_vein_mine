package enderveinmine.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class VeinMineConfig {

    public static final VeinMineConfig INSTANCE;
    public static final ModConfigSpec SPEC;

    public final ModConfigSpec.IntValue maxBlocks;
    public final ModConfigSpec.BooleanValue damageTool;
    public final ModConfigSpec.EnumValue<ActivationMode> activationMode;
    public final ModConfigSpec.EnumValue<ShapeMode> shapeMode;
    public final ModConfigSpec.ConfigValue<String> customShape;
    public final ModConfigSpec.BooleanValue renderOutline;
    public final ModConfigSpec.BooleanValue directDropToInventory;
    public final ModConfigSpec.BooleanValue preventToolBreak;
    public final ModConfigSpec.BooleanValue hungerDrain;

    public enum ActivationMode {
        HOLD_KEY,
        SNEAK,
        TOGGLE
    }

    public enum ShapeMode {
        SHAPELESS,
        SHAPED,
        TUNNEL_3X3,
        TUNNEL_2X1,
        STAIRS_UP,
        STAIRS_DOWN,
        TUNNEL_1X1
    }

    static {
        Pair<VeinMineConfig, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(VeinMineConfig::new);
        SPEC = specPair.getRight();
        INSTANCE = specPair.getLeft();
    }

    public VeinMineConfig(ModConfigSpec.Builder builder) {
        builder.push("General");
        
        maxBlocks = builder
                .comment("Maximum number of blocks to mine at once")
                .defineInRange("maxBlocks", 64, 1, 1024);
                
        damageTool = builder
                .comment("Should the tool take damage for each mined block?")
                .define("damageTool", true);
                
        activationMode = builder
                .comment("How vein mining is activated")
                .defineEnum("activationMode", ActivationMode.HOLD_KEY);
                
        shapeMode = builder
                .comment("The shape of the mining area")
                .defineEnum("shapeMode", ShapeMode.SHAPELESS);
                
        customShape = builder
                .comment("Custom shape dimensions (e.g. 3x3x3). Only used if shapeMode is SHAPED")
                .define("customShape", "3x3x3");
                
        renderOutline = builder
                .comment("Should an outline be rendered around blocks that will be mined?")
                .define("renderOutline", true);
                
        directDropToInventory = builder
                .comment("Should mined items drop directly into the player's inventory?")
                .define("directDropToInventory", false);
                
        preventToolBreak = builder
                .comment("Should vein mining stop before breaking the tool?")
                .define("preventToolBreak", true);
                
        hungerDrain = builder
                .comment("Should vein mining drain extra hunger?")
                .define("hungerDrain", true);

        builder.pop();
    }
}
