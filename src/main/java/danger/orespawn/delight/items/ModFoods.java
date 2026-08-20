package danger.orespawn.delight.items;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import vectorwing.farmersdelight.common.registry.ModEffects;

/**
 * Food values benchmarked against Farmer's Delight staples
 * (FoodValues: minced 2/0.3, patty 4/0.8, sandwiches 8-11/0.8, rolls 7/0.6,
 * barbecue stick 8/0.9, bowl meals 12/0.8 + Nourishment, big plates 14/0.75).
 * Effect wiring copies the FD/oceansdelight Supplier-based .effect() pattern.
 */
public class ModFoods {
    // FD FoodValues durations (ticks)
    public static final int BRIEF_DURATION = 600;   // 30 seconds
    public static final int SHORT_DURATION = 1200;  // 1 minute
    public static final int MEDIUM_DURATION = 3600; // 3 minutes
    public static final int LONG_DURATION = 6000;   // 5 minutes

    // === MEATS + BASICS ===

    // Dino cuts mirror the vanilla beef family (beef 3/0.3 -> steak 8/0.8)
    public static final FoodProperties RAW_DINO_MEAT = new FoodProperties.Builder()
            .nutrition(3).saturationModifier(0.3F).build();

    public static final FoodProperties DINO_STEAK = new FoodProperties.Builder()
            .nutrition(8).saturationModifier(0.8F).build();

    // Mirrors FD MINCED_BEEF (2/0.3)
    public static final FoodProperties MINCED_DINO = new FoodProperties.Builder()
            .nutrition(2).saturationModifier(0.3F).build();

    // Mirrors FD BEEF_PATTY (4/0.8)
    public static final FoodProperties DINO_PATTY = new FoodProperties.Builder()
            .nutrition(4).saturationModifier(0.8F).build();

    // Raw seafood slice tier (FD COD_SLICE 1/0.1, raw fish 2/0.1)
    public static final FoodProperties KRAKEN_TENTACLE = new FoodProperties.Builder()
            .nutrition(2).saturationModifier(0.1F).build();

    // Mirrors vanilla raw chicken (2/0.3, 30% Hunger)
    public static final FoodProperties RAW_OSTRICH = new FoodProperties.Builder()
            .nutrition(2).saturationModifier(0.3F)
            .effect(() -> new MobEffectInstance(MobEffects.HUNGER, 600, 0), 0.3F)
            .build();

    // Mirrors vanilla cooked chicken (6/0.6)
    public static final FoodProperties COOKED_OSTRICH = new FoodProperties.Builder()
            .nutrition(6).saturationModifier(0.6F).build();

    // Shark mirrors the porkchop/beef staples, a hair leaner
    public static final FoodProperties RAW_SHARK_MEAT = new FoodProperties.Builder()
            .nutrition(3).saturationModifier(0.3F).build();

    public static final FoodProperties SHARK_STEAK = new FoodProperties.Builder()
            .nutrition(7).saturationModifier(0.8F).build();

    // Fatty but chewy on its own
    public static final FoodProperties WHALE_BLUBBER = new FoodProperties.Builder()
            .nutrition(3).saturationModifier(0.6F).build();

    public static final FoodProperties WHALE_STEAK = new FoodProperties.Builder()
            .nutrition(8).saturationModifier(0.8F).build();

    // Small delicacy (raw rabbit tier)
    public static final FoodProperties RAW_FROG_LEGS = new FoodProperties.Builder()
            .nutrition(1).saturationModifier(0.2F).build();

    public static final FoodProperties COOKED_FROG_LEGS = new FoodProperties.Builder()
            .nutrition(4).saturationModifier(0.6F).build();

    // Mirrors FD COD_SLICE (1/0.1) -> COOKED_COD_SLICE (3/0.5)
    public static final FoodProperties RAW_FLOUNDER_FILLET = new FoodProperties.Builder()
            .nutrition(1).saturationModifier(0.1F).build();

    public static final FoodProperties COOKED_FLOUNDER_FILLET = new FoodProperties.Builder()
            .nutrition(3).saturationModifier(0.5F).build();

    // Trail snack, eats quick
    public static final FoodProperties SALTED_JERKY = new FoodProperties.Builder()
            .nutrition(4).saturationModifier(0.6F).fast().build();

