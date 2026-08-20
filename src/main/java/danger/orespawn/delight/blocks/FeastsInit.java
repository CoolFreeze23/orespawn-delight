package danger.orespawn.delight.blocks;

import danger.orespawn.delight.OreSpawnDelight;
import danger.orespawn.delight.creativetabs.ModTabs;
import danger.orespawn.delight.items.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Tier 3 feast centerpieces - self-contained registration unit.
 * Blocks follow the verified addon FeastBlock recipe:
 * - block properties mirror FD's own feasts (ofFullCopy(Blocks.CAKE), jar-verified
 *   on ROAST_CHICKEN_BLOCK / HONEY_GLAZED_HAM_BLOCK);
 * - BlockItems are plain BlockItem like oceansdelight's ODItems.fromBlock;
 * - serving items are existing Tier 1/2 foods WITHOUT a container item, so
 *   FeastBlock.takeServing hands out servings bare-handed (jar-verified branch:
 *   the container check is skipped when the serving stack has no crafting remainder).
 *
 * The creative tab is appended via BuildCreativeModeTabContentsEvent (a mod-bus
 * event) targeting our own tab key instead of editing ModTabs.
 */
public class FeastsInit {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(OreSpawnDelight.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(OreSpawnDelight.MODID);

    // === BLOCKS ===

    /** Roast T-Rex haunch; each serving hands out a Dino Steak. */
    public static final DeferredBlock<Block> REX_ROAST = BLOCKS.register("rex_roast",
            () -> new FeastCenterpieceBlock(feastProperties(), ModItems.DINO_STEAK, true, 8));

    /** Boss delicacy cooked from Mobzilla's beef hoard; serves Mobzilla Burgers. */
    public static final DeferredBlock<Block> MOBZILLA_FEAST = BLOCKS.register("mobzilla_feast",
            () -> new FeastCenterpieceBlock(feastProperties(), ModItems.MOBZILLA_BURGER, true, 8));

    /** The Queen's spread of medallions, quinoa and strawberries; serves Venison Medallions. */
    public static final DeferredBlock<Block> QUEENS_BANQUET = BLOCKS.register("queens_banquet",
            () -> new FeastCenterpieceBlock(feastProperties(), ModItems.VENISON_MEDALLIONS, true, 5));

    // === BLOCK ITEMS ===

    public static final DeferredItem<Item> REX_ROAST_ITEM = ITEMS.register("rex_roast",
            () -> new BlockItem(REX_ROAST.get(), new Item.Properties()));
    public static final DeferredItem<Item> MOBZILLA_FEAST_ITEM = ITEMS.register("mobzilla_feast",
            () -> new BlockItem(MOBZILLA_FEAST.get(), new Item.Properties()));
    public static final DeferredItem<Item> QUEENS_BANQUET_ITEM = ITEMS.register("queens_banquet",
            () -> new BlockItem(QUEENS_BANQUET.get(), new Item.Properties()));

    private static BlockBehaviour.Properties feastProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE);
    }

    public static void init(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        modEventBus.addListener(FeastsInit::addToCreativeTab);
    }

    private static void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(ModTabs.OSD_TAB.getKey())) {
            event.accept(REX_ROAST_ITEM.get());
            event.accept(MOBZILLA_FEAST_ITEM.get());
            event.accept(QUEENS_BANQUET_ITEM.get());
        }
    }
}
