package enderveinmine.network;

import enderveinmine.PlayerState;
import enderveinmine.VeinMine;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class VeinMineNetworkingServer {
    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(VeinMine.id("active"), (server, player, handler, buf, responseSender) -> {
            VeinMineActivePayload payload = new VeinMineActivePayload(buf);
            server.execute(() -> {
                PlayerState.getState(player.getUUID()).active = payload.active();
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(VeinMine.id("config"), (server, player, handler, buf, responseSender) -> {
            VeinMineConfigPayload payload = new VeinMineConfigPayload(buf);
            server.execute(() -> {
                PlayerState.getState(player.getUUID()).config = payload;
            });
        });
    }
}
