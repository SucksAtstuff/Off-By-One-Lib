package studio.offbyone.lib;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import studio.offbyone.lib.shape.SelectShapePayload;

/**
 * Shared code for Off by One Studio's Create addons.
 *
 * The library registers almost nothing itself: addons extend or instantiate its classes and
 * register them under their own mod id. Only things that must exist once per game (the radial
 * shape menu's handler and packet) belong to this mod id.
 */
@Mod(OffByOneLib.MODID)
public class OffByOneLib {
    public static final String MODID = "offbyonelib";
    public static final Logger LOGGER = LogUtils.getLogger();

    public OffByOneLib(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(SelectShapePayload::register);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
