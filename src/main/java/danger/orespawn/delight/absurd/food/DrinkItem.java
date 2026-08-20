package danger.orespawn.delight.absurd.food;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;

/**
 * Minimal vanilla-path drink (potion-style animation/sound/duration).
 *
 * Deliberately NOT Farmer's Delight's DrinkableItem: FD's ConsumableItem.finishUsingItem
 * discards the ItemStack returned by Player.eat (javap-verified on FarmersDelight
 * 1.21.1-1.3.2), and in 1.21.1 that return value is exactly where
 * FoodProperties.usingConvertsTo hands back the empty bottle when the drunk stack
 * empties. Extending Item keeps the vanilla eat path intact, so the spec's
 * "bottle returned via usingConvertsTo" works for the last drink in the stack too.
 */
public class DrinkItem extends Item {
    public static final int DRINK_DURATION_TICKS = 32;

    public DrinkItem(Properties properties) {
        super(properties);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return DRINK_DURATION_TICKS;
    }

    @Override
    public SoundEvent getDrinkingSound() {
        return SoundEvents.GENERIC_DRINK;
    }

    @Override
    public SoundEvent getEatingSound() {
        return SoundEvents.GENERIC_DRINK;
    }
}
