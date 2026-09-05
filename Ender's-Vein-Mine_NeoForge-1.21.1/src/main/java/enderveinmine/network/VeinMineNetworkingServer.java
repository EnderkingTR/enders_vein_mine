package enderveinmine.network;

import enderveinmine.PlayerState;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class VeinMineNetworkingServer {
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("vein_mine").versioned("1.0.0");

        registrar.playToServer(
                VeinMineActivePayload.ID,
                VeinMineActivePayload.CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    PlayerState.getState(context.player().getUUID()).active = payload.active();
                })
        );

        registrar.playToServer(
                VeinMineConfigPayload.ID,
                VeinMineConfigPayload.CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    PlayerState.getState(context.player().getUUID()).config = payload;
                })
        );
    }
}
