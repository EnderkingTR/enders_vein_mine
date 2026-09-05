package enderveinmine;

import com.mojang.logging.LogUtils;
import enderveinmine.config.VeinMineConfig;
import enderveinmine.logic.BlockBreakHandler;
import enderveinmine.logic.BlockInteractHandler;
import enderveinmine.network.VeinMineNetworkingServer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

@Mod(VeinMine.MOD_ID)
public class VeinMine {
    public static final String MOD_ID = "vein_mine";
    public static final Logger LOGGER = LogUtils.getLogger();

    public VeinMine(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, VeinMineConfig.SPEC);
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, (container, screen) -> enderveinmine.client.ConfigScreen.createScreen(screen));

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(VeinMineNetworkingServer::register);

        NeoForge.EVENT_BUS.register(BlockBreakHandler.class);
        NeoForge.EVENT_BUS.register(BlockInteractHandler.class);

        if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient()) {
            modEventBus.addListener(enderveinmine.client.ClientSetup::registerBindings);
            NeoForge.EVENT_BUS.register(enderveinmine.client.ClientSetup.ClientGameEvents.class);
            NeoForge.EVENT_BUS.register(enderveinmine.client.BlockOutlineRenderer.class);
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Hello from Vein Mine (NeoForge)!");
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
