package enderveinmine.network;

import net.minecraft.network.FriendlyByteBuf;

public record VeinMineActivePayload(boolean active) {
    public VeinMineActivePayload(FriendlyByteBuf buf) {
        this(buf.readBoolean());
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(active);
    }
}
