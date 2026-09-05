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
            (buf, payload) -> payload.write(buf),
            VeinMineConfigPayload::new
    );

    public VeinMineConfigPayload(RegistryFriendlyByteBuf buf) {
        this(buf.readInt(), buf.readBoolean(), buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean());
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeInt(maxBlocks);
        buf.writeBoolean(damageTool);
        buf.writeUtf(activationMode);
        buf.writeUtf(shapeMode);
        buf.writeUtf(customShape);
        buf.writeBoolean(renderOutline);
        buf.writeBoolean(directDropToInventory);
        buf.writeBoolean(preventToolBreak);
        buf.writeBoolean(hungerDrain);
    }

    public VeinMineConfigPayload(VeinMineConfig config) {
        this(config.maxBlocks, config.damageTool, config.activationMode.name(), config.shapeMode.name(), config.customShape, config.renderOutline, config.directDropToInventory, config.preventToolBreak, config.hungerDrain);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
