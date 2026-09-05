package enderveinmine.client;

import enderveinmine.config.VeinMineConfig;
import enderveinmine.network.VeinMineActivePayload;
import enderveinmine.network.VeinMineConfigPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

public class VeinMineClient implements ClientModInitializer {
    
    private static KeyMapping activateKey;
    private static KeyMapping settingsKey;
    private static KeyMapping nextShapeKey;
    private static KeyMapping prevShapeKey;
    
    private static boolean lastActiveState = false;

    @Override
    public void onInitializeClient() {
        String category = "category.vein-mine";

        activateKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.vein-mine.activate",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_GRAVE_ACCENT,
                category
        ));

        settingsKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.vein-mine.settings",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                category
        ));

        nextShapeKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.vein-mine.next_shape",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_BRACKET,
                category
        ));

        prevShapeKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.vein-mine.prev_shape",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT_BRACKET,
                category
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            
            while (settingsKey.consumeClick()) {
                client.setScreen(VeinMineConfigScreen.create(null));
            }

            boolean shapeChanged = false;
            while (nextShapeKey.consumeClick()) {
                VeinMineConfig.ShapeMode[] modes = VeinMineConfig.ShapeMode.values();
                int nextOrd = (VeinMineConfig.INSTANCE.shapeMode.ordinal() + 1) % modes.length;
                VeinMineConfig.INSTANCE.shapeMode = modes[nextOrd];
                shapeChanged = true;
            }
            while (prevShapeKey.consumeClick()) {
                VeinMineConfig.ShapeMode[] modes = VeinMineConfig.ShapeMode.values();
                int prevOrd = (VeinMineConfig.INSTANCE.shapeMode.ordinal() - 1 + modes.length) % modes.length;
                VeinMineConfig.INSTANCE.shapeMode = modes[prevOrd];
                shapeChanged = true;
            }

            if (shapeChanged) {
                VeinMineConfig.save();
                sendConfigToServer();
                String modeName = VeinMineConfig.INSTANCE.shapeMode.name();
                net.minecraft.network.chat.MutableComponent msg = net.minecraft.network.chat.Component.translatable("message.veinmine.mode_changed", 
                        net.minecraft.network.chat.Component.translatable("config.veinmine.enum.shape." + modeName.toLowerCase(java.util.Locale.ROOT)));
                if (modeName.startsWith("TUNNEL") || modeName.startsWith("STAIRS")) {
                    msg.append(net.minecraft.network.chat.Component.literal(" - "))
                       .append(net.minecraft.network.chat.Component.translatable("message.veinmine.max_blocks", VeinMineConfig.INSTANCE.maxBlocks));
                }
                client.player.displayClientMessage(msg, false);
            }

            boolean isActive = lastActiveState;
            boolean stateChanged = false;

            if (VeinMineConfig.INSTANCE.activationMode == VeinMineConfig.ActivationMode.SNEAK) {
                isActive = client.player.isCrouching();
            } else if (VeinMineConfig.INSTANCE.activationMode == VeinMineConfig.ActivationMode.HOLD_KEY) {
                isActive = activateKey.isDown();
            } else if (VeinMineConfig.INSTANCE.activationMode == VeinMineConfig.ActivationMode.TOGGLE) {
                while (activateKey.consumeClick()) {
                    isActive = !isActive;
                }
            }

            if (isActive != lastActiveState) {
                lastActiveState = isActive;
                sendActiveState(isActive);
            }
        });

        net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.LAST.register(new BlockOutlineRenderer());
    }
    
    public static boolean isActive() {
        return lastActiveState;
    }
    
    public static void sendActiveState(boolean active) {
        if (ClientPlayNetworking.canSend(enderveinmine.VeinMine.id("active"))) {
            net.minecraft.network.FriendlyByteBuf buf = net.fabricmc.fabric.api.networking.v1.PacketByteBufs.create();
            new VeinMineActivePayload(active).write(buf);
            ClientPlayNetworking.send(enderveinmine.VeinMine.id("active"), buf);
        }
    }

    public static void sendConfigToServer() {
        if (ClientPlayNetworking.canSend(enderveinmine.VeinMine.id("config"))) {
            net.minecraft.network.FriendlyByteBuf buf = net.fabricmc.fabric.api.networking.v1.PacketByteBufs.create();
            new VeinMineConfigPayload(VeinMineConfig.INSTANCE).write(buf);
            ClientPlayNetworking.send(enderveinmine.VeinMine.id("config"), buf);
        }
    }
}