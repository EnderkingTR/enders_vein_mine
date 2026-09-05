package enderveinmine.network;

import enderveinmine.VeinMine;
import enderveinmine.config.VeinMineConfig;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record VeinMineConfigPayload(int maxBlocks, boolean damageTool, String activationMode, String shapeMode, String customShape, boolean renderOutline, boolean directDropToInventory, boolean preventToolBreak, boolean hungerDrain) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<VeinMineConfigPayload> ID = new CustomPacketPayload.Type<>(VeinMine.id("config"));
    public static final StreamCodec<RegistryFriendlyByteBuf, VeinMineConfigPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, VeinMineConfigPayload::maxBlocks,
            ByteBufCodecs.BOOL, VeinMineConfigPayload::damageTool,
            ByteBufCodecs.STRING_UTF8, VeinMineConfigPayload::activationMode,
            ByteBufCodecs.STRING_UTF8, VeinMineConfigPayload::shapeMode,
            ByteBufCodecs.STRING_UTF8, VeinMineConfigPayload::customShape,
            ByteBufCodecs.BOOL, VeinMineConfigPayload::renderOutline,
            ByteBufCodecs.BOOL, VeinMineConfigPayload::directDropToInventory,
            ByteBufCodecs.BOOL, VeinMineConfigPayload::preventToolBreak,
            ByteBufCodecs.BOOL, VeinMineConfigPayload::hungerDrain,
            VeinMineConfigPayload::new
    );

    public VeinMineConfigPayload(VeinMineConfig config) {
        this(config.maxBlocks, config.damageTool, config.activationMode.name(), config.shapeMode.name(), config.customShape, config.renderOutline, config.directDropToInventory, config.preventToolBreak, config.hungerDrain);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
