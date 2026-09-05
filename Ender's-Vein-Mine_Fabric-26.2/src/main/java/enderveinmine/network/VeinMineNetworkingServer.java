package enderveinmine.network;

import enderveinmine.PlayerState;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class VeinMineNetworkingServer {
    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(VeinMineActivePayload.ID, VeinMineActivePayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(VeinMineConfigPayload.ID, VeinMineConfigPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(VeinMineActivePayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                PlayerState.getState(context.player().getUUID()).active = payload.active();
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(VeinMineConfigPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                PlayerState.getState(context.player().getUUID()).config = payload;
            });
        });
    }
}
