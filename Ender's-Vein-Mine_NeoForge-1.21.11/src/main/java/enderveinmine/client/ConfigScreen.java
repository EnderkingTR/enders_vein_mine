package enderveinmine.client;

import enderveinmine.config.VeinMineConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ConfigScreen {

    public static Screen createScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.literal("Vein Mine Configuration"));

        builder.setSavingRunnable(() -> {
            // ModConfig values are automatically saved when we set them, 
            // but we can ensure they are written.
            VeinMineConfig.SPEC.save();
        });

        ConfigCategory general = builder.getOrCreateCategory(Component.literal("General"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        general.addEntry(entryBuilder.startIntField(Component.literal("Max Blocks"), VeinMineConfig.INSTANCE.maxBlocks.get())
                .setDefaultValue(64)
                .setTooltip(Component.literal("Maximum number of blocks to mine at once"))
                .setSaveConsumer(VeinMineConfig.INSTANCE.maxBlocks::set)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.literal("Damage Tool"), VeinMineConfig.INSTANCE.damageTool.get())
                .setDefaultValue(true)
                .setTooltip(Component.literal("Should the tool take damage for each mined block?"))
                .setSaveConsumer(VeinMineConfig.INSTANCE.damageTool::set)
                .build());

        general.addEntry(entryBuilder.startEnumSelector(Component.literal("Activation Mode"), VeinMineConfig.ActivationMode.class, VeinMineConfig.INSTANCE.activationMode.get())
                .setDefaultValue(VeinMineConfig.ActivationMode.HOLD_KEY)
                .setTooltip(Component.literal("How vein mining is activated"))
                .setSaveConsumer(VeinMineConfig.INSTANCE.activationMode::set)
                .build());

        general.addEntry(entryBuilder.startEnumSelector(Component.literal("Shape Mode"), VeinMineConfig.ShapeMode.class, VeinMineConfig.INSTANCE.shapeMode.get())
                .setDefaultValue(VeinMineConfig.ShapeMode.SHAPELESS)
                .setTooltip(Component.literal("The shape of the mining area"))
                .setSaveConsumer(VeinMineConfig.INSTANCE.shapeMode::set)
                .build());

        general.addEntry(entryBuilder.startStrField(Component.literal("Custom Shape"), VeinMineConfig.INSTANCE.customShape.get())
                .setDefaultValue("3x3x3")
                .setTooltip(Component.literal("Custom shape dimensions (e.g. 3x3x3)"))
                .setSaveConsumer(VeinMineConfig.INSTANCE.customShape::set)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.literal("Render Outline"), VeinMineConfig.INSTANCE.renderOutline.get())
                .setDefaultValue(true)
                .setTooltip(Component.literal("Should an outline be rendered around blocks that will be mined?"))
                .setSaveConsumer(VeinMineConfig.INSTANCE.renderOutline::set)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.literal("Direct Drop To Inventory"), VeinMineConfig.INSTANCE.directDropToInventory.get())
                .setDefaultValue(false)
                .setTooltip(Component.literal("Should mined items drop directly into the player's inventory?"))
                .setSaveConsumer(VeinMineConfig.INSTANCE.directDropToInventory::set)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.literal("Prevent Tool Break"), VeinMineConfig.INSTANCE.preventToolBreak.get())
                .setDefaultValue(true)
                .setTooltip(Component.literal("Should vein mining stop before breaking the tool?"))
                .setSaveConsumer(VeinMineConfig.INSTANCE.preventToolBreak::set)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.literal("Hunger Drain"), VeinMineConfig.INSTANCE.hungerDrain.get())
                .setDefaultValue(true)
                .setTooltip(Component.literal("Should vein mining drain extra hunger?"))
                .setSaveConsumer(VeinMineConfig.INSTANCE.hungerDrain::set)
                .build());

        return builder.build();
    }
}
