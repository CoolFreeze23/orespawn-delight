package danger.orespawn.delight.items;

import danger.orespawn.delight.OreSpawnDelight;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import vectorwing.farmersdelight.common.item.ConsumableItem;

/**
 * All 45 OreSpawn Delight items. Registration mirrors oceansdelight's ODItems;
 * property helpers mirror Farmer's Delight ModItems (foodItem / bowlFoodItem).
 * Bowl dishes follow the FD convention: ConsumableItem + craftRemainder(BOWL) + stacksTo(16).
 * Stick foods follow the FD/ends_delight skewer convention: no stick craftRemainder.
 */
public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(OreSpawnDelight.MODID);

    // === MEATS + BASICS ===

    public static final DeferredItem<Item> RAW_DINO_MEAT = ITEMS.register("raw_dino_meat",
            () -> new Item(foodItem(ModFoods.RAW_DINO_MEAT)));
    public static final DeferredItem<Item> DINO_STEAK = ITEMS.register("dino_steak",
            () -> new Item(foodItem(ModFoods.DINO_STEAK)));
    public static final DeferredItem<Item> MINCED_DINO = ITEMS.register("minced_dino",
            () -> new Item(foodItem(ModFoods.MINCED_DINO)));
    public static final DeferredItem<Item> DINO_PATTY = ITEMS.register("dino_patty",
            () -> new Item(foodItem(ModFoods.DINO_PATTY)));
    public static final DeferredItem<Item> KRAKEN_TENTACLE = ITEMS.register("kraken_tentacle",
            () -> new Item(foodItem(ModFoods.KRAKEN_TENTACLE)));
    public static final DeferredItem<Item> RAW_OSTRICH = ITEMS.register("raw_ostrich",
            () -> new Item(foodItem(ModFoods.RAW_OSTRICH)));
    public static final DeferredItem<Item> COOKED_OSTRICH = ITEMS.register("cooked_ostrich",
            () -> new Item(foodItem(ModFoods.COOKED_OSTRICH)));
    public static final DeferredItem<Item> OSTRICH_EGG = ITEMS.register("ostrich_egg",
            () -> new Item(basicItem().stacksTo(16)));
    public static final DeferredItem<Item> RAW_SHARK_MEAT = ITEMS.register("raw_shark_meat",
            () -> new Item(foodItem(ModFoods.RAW_SHARK_MEAT)));
    public static final DeferredItem<Item> SHARK_STEAK = ITEMS.register("shark_steak",
            () -> new Item(foodItem(ModFoods.SHARK_STEAK)));
    public static final DeferredItem<Item> WHALE_BLUBBER = ITEMS.register("whale_blubber",
            () -> new Item(foodItem(ModFoods.WHALE_BLUBBER)));
    public static final DeferredItem<Item> WHALE_STEAK = ITEMS.register("whale_steak",
            () -> new Item(foodItem(ModFoods.WHALE_STEAK)));
    public static final DeferredItem<Item> RAW_FROG_LEGS = ITEMS.register("raw_frog_legs",
            () -> new Item(foodItem(ModFoods.RAW_FROG_LEGS)));
    public static final DeferredItem<Item> COOKED_FROG_LEGS = ITEMS.register("cooked_frog_legs",
            () -> new Item(foodItem(ModFoods.COOKED_FROG_LEGS)));
    public static final DeferredItem<Item> RAW_FLOUNDER_FILLET = ITEMS.register("raw_flounder_fillet",
            () -> new Item(foodItem(ModFoods.RAW_FLOUNDER_FILLET)));
    public static final DeferredItem<Item> COOKED_FLOUNDER_FILLET = ITEMS.register("cooked_flounder_fillet",
            () -> new Item(foodItem(ModFoods.COOKED_FLOUNDER_FILLET)));
    public static final DeferredItem<Item> SALTED_JERKY = ITEMS.register("salted_jerky",
            () -> new Item(foodItem(ModFoods.SALTED_JERKY)));
    public static final DeferredItem<Item> IRRADIATED_JERKY = ITEMS.register("irradiated_jerky",
            () -> new ConsumableItem(foodItem(ModFoods.IRRADIATED_JERKY), true));

    // === DISHES ===

    public static final DeferredItem<Item> CALAMARI_RINGS = ITEMS.register("calamari_rings",
            () -> new Item(foodItem(ModFoods.CALAMARI_RINGS)));
    public static final DeferredItem<Item> INK_BLACK_PASTA = ITEMS.register("ink_black_pasta",
            () -> new ConsumableItem(foodItem(ModFoods.INK_BLACK_PASTA), true));
    public static final DeferredItem<Item> CRABBY_PATTY_DELUXE = ITEMS.register("crabby_patty_deluxe",
            () -> new Item(foodItem(ModFoods.CRABBY_PATTY_DELUXE)));
    public static final DeferredItem<Item> DINO_STEW = ITEMS.register("dino_stew",
            () -> new ConsumableItem(bowlFoodItem(ModFoods.DINO_STEW), true));
    public static final DeferredItem<Item> FROG_LEG_PLATTER = ITEMS.register("frog_leg_platter",
            () -> new Item(foodItem(ModFoods.FROG_LEG_PLATTER)));
    public static final DeferredItem<Item> BUTTER_SEARED_FLOUNDER = ITEMS.register("butter_seared_flounder",
            () -> new Item(foodItem(ModFoods.BUTTER_SEARED_FLOUNDER)));
    public static final DeferredItem<Item> BUTTERED_SKATE_WING = ITEMS.register("buttered_skate_wing",
            () -> new Item(foodItem(ModFoods.BUTTERED_SKATE_WING)));
    public static final DeferredItem<Item> WHALE_CHOWDER = ITEMS.register("whale_chowder",
            () -> new ConsumableItem(bowlFoodItem(ModFoods.WHALE_CHOWDER), true));
    public static final DeferredItem<Item> SHARK_FIN_SOUP = ITEMS.register("shark_fin_soup",
            () -> new ConsumableItem(bowlFoodItem(ModFoods.SHARK_FIN_SOUP), true));
    public static final DeferredItem<Item> OSTRICH_EGG_OMELETTE = ITEMS.register("ostrich_egg_omelette",
            () -> new Item(foodItem(ModFoods.OSTRICH_EGG_OMELETTE)));
    public static final DeferredItem<Item> CASSOWARY_DRUMSTICK = ITEMS.register("cassowary_drumstick",
            () -> new Item(foodItem(ModFoods.CASSOWARY_DRUMSTICK)));
    public static final DeferredItem<Item> VENISON_MEDALLIONS = ITEMS.register("venison_medallions",
            () -> new ConsumableItem(foodItem(ModFoods.VENISON_MEDALLIONS), true));
    public static final DeferredItem<Item> JELLYFISH_SALAD = ITEMS.register("jellyfish_salad",
            () -> new ConsumableItem(bowlFoodItem(ModFoods.JELLYFISH_SALAD), true));
    public static final DeferredItem<Item> FRIED_STINK_BUG = ITEMS.register("fried_stink_bug",
            () -> new Item(foodItem(ModFoods.FRIED_STINK_BUG)));
    public static final DeferredItem<Item> SEA_VIPER_SKEWER = ITEMS.register("sea_viper_skewer",
            () -> new ConsumableItem(foodItem(ModFoods.SEA_VIPER_SKEWER), true));
    public static final DeferredItem<Item> GRILLED_CHEESE = ITEMS.register("grilled_cheese",
            () -> new ConsumableItem(foodItem(ModFoods.GRILLED_CHEESE), true));
    public static final DeferredItem<Item> MOBZILLA_BURGER = ITEMS.register("mobzilla_burger",
            () -> new ConsumableItem(foodItem(ModFoods.MOBZILLA_BURGER), true));
    public static final DeferredItem<Item> CRAB_ROLL = ITEMS.register("crab_roll",
            () -> new Item(foodItem(ModFoods.CRAB_ROLL)));
    public static final DeferredItem<Item> SHARK_SANDWICH = ITEMS.register("shark_sandwich",
            () -> new Item(foodItem(ModFoods.SHARK_SANDWICH)));
    public static final DeferredItem<Item> PEACOCK_CLUB_SANDWICH = ITEMS.register("peacock_club_sandwich",
            () -> new Item(foodItem(ModFoods.PEACOCK_CLUB_SANDWICH)));
    public static final DeferredItem<Item> DINO_DOG = ITEMS.register("dino_dog",
            () -> new Item(foodItem(ModFoods.DINO_DOG)));
    public static final DeferredItem<Item> KRAKEN_DOG = ITEMS.register("kraken_dog",
            () -> new Item(foodItem(ModFoods.KRAKEN_DOG)));
    public static final DeferredItem<Item> KRAKEN_NIGIRI = ITEMS.register("kraken_nigiri",
            () -> new Item(foodItem(ModFoods.KRAKEN_NIGIRI)));
    public static final DeferredItem<Item> FLOUNDER_ROLL = ITEMS.register("flounder_roll",
            () -> new Item(foodItem(ModFoods.FLOUNDER_ROLL)));
    public static final DeferredItem<Item> IRUKANDJI_ROLL = ITEMS.register("irukandji_roll",
            () -> new ConsumableItem(foodItem(ModFoods.IRUKANDJI_ROLL), true));
    public static final DeferredItem<Item> STRAWBERRY_SHORTCAKE = ITEMS.register("strawberry_shortcake",
            () -> new Item(foodItem(ModFoods.STRAWBERRY_SHORTCAKE)));
    public static final DeferredItem<Item> QUINOA_BOWL = ITEMS.register("quinoa_bowl",
            () -> new ConsumableItem(bowlFoodItem(ModFoods.QUINOA_BOWL), true));

    // === FD-style property helpers ===

    public static Item.Properties basicItem() {
        return new Item.Properties();
    }

    public static Item.Properties foodItem(FoodProperties food) {
        return new Item.Properties().food(food);
    }

    public static Item.Properties bowlFoodItem(FoodProperties food) {
        return new Item.Properties().food(food).craftRemainder(Items.BOWL).stacksTo(16);
    }
}
