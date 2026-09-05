package enderveinmine.network;

import enderveinmine.VeinMine;
import enderveinmine.config.VeinMineConfig;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record VeinMineConfigPayload(int maxBlocks, boolean damageTool, String activationMode, String shapeMode, String customShape, boolean renderOutline, boolean directDropToInventory, boolean preventToolBreak, boolean hungerDrain) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<VeinMineConfigPayload> ID = new CustomPacketPayload.Type<>(VeinMine.id("config"));
    public static final StreamCodec<RegistryFriendlyByteBuf, VeinMineConfigPayload> CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeInt(payload.maxBlocks());
                buf.writeBoolean(payload.damageTool());
                buf.writeUtf(payload.activationMode());
                buf.writeUtf(payload.shapeMode());
                buf.writeUtf(payload.customShape());
                buf.writeBoolean(payload.renderOutline());
                buf.writeBoolean(payload.directDropToInventory());
                buf.writeBoolean(payload.preventToolBreak());
                buf.writeBoolean(payload.hungerDrain());
            },
            buf -> new VeinMineConfigPayload(
                    buf.readInt(),
                    buf.readBoolean(),
                    buf.readUtf(),
                    buf.readUtf(),
                    buf.readUtf(),
                    buf.readBoolean(),
                    buf.readBoolean(),
                    buf.readBoolean(),
                    buf.readBoolean()
            )
    );

    public static VeinMineConfigPayload fromConfig() {
        return new VeinMineConfigPayload(
                VeinMineConfig.INSTANCE.maxBlocks.get(),
                VeinMineConfig.INSTANCE.damageTool.get(),
                VeinMineConfig.INSTANCE.activationMode.get().name(),
                VeinMineConfig.INSTANCE.shapeMode.get().name(),
                VeinMineConfig.INSTANCE.customShape.get(),
                VeinMineConfig.INSTANCE.renderOutline.get(),
                VeinMineConfig.INSTANCE.directDropToInventory.get(),
                VeinMineConfig.INSTANCE.preventToolBreak.get(),
                VeinMineConfig.INSTANCE.hungerDrain.get()
        );
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
