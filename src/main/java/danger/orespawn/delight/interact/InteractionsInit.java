package danger.orespawn.delight.interact;

import danger.orespawn.delight.OreSpawnDelight;
import danger.orespawn.delight.creativetabs.ModTabs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Absurd-tier "living world" layer - self-contained registration unit
 * (parent wires a single {@code InteractionsInit.init(modEventBus)} call).
 *
 * Registers:
 * - the chum_bucket item (snowball-style throwable, frozen id) and the chum
 *   thrown-projectile entity it launches;
 * - every gameplay event handler of the interactions layer (laser-seared steak,
 *   TNT candy burp, Stinky chili, dino truces, companion chocolates, caramel
 *   mash, hiccups) - all on the game bus, all defensive (see
 *   {@link InteractionsHandler}).
 *
 * Items this layer REACTS to (ray_seared_steak, tnt_rock_candy, burp_back_chili,
 * dino_dog_grande, heart_box_chocolates, caramel_mash, jumpy_bug_gummies) are
 * registered by the items module; the handler resolves them from the item
 * registry by frozen id at event time so this unit compiles and runs even while
 * that module is still landing.
 */
public class InteractionsInit {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(OreSpawnDelight.MODID);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, OreSpawnDelight.MODID);

    /** Frozen id: chum_bucket. Thrown whole, bucket and all - that is the joke. */
    public static final DeferredItem<Item> CHUM_BUCKET = ITEMS.register("chum_bucket",
            () -> new ChumBucketItem(new Item.Properties().stacksTo(16)));

    /** Frozen id: chum. Snowball-sized thrown projectile; sized/tracked like vanilla Snowball. */
    public static final DeferredHolder<EntityType<?>, EntityType<ChumEntity>> CHUM =
            ENTITY_TYPES.register("chum",
                    () -> EntityType.Builder.<ChumEntity>of(ChumEntity::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build("chum"));

    public static void init(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        ENTITY_TYPES.register(modEventBus);
        modEventBus.addListener(InteractionsInit::addToCreativeTab);
        if (FMLEnvironment.dist.isClient()) {
            // Client-only class is referenced strictly behind the dist check so a
            // dedicated server never classloads renderer code.
            modEventBus.addListener(InteractionsClient::onRegisterRenderers);
        }

        // Gameplay events live on the game bus, not the mod bus.
        NeoForge.EVENT_BUS.addListener(InteractionsHandler::onProjectileImpact);
        NeoForge.EVENT_BUS.addListener(InteractionsHandler::onLivingDrops);
        NeoForge.EVENT_BUS.addListener(InteractionsHandler::onFinishEat);
        NeoForge.EVENT_BUS.addListener(InteractionsHandler::onEntityInteract);
        NeoForge.EVENT_BUS.addListener(InteractionsHandler::onLivingChangeTarget);
        NeoForge.EVENT_BUS.addListener(InteractionsHandler::onEntityTick);
        NeoForge.EVENT_BUS.addListener(InteractionsHandler::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(InteractionsHandler::onServerTick);
        NeoForge.EVENT_BUS.addListener(InteractionsHandler::onServerStopped);
    }

    private static void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(ModTabs.OSD_TAB.getKey())) {
            event.accept(CHUM_BUCKET.get());
        }
    }
}
