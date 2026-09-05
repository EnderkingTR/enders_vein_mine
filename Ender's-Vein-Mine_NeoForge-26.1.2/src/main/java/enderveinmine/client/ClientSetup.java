package enderveinmine.client;

import com.mojang.blaze3d.platform.InputConstants;
import enderveinmine.VeinMine;
import enderveinmine.config.VeinMineConfig;
import enderveinmine.network.VeinMineActivePayload;
import enderveinmine.network.VeinMineConfigPayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;

public class ClientSetup {

    public static KeyMapping activateKey;
    public static KeyMapping settingsKey;
    public static KeyMapping nextShapeKey;
    public static KeyMapping prevShapeKey;

    private static boolean lastActiveState = false;

    @SubscribeEvent
    public static void registerBindings(RegisterKeyMappingsEvent event) {
        KeyMapping.Category category = new KeyMapping.Category(VeinMine.id("category"));
        event.registerCategory(category);
        
        activateKey = new KeyMapping("key.vein-mine.activate", GLFW.GLFW_KEY_GRAVE_ACCENT, category);
        settingsKey = new KeyMapping("key.vein-mine.settings", GLFW.GLFW_KEY_K, category);
        nextShapeKey = new KeyMapping("key.vein-mine.next_shape", GLFW.GLFW_KEY_RIGHT_BRACKET, category);
        prevShapeKey = new KeyMapping("key.vein-mine.prev_shape", GLFW.GLFW_KEY_LEFT_BRACKET, category);

        event.register(activateKey);
        event.register(settingsKey);
        event.register(nextShapeKey);
        event.register(prevShapeKey);
    }
    
    public static boolean isActive() {
        return lastActiveState;
    }

    public static void sendActiveState(boolean active) {
        ClientPacketDistributor.sendToServer(new VeinMineActivePayload(active));
    }

    public static void sendConfigToServer() {
        ClientPacketDistributor.sendToServer(VeinMineConfigPayload.fromConfig());
    }

    public static class ClientGameEvents {
        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            Minecraft client = Minecraft.getInstance();
            if (client.player == null) return;
            
            while (settingsKey.consumeClick()) {
                client.setScreen(ConfigScreen.createScreen(client.screen));
            }

            boolean shapeChanged = false;
            while (nextShapeKey.consumeClick()) {
                VeinMineConfig.ShapeMode[] modes = VeinMineConfig.ShapeMode.values();
                int nextOrd = (VeinMineConfig.INSTANCE.shapeMode.get().ordinal() + 1) % modes.length;
                VeinMineConfig.INSTANCE.shapeMode.set(modes[nextOrd]);
                shapeChanged = true;
            }
            while (prevShapeKey.consumeClick()) {
                VeinMineConfig.ShapeMode[] modes = VeinMineConfig.ShapeMode.values();
                int prevOrd = (VeinMineConfig.INSTANCE.shapeMode.get().ordinal() - 1 + modes.length) % modes.length;
                VeinMineConfig.INSTANCE.shapeMode.set(modes[prevOrd]);
                shapeChanged = true;
            }

            if (shapeChanged) {
                VeinMineConfig.SPEC.save();
                sendConfigToServer();
                String modeName = VeinMineConfig.INSTANCE.shapeMode.get().name();
                MutableComponent msg = Component.translatable("message.veinmine.mode_changed", 
                        Component.translatable("config.veinmine.enum.shape." + modeName.toLowerCase(Locale.ROOT)));
                if (modeName.startsWith("TUNNEL") || modeName.startsWith("STAIRS")) {
                    msg.append(Component.literal(" - "))
                       .append(Component.translatable("message.veinmine.max_blocks", VeinMineConfig.INSTANCE.maxBlocks.get()));
                }
                client.player.sendSystemMessage(msg);
            }

            boolean isActive = lastActiveState;

            VeinMineConfig.ActivationMode mode = VeinMineConfig.INSTANCE.activationMode.get();
            if (mode == VeinMineConfig.ActivationMode.SNEAK) {
                isActive = client.player.isCrouching();
            } else if (mode == VeinMineConfig.ActivationMode.HOLD_KEY) {
                isActive = activateKey.isDown();
            } else if (mode == VeinMineConfig.ActivationMode.TOGGLE) {
                while (activateKey.consumeClick()) {
                    isActive = !isActive;
                }
            }

            if (isActive != lastActiveState) {
                lastActiveState = isActive;
                sendActiveState(isActive);
            }
        }
    }
}
