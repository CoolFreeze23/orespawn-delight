package danger.orespawn.delight.setup;

import danger.orespawn.delight.creativetabs.ModTabs;
import danger.orespawn.delight.items.ModItems;
import net.neoforged.bus.api.IEventBus;

public class Registration {

    public static void init(IEventBus modEventBus) {
        ModItems.ITEMS.register(modEventBus);
        ModTabs.TABS.register(modEventBus);
        // Tier 3 modules (each self-contained per the build's coordination rule)
        danger.orespawn.delight.blocks.FeastsInit.init(modEventBus);
        danger.orespawn.delight.farming.GreenThumbInit.init(modEventBus);
        danger.orespawn.delight.knife.KnifeInit.init(modEventBus);
        // The Absurd 21
        danger.orespawn.delight.absurd.AbsurdItemsInit.init(modEventBus);
        danger.orespawn.delight.absurd.blocks.AbsurdBlocksInit.init(modEventBus);
        danger.orespawn.delight.interact.InteractionsInit.init(modEventBus);
    }
}