    // Glowing 60s + a small kick of Regeneration; always edible so the glow is on demand
    public static final FoodProperties IRRADIATED_JERKY = new FoodProperties.Builder()
            .nutrition(4).saturationModifier(0.6F).fast().alwaysEdible()
            .effect(() -> new MobEffectInstance(MobEffects.GLOWING, SHORT_DURATION, 0), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 100, 0), 1.0F)
            .build();

    // === DISHES ===

    // Mirrors oceansdelight squid rings (5/0.5)
    public static final FoodProperties CALAMARI_RINGS = new FoodProperties.Builder()
            .nutrition(5).saturationModifier(0.5F).build();

    // Plated pasta meal tier (FD pasta dishes 12/0.8 + Nourishment)
    public static final FoodProperties INK_BLACK_PASTA = new FoodProperties.Builder()
            .nutrition(12).saturationModifier(0.8F)
            .effect(() -> nourishment(MEDIUM_DURATION), 1.0F)
            .build();

    // A burger worthy of the name, one rung under the Mobzilla Burger
    public static final FoodProperties CRABBY_PATTY_DELUXE = new FoodProperties.Builder()
            .nutrition(10).saturationModifier(0.8F).build();

    // Bowl meal tier (FD BEEF_STEW 12/0.8 + Nourishment)
    public static final FoodProperties DINO_STEW = new FoodProperties.Builder()
            .nutrition(12).saturationModifier(0.8F)
            .effect(() -> nourishment(MEDIUM_DURATION), 1.0F)
            .build();

    public static final FoodProperties FROG_LEG_PLATTER = new FoodProperties.Builder()
            .nutrition(9).saturationModifier(0.7F).build();

    public static final FoodProperties BUTTER_SEARED_FLOUNDER = new FoodProperties.Builder()
            .nutrition(9).saturationModifier(0.8F).build();

    public static final FoodProperties BUTTERED_SKATE_WING = new FoodProperties.Builder()
            .nutrition(10).saturationModifier(0.8F).build();

    // Warm bowls get Comfort
    public static final FoodProperties WHALE_CHOWDER = new FoodProperties.Builder()
            .nutrition(12).saturationModifier(0.8F)
            .effect(() -> comfort(MEDIUM_DURATION), 1.0F)
            .build();

    public static final FoodProperties SHARK_FIN_SOUP = new FoodProperties.Builder()
            .nutrition(12).saturationModifier(0.8F)
            .effect(() -> comfort(MEDIUM_DURATION), 1.0F)
            .build();

    // Mirrors FD BACON_AND_EGGS breakfast tier (10/0.6)
    public static final FoodProperties OSTRICH_EGG_OMELETTE = new FoodProperties.Builder()
            .nutrition(9).saturationModifier(0.6F).build();

    public static final FoodProperties CASSOWARY_DRUMSTICK = new FoodProperties.Builder()
            .nutrition(7).saturationModifier(0.7F).build();

    public static final FoodProperties VENISON_MEDALLIONS = new FoodProperties.Builder()
            .nutrition(10).saturationModifier(0.8F)
            .effect(() -> nourishment(SHORT_DURATION), 1.0F)
            .build();

    // Brief Slow Falling - it wobbles all the way down
    public static final FoodProperties JELLYFISH_SALAD = new FoodProperties.Builder()
            .nutrition(7).saturationModifier(0.6F)
            .effect(() -> new MobEffectInstance(MobEffects.SLOW_FALLING, BRIEF_DURATION, 0), 1.0F)
            .build();

    public static final FoodProperties FRIED_STINK_BUG = new FoodProperties.Builder()
            .nutrition(3).saturationModifier(0.4F).fast().build();

    // Skewer tier (FD BARBECUE_STICK 8/0.9); venom kick: Strength, with a 30% brief Poison nip
    public static final FoodProperties SEA_VIPER_SKEWER = new FoodProperties.Builder()
            .nutrition(8).saturationModifier(0.9F)
            .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, SHORT_DURATION, 0), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.POISON, 100, 0), 0.3F)
            .build();

    public static final FoodProperties GRILLED_CHEESE = new FoodProperties.Builder()
            .nutrition(8).saturationModifier(0.8F)
            .effect(() -> comfort(SHORT_DURATION), 1.0F)
            .build();

    // Flagship burger (FD HAMBURGER 11/0.8, ours tops out the dish tier)
    public static final FoodProperties MOBZILLA_BURGER = new FoodProperties.Builder()
            .nutrition(12).saturationModifier(0.8F)
            .effect(() -> nourishment(MEDIUM_DURATION), 1.0F)
            .build();

    // Rolls mirror FD SALMON_ROLL / COD_ROLL (7/0.6)
    public static final FoodProperties CRAB_ROLL = new FoodProperties.Builder()
            .nutrition(7).saturationModifier(0.6F).build();

    public static final FoodProperties SHARK_SANDWICH = new FoodProperties.Builder()
            .nutrition(9).saturationModifier(0.8F).build();

    // Mirrors FD CHICKEN_SANDWICH (10/0.8)
    public static final FoodProperties PEACOCK_CLUB_SANDWICH = new FoodProperties.Builder()
            .nutrition(10).saturationModifier(0.8F).build();

    public static final FoodProperties DINO_DOG = new FoodProperties.Builder()
            .nutrition(8).saturationModifier(0.8F).build();

    public static final FoodProperties KRAKEN_DOG = new FoodProperties.Builder()
            .nutrition(8).saturationModifier(0.8F).build();

    public static final FoodProperties KRAKEN_NIGIRI = new FoodProperties.Builder()
            .nutrition(4).saturationModifier(0.6F).fast().build();

    public static final FoodProperties FLOUNDER_ROLL = new FoodProperties.Builder()
            .nutrition(7).saturationModifier(0.6F).build();

    // Named for the jellyfish; 30% brief Poison, in the FD NETHER_SALAD risk tradition
    public static final FoodProperties IRUKANDJI_ROLL = new FoodProperties.Builder()
            .nutrition(6).saturationModifier(0.6F)
            .effect(() -> new MobEffectInstance(MobEffects.POISON, 100, 0), 0.3F)
            .build();

    public static final FoodProperties STRAWBERRY_SHORTCAKE = new FoodProperties.Builder()
            .nutrition(7).saturationModifier(0.6F).build();

    // Healthy bowl
    public static final FoodProperties QUINOA_BOWL = new FoodProperties.Builder()
            .nutrition(8).saturationModifier(0.7F)
            .effect(() -> nourishment(SHORT_DURATION), 1.0F)
            .build();

    // FD-style helpers (FoodValues.nourishment mirrored; comfort built the same way)
    public static MobEffectInstance comfort(int duration) {
        return new MobEffectInstance(ModEffects.COMFORT, duration, 0, false, false);
    }

    public static MobEffectInstance nourishment(int duration) {
        return new MobEffectInstance(ModEffects.NOURISHMENT, duration, 0, false, false);
    }
}
