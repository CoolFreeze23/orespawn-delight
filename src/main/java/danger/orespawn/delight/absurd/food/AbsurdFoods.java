package danger.orespawn.delight.absurd.food;

import danger.orespawn.delight.items.ModFoods;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

/**
 * FoodProperties for the ABSURD-21 items (spec: ABSURD-21.json). Values follow
 * the ModFoods benchmarks (FD staples); every effect id/duration below is the
 * one written in the spec entry for that item.
 *
 * The Hammy chain foods are NOT static constants: usingConvertsTo(ItemLike)
 * eagerly builds an ItemStack (verified in 1.21.1 FoodProperties.Builder), so a
 * DeferredItem argument would throw during static init. AbsurdItemsInit builds
 * them lazily inside the item suppliers instead, registering the chain
 * bottom-up (hammy_1 first) so each stage's converts-to target already exists.
 */
public class AbsurdFoods {

    // "nutrition slightly above cooked beef" + glowing 15s ("still irradiated from the sear")
    public static final FoodProperties RAY_SEARED_STEAK = new FoodProperties.Builder()
            .nutrition(9).saturationModifier(0.8F)
            .effect(() -> new MobEffectInstance(MobEffects.GLOWING, 300, 0), 1.0F)
            .build();

    // "Red Cow gives you wings": jump_boost II 60s + slow_falling 20s + speed I 60s.
    // Deliberately does NOT clear effects despite being milk. Bottle via usingConvertsTo
    // (Items.GLASS_BOTTLE is vanilla, so the eager ItemStack is safe at static init).
    public static final FoodProperties RED_COW_ENERGY_DRINK = new FoodProperties.Builder()
            .nutrition(2).saturationModifier(0.3F).alwaysEdible()
            .effect(() -> new MobEffectInstance(MobEffects.JUMP, 1200, 1), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.SLOW_FALLING, 400, 0), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1200, 0), 1.0F)
            .usingConvertsTo(Items.GLASS_BOTTLE)
            .build();

    /** One Hammy bite: a full porkchop's nutrition + strength I 10s, converting to the next stage. */
    public static FoodProperties hammyBite(ItemLike convertsTo) {
        return new FoodProperties.Builder()
                .nutrition(8).saturationModifier(0.8F)
                .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 0), 1.0F)
                .usingConvertsTo(convertsTo)
                .build();
    }

    // Color rock candy: item-only sugar rush, speed I 20s
    public static final FoodProperties ROCK_CANDY = new FoodProperties.Builder()
            .nutrition(2).saturationModifier(0.3F).fast()
            .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 400, 0), 1.0F)
            .build();

    // The forbidden flavor. No FoodProperties effects: the delayed burp explosion plus
    // wind_charged 60s + strength I 60s all come from the interactions agent's handler,
    // triggered by the TntRockCandyItem.TNT_BURP_AT_KEY persistent-data tag.
    public static final FoodProperties TNT_ROCK_CANDY = new FoodProperties.Builder()
            .nutrition(2).saturationModifier(0.3F).fast().alwaysEdible()
            .build();

    // Bowl food; fire_resistance 30s. The coal front-burp + the Stinky feeding verb are interactions'.
    public static final FoodProperties BURP_BACK_CHILI = new FoodProperties.Builder()
            .nutrition(10).saturationModifier(0.7F)
            .effect(() -> new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 600, 0), 1.0F)
            .build();

    // Meter-long corn dog on a trex tooth; the T-Rex bribe verb is interactions'.
    public static final FoodProperties DINO_DOG_GRANDE = new FoodProperties.Builder()
            .nutrition(10).saturationModifier(0.8F)
            .build();

    // The gifting behavior (companion cooking, Valentine calm roll) is interactions'.
    public static final FoodProperties HEART_BOX_CHOCOLATES = new FoodProperties.Builder()
            .nutrition(6).saturationModifier(0.6F)
            .build();

    // Apple-cow feed; the milking-for-apples verb is interactions'. Snackable by players.
    public static final FoodProperties CARAMEL_MASH = new FoodProperties.Builder()
            .nutrition(5).saturationModifier(0.5F)
            .build();

    // fire_resistance 5:00 + high nutrition - the proper upgrade over raw lava eel's 60s
    public static final FoodProperties LAVA_EEL_HOTPOT = new FoodProperties.Builder()
            .nutrition(12).saturationModifier(0.8F)
            .effect(() -> new MobEffectInstance(MobEffects.FIRE_RESISTANCE, ModFoods.LONG_DURATION, 0), 1.0F)
            .build();

    // slowness III 0:10 + resistance II 3:00 together: ten seconds of statue-tank stance
    public static final FoodProperties BASILISK_RAMEN = new FoodProperties.Builder()
            .nutrition(12).saturationModifier(0.8F)
            .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 2), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, ModFoods.MEDIUM_DURATION, 1), 1.0F)
            .build();

    // nausea 0:10 + best-in-mod saturation + speed II 2:00; the teleport lives in VortexDangoItem
    public static final FoodProperties VORTEX_DANGO = new FoodProperties.Builder()
            .nutrition(8).saturationModifier(1.2F)
            .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 200, 0), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 2400, 1), 1.0F)
            .build();

    // slow_falling 1:30 + speed I 1:00 - sky-island survival wings
    public static final FoodProperties CLIFF_RACER_HOT_WINGS = new FoodProperties.Builder()
            .nutrition(8).saturationModifier(0.8F)
            .effect(() -> new MobEffectInstance(MobEffects.SLOW_FALLING, 1800, 0), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1200, 0), 1.0F)
            .build();

    // jump_boost IV 90s + slow_falling 90s; the hiccup cost is the HiccupUntil tag
    public static final FoodProperties JUMPY_BUG_GUMMIES = new FoodProperties.Builder()
            .nutrition(4).saturationModifier(0.3F).fast()
            .effect(() -> new MobEffectInstance(MobEffects.JUMP, 1800, 3), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.SLOW_FALLING, 1800, 0), 1.0F)
            .build();

    // levitation 0:05 + slow_falling 0:45 simultaneously - float up ~8 blocks, drift gently down
    public static final FoodProperties FAIRY_FLOSS = new FoodProperties.Builder()
            .nutrition(3).saturationModifier(0.3F).fast()
            .effect(() -> new MobEffectInstance(MobEffects.LEVITATION, 100, 0), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.SLOW_FALLING, 900, 0), 1.0F)
            .build();

    // No builder effects: the exclusive 60/40 branches roll in IrukandjiShooterItem.finishUsingItem
    public static final FoodProperties IRUKANDJI_SHOOTER = new FoodProperties.Builder()
            .nutrition(1).saturationModifier(0.3F).alwaysEdible()
            .usingConvertsTo(Items.GLASS_BOTTLE)
            .build();

    // slow_falling 5:00 + jump_boost II 5:00 + weaving 2:00 (the moth-silk pun)
    public static final FoodProperties LUNA_MOTH_MACARON = new FoodProperties.Builder()
            .nutrition(4).saturationModifier(0.6F).fast()
            .effect(() -> new MobEffectInstance(MobEffects.SLOW_FALLING, ModFoods.LONG_DURATION, 0), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.JUMP, ModFoods.LONG_DURATION, 1), 1.0F)
            .effect(() -> new MobEffectInstance(MobEffects.WEAVING, 2400, 0), 1.0F)
            .build();

    // Sunday Pot Roast serving: solid nutrition + comfort 5:00 (feast block is the blocks agent's)
    public static final FoodProperties POT_ROAST_CUT = new FoodProperties.Builder()
            .nutrition(10).saturationModifier(0.7F)
            .effect(() -> ModFoods.comfort(ModFoods.LONG_DURATION), 1.0F)
            .build();
}
