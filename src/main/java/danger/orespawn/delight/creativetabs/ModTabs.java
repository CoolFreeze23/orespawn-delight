package danger.orespawn.delight.creativetabs;

import danger.orespawn.delight.OreSpawnDelight;
import danger.orespawn.delight.items.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Creative tab registered the addon way - structure copied from oceansdelight's ODTabs
 * (static tab built with the CreativeModeTab.Builder, then registered via supplier).
 */
public class ModTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, OreSpawnDelight.MODID);

    private static final CreativeModeTab FOODS = new CreativeModeTab.Builder(CreativeModeTab.Row.TOP, 9)
            .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
            .title(Component.translatable("itemGroup." + OreSpawnDelight.MODID))
            .icon(() -> new ItemStack(ModItems.MOBZILLA_BURGER.get()))
            .displayItems((parameters, output) -> {
                // Meats + basics, manifest order
                output.accept(ModItems.RAW_DINO_MEAT.get());
                output.accept(ModItems.DINO_STEAK.get());
                output.accept(ModItems.MINCED_DINO.get());
                output.accept(ModItems.DINO_PATTY.get());
                output.accept(ModItems.KRAKEN_TENTACLE.get());
                output.accept(ModItems.RAW_OSTRICH.get());
                output.accept(ModItems.COOKED_OSTRICH.get());
                output.accept(ModItems.OSTRICH_EGG.get());
                output.accept(ModItems.RAW_SHARK_MEAT.get());
                output.accept(ModItems.SHARK_STEAK.get());
                output.accept(ModItems.WHALE_BLUBBER.get());
                output.accept(ModItems.WHALE_STEAK.get());
                output.accept(ModItems.RAW_FROG_LEGS.get());
                output.accept(ModItems.COOKED_FROG_LEGS.get());
                output.accept(ModItems.RAW_FLOUNDER_FILLET.get());
                output.accept(ModItems.COOKED_FLOUNDER_FILLET.get());
                output.accept(ModItems.SALTED_JERKY.get());
                output.accept(ModItems.IRRADIATED_JERKY.get());
                // Dishes, manifest order
                output.accept(ModItems.CALAMARI_RINGS.get());
                output.accept(ModItems.INK_BLACK_PASTA.get());
                output.accept(ModItems.CRABBY_PATTY_DELUXE.get());
                output.accept(ModItems.DINO_STEW.get());
                output.accept(ModItems.FROG_LEG_PLATTER.get());
                output.accept(ModItems.BUTTER_SEARED_FLOUNDER.get());
                output.accept(ModItems.BUTTERED_SKATE_WING.get());
                output.accept(ModItems.WHALE_CHOWDER.get());
                output.accept(ModItems.SHARK_FIN_SOUP.get());
                output.accept(ModItems.OSTRICH_EGG_OMELETTE.get());
                output.accept(ModItems.CASSOWARY_DRUMSTICK.get());
                output.accept(ModItems.VENISON_MEDALLIONS.get());
                output.accept(ModItems.JELLYFISH_SALAD.get());
                output.accept(ModItems.FRIED_STINK_BUG.get());
                output.accept(ModItems.SEA_VIPER_SKEWER.get());
                output.accept(ModItems.GRILLED_CHEESE.get());
                output.accept(ModItems.MOBZILLA_BURGER.get());
                output.accept(ModItems.CRAB_ROLL.get());
                output.accept(ModItems.SHARK_SANDWICH.get());
                output.accept(ModItems.PEACOCK_CLUB_SANDWICH.get());
                output.accept(ModItems.DINO_DOG.get());
                output.accept(ModItems.KRAKEN_DOG.get());
                output.accept(ModItems.KRAKEN_NIGIRI.get());
                output.accept(ModItems.FLOUNDER_ROLL.get());
                output.accept(ModItems.IRUKANDJI_ROLL.get());
                output.accept(ModItems.STRAWBERRY_SHORTCAKE.get());
                output.accept(ModItems.QUINOA_BOWL.get());
            })
            .build();

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> OSD_TAB =
            TABS.register(OreSpawnDelight.MODID, () -> FOODS);
}
