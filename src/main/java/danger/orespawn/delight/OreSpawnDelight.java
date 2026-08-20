package danger.orespawn.delight;

import com.mojang.logging.LogUtils;
import danger.orespawn.delight.setup.Registration;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

import java.util.Locale;

/**
 * OreSpawn Delight - a Farmer's Delight kitchen for OreSpawn.
 * Structure mirrors the oceansdelight addon (main class -> Registration -> registries).
 */
@Mod(OreSpawnDelight.MODID)
public class OreSpawnDelight {
    public static final String MODID = "orespawn_delight";
    public static final Logger LOGGER = LogUtils.getLogger();

    public OreSpawnDelight(IEventBus modEventBus, ModContainer modContainer) {
        Registration.init(modEventBus);
    }

    public static ResourceLocation prefix(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path.toLowerCase(Locale.ROOT));
    }
}
