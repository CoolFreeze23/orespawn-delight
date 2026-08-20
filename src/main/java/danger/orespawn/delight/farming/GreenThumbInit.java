package danger.orespawn.delight.farming;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Green Thumb - makes OreSpawn's 11 inert seed items plantable.
 *
 * The OreSpawn port registers 14 seed items as plain {@code registerSimpleItem}
 * placeholders (the 1.7.10 originals were ItemSeeds/ItemSeedFood), so the crop
 * blocks that already exist in the port are only reachable via worldgen. This
 * package wires the missing seed -&gt; plant step with a game-bus event handler;
 * no new blocks or items are registered.
 *
 * Self-contained per the tier coordination rule: the parent wires a single
 * {@code GreenThumbInit.init(modEventBus)} call from the main mod class.
 */
public class GreenThumbInit {

    /**
     * @param modEventBus the mod event bus (unused - this feature registers no
     *                    registry entries; kept for the shared init contract)
     */
    public static void init(IEventBus modEventBus) {
        // Gameplay events live on the game bus, not the mod bus.
        NeoForge.EVENT_BUS.addListener(GreenThumbHandler::onUseSeedOnBlock);
        NeoForge.EVENT_BUS.addListener(GreenThumbHandler::onSeedTooltip);
    }
}
