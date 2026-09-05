package enderveinmine;

import enderveinmine.network.VeinMineConfigPayload;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerState {
    public static final Map<UUID, PlayerState> STATES = new HashMap<>();

    public boolean active = false;
    public VeinMineConfigPayload config;

    public PlayerState(VeinMineConfigPayload config) {
        this.config = config;
    }
    
    public static PlayerState getState(UUID uuid) {
        return STATES.computeIfAbsent(uuid, k -> new PlayerState(null));
    }
}
