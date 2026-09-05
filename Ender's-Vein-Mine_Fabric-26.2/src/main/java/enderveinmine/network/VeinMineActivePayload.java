package enderveinmine.network;

import enderveinmine.VeinMine;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record VeinMineActivePayload(boolean active) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<VeinMineActivePayload> ID = new CustomPacketPayload.Type<>(VeinMine.id("active"));
    public static final StreamCodec<RegistryFriendlyByteBuf, VeinMineActivePayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, VeinMineActivePayload::active,
            VeinMineActivePayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
