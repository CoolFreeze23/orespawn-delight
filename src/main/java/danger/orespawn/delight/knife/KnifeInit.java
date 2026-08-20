package danger.orespawn.delight.knife;

import danger.orespawn.delight.OreSpawnDelight;
import danger.orespawn.delight.creativetabs.ModTabs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import vectorwing.farmersdelight.common.item.KnifeItem;

/**
 * Tiger's Eye Knife - self-contained registration (own DeferredRegister + tab event),
 * wired by a single KnifeInit.init(modEventBus) call.
 *
 * The custom Tier follows ends_delight's ModMaterials pattern (anonymous Tier
 * implementation, verified via javap on ends_delight-2.6+neoforge.1.21.1):
 * durability ~250 with speed/damage/enchantability sitting between iron and diamond.
 * Knife construction mirrors FD's own ModItems.knifeItem(Tier) helper:
 * new KnifeItem(tier, props.attributes(KnifeItem.createAttributes(tier, 0.5F, -2.0F))).
 */
public class KnifeInit {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(OreSpawnDelight.MODID);

    /** Banded gold from the Crystal Dimension. Repairs with orespawn:tigers_eye_ingot. */
    public static final Tier TIGERS_EYE = new Tier() {
        @Override
        public int getUses() {
            return 250;
        }

        @Override
        public float getSpeed() {
            return 7.0F;
        }

        @Override
        public float getAttackDamageBonus() {
            return 2.5F;
        }

        @Override
        public TagKey<Block> getIncorrectBlocksForDrops() {
            return BlockTags.INCORRECT_FOR_IRON_TOOL;
        }

        @Override
        public int getEnchantmentValue() {
            return 16;
        }

        @Override
        public Ingredient getRepairIngredient() {
            // orespawn is a hard runtime dependency; lazy registry lookup avoids
            // classloading orespawn registries during our static init.
            return Ingredient.of(BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath("orespawn", "tigers_eye_ingot")));
        }
    };

    public static final DeferredItem<Item> TIGERS_EYE_KNIFE = ITEMS.register("tigers_eye_knife",
            () -> new KnifeItem(TIGERS_EYE, new Item.Properties()
                    .attributes(KnifeItem.createAttributes(TIGERS_EYE, 0.5F, -2.0F))));

    public static void init(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        modEventBus.addListener(KnifeInit::addItemsToTab);
    }

    /** Appends the knife to the OreSpawn Delight tab without touching ModTabs. */
    private static void addItemsToTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(ModTabs.OSD_TAB.getKey())) {
            event.accept(TIGERS_EYE_KNIFE.get());
        }
    }
}
