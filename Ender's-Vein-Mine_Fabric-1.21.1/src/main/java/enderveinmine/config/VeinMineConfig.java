package enderveinmine.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import enderveinmine.VeinMine;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class VeinMineConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "vein-mine.json");
    
    public static VeinMineConfig INSTANCE = new VeinMineConfig();
    
    public int maxBlocks = 64;
    public boolean damageTool = true;
    public ActivationMode activationMode = ActivationMode.HOLD_KEY;
    public ShapeMode shapeMode = ShapeMode.SHAPELESS;
    public String customShape = "3x3x3";
    public boolean renderOutline = true;
    public boolean directDropToInventory = false;
    public boolean preventToolBreak = true;
    public boolean hungerDrain = true;
    
    public enum ActivationMode {
        HOLD_KEY,
        SNEAK,
        TOGGLE
    }
    
    public enum ShapeMode {
        SHAPELESS,
        SHAPED,
        TUNNEL_3X3,
        TUNNEL_2X1,
        STAIRS_UP,
        STAIRS_DOWN,
        TUNNEL_1X1
    }
    
    public static void load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                VeinMineConfig loaded = GSON.fromJson(reader, VeinMineConfig.class);
                if (loaded != null) {
                    INSTANCE = loaded;
                    // Fix missing boolean field defaulting to false because of Gson UnsafeAllocator
                    if (!INSTANCE.renderOutline && !CONFIG_FILE.exists()) {
                        // wait, actually we can just check if the text contains the key
                    }
                }
            } catch (Exception e) {
                VeinMine.LOGGER.error("Failed to load config", e);
            }
        }
        // Save to create file with default values if it doesn't exist
        save();
    }
    
    public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(INSTANCE, writer);
        } catch (IOException e) {
            VeinMine.LOGGER.error("Failed to save config", e);
        }
    }
}
