package danger.orespawn.delight.absurd.blocks;

import danger.orespawn.delight.OreSpawnDelight;
import danger.orespawn.delight.blocks.FeastCenterpieceBlock;
import danger.orespawn.delight.creativetabs.ModTabs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * ABSURD-21 Tier: the four absurd blocks - self-contained registration unit
 * mirroring FeastsInit (own DeferredRegisters, one init(IEventBus) call for
 * the parent to wire, creative-tab append via BuildCreativeModeTabContentsEvent).
 *
 * Frozen block ids: yellowcake, cheese_wheel, butter_pat, sunday_pot_roast.
 */
public class AbsurdBlocksInit {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(OreSpawnDelight.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(OreSpawnDelight.MODID);

    // === BLOCKS ===

    /** The literal uranium cake. Cake properties, cake pattern, extra dosimetry. */
    public static final DeferredBlock<Block> YELLOWCAKE = BLOCKS.register("yellowcake",
            () -> new YellowcakeBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE)));

    /** Six-wedge rat bait. randomTicks() feeds the scheduled-tick backstop re-arm. */
    public static final DeferredBlock<Block> CHEESE_WHEEL = BLOCKS.register("cheese_wheel",
            () -> new CheeseWheelBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE).randomTicks()));

    /**
     * Carpet-height butter slick. friction(0.98F) = vanilla ice tier (Blocks.ICE
     * verified); randomTicks() feeds the melt backstop; carpet-tier strength.
     */
    public static final DeferredBlock<Block> BUTTER_PAT = BLOCKS.register("butter_pat",
            () -> new ButterPatBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_YELLOW)
                    .strength(0.1F)
                    .sound(SoundType.HONEY_BLOCK)
                    .friction(0.98F)
                    .randomTicks()
                    .ignitedByLava()));

    /**
     * Village Dimension feast block on the exact shipped pattern: reuses
     * FeastCenterpieceBlock (4 servings + leftovers, foodHeight 8 like
     * rex_roast). The serving item pot_roast_cut belongs to the items agent's
     * territory, so it is resolved lazily by registry id instead of a class
     * reference - keeps this unit compiling standalone; by the time a serving
     * can be taken, registration is long done. (ITEM is a DefaultedRegistry:
     * if the item were somehow absent the supplier hands back minecraft:air
     * and FeastBlock serves nothing - degraded, never crashing.)
     */
    public static final DeferredBlock<Block> SUNDAY_POT_ROAST = BLOCKS.register("sunday_pot_roast",
            () -> new FeastCenterpieceBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE),
                    AbsurdBlocksInit::potRoastCut, true, 8));

    // === BLOCK ITEMS ===

    public static final DeferredItem<Item> YELLOWCAKE_ITEM = ITEMS.register("yellowcake",
            () -> new BlockItem(YELLOWCAKE.get(), new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> CHEESE_WHEEL_ITEM = ITEMS.register("cheese_wheel",
            () -> new BlockItem(CHEESE_WHEEL.get(), new Item.Properties()));
    public static final DeferredItem<Item> BUTTER_PAT_ITEM = ITEMS.register("butter_pat",
            () -> new BlockItem(BUTTER_PAT.get(), new Item.Properties()));
    public static final DeferredItem<Item> SUNDAY_POT_ROAST_ITEM = ITEMS.register("sunday_pot_roast",
            () -> new BlockItem(SUNDAY_POT_ROAST.get(), new Item.Properties()));

    private static Item potRoastCut() {
        return BuiltInRegistries.ITEM.get(
                ResourceLocation.fromNamespaceAndPath(OreSpawnDelight.MODID, "pot_roast_cut"));
    }

    public static void init(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        modEventBus.addListener(AbsurdBlocksInit::addToCreativeTab);
    }

    private static void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(ModTabs.OSD_TAB.getKey())) {
            event.accept(YELLOWCAKE_ITEM.get());
            event.accept(CHEESE_WHEEL_ITEM.get());
            event.accept(BUTTER_PAT_ITEM.get());
            event.accept(SUNDAY_POT_ROAST_ITEM.get());
        }
    }
}
