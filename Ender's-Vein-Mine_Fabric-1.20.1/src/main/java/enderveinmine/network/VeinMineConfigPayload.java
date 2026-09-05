package enderveinmine.network;

import enderveinmine.config.VeinMineConfig;
import net.minecraft.network.FriendlyByteBuf;

public record VeinMineConfigPayload(int maxBlocks, boolean damageTool, String activationMode, String shapeMode, String customShape, boolean renderOutline, boolean directDropToInventory, boolean preventToolBreak, boolean hungerDrain) {
    public VeinMineConfigPayload(FriendlyByteBuf buf) {
        this(buf.readInt(), buf.readBoolean(), buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean());
    }

    public void write(FriendlyByteBuf buf) {
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
}
