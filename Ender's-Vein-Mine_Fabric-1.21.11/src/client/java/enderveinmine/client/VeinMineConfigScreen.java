package enderveinmine.client;

import enderveinmine.config.VeinMineConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class VeinMineConfigScreen {
    
    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("config.veinmine.title"));
        
        builder.setSavingRunnable(() -> {
            VeinMineConfig.save();
            VeinMineClient.sendConfigToServer();
        });
        
        ConfigCategory general = builder.getOrCreateCategory(Component.translatable("config.veinmine.category.general"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        
        VeinMineConfig config = VeinMineConfig.INSTANCE;
        
        general.addEntry(entryBuilder.startIntField(Component.translatable("config.veinmine.maxBlocks"), config.maxBlocks)
                .setDefaultValue(64)
                .setTooltip(Component.translatable("config.veinmine.maxBlocks.tooltip"))
                .setSaveConsumer(newValue -> config.maxBlocks = newValue)
                .build());
                
        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.veinmine.damageTool"), config.damageTool)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config.veinmine.damageTool.tooltip"))
                .setSaveConsumer(newValue -> config.damageTool = newValue)
                .build());
                
        general.addEntry(entryBuilder.startEnumSelector(Component.translatable("config.veinmine.activationMode"), VeinMineConfig.ActivationMode.class, config.activationMode)
                .setDefaultValue(VeinMineConfig.ActivationMode.HOLD_KEY)
                .setTooltip(Component.translatable("config.veinmine.activationMode.tooltip"))
                .setEnumNameProvider(mode -> Component.translatable("config.veinmine.enum.activation." + ((VeinMineConfig.ActivationMode) mode).name().toLowerCase(java.util.Locale.ROOT)))
                .setSaveConsumer(newValue -> config.activationMode = newValue)
                .build());
                
        general.addEntry(entryBuilder.startEnumSelector(Component.translatable("config.veinmine.shapeMode"), VeinMineConfig.ShapeMode.class, config.shapeMode)
                .setDefaultValue(VeinMineConfig.ShapeMode.SHAPELESS)
                .setTooltip(Component.translatable("config.veinmine.shapeMode.tooltip"))
                .setEnumNameProvider(mode -> Component.translatable("config.veinmine.enum.shape." + ((VeinMineConfig.ShapeMode) mode).name().toLowerCase(java.util.Locale.ROOT)))
                .setSaveConsumer(newValue -> config.shapeMode = newValue)
                .build());
                
        general.addEntry(entryBuilder.startStrField(Component.translatable("config.veinmine.customShape"), config.customShape)
                .setDefaultValue("3x3x3")
                .setTooltip(Component.translatable("config.veinmine.customShape.tooltip"))
                .setSaveConsumer(newValue -> config.customShape = newValue)
                .build());
                
        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.veinmine.renderOutline"), config.renderOutline)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config.veinmine.renderOutline.tooltip"))
                .setSaveConsumer(newValue -> config.renderOutline = newValue)
                .build());
                
        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.veinmine.directDrop"), config.directDropToInventory)
                .setDefaultValue(false)
                .setTooltip(Component.translatable("config.veinmine.directDrop.tooltip"))
                .setSaveConsumer(newValue -> config.directDropToInventory = newValue)
                .build());
                
        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.veinmine.preventToolBreak"), config.preventToolBreak)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config.veinmine.preventToolBreak.tooltip"))
                .setSaveConsumer(newValue -> config.preventToolBreak = newValue)
                .build());
                
        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.veinmine.hungerDrain"), config.hungerDrain)
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config.veinmine.hungerDrain.tooltip"))
                .setSaveConsumer(newValue -> config.hungerDrain = newValue)
                .build());
                
        return builder.build();
    }
}
