package danger.orespawn.delight.absurd.food;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import vectorwing.farmersdelight.common.item.ConsumableItem;

/**
 * Trooper-bug scales melted with sugar into springy gummies. The superjump
 * buffs (jump_boost IV + slow_falling 90s) live in AbsurdFoods.JUMPY_BUG_GUMMIES;
 * the cost is the hiccups, handed off to the interactions agent via entity
 * persistent data:
 *
 * CONTRACT (read by the interactions agent's player tick handler):
 * key {@link #HICCUP_UNTIL_KEY} = "HiccupUntil" (long) - absolute game time
 * (level.getGameTime() basis) until which the eater has the hiccups. Written
 * here as now + {@link #HICCUP_DURATION_TICKS} (600 = 30s, the spec's "short"
 * tag). While gameTime < HiccupUntil, every 200-300 ticks: hiccup sound + a
 * ~0.35 upward pop. The handler may remove the key once expired.
 */
public class JumpyBugGummiesItem extends ConsumableItem {
    public static final String HICCUP_UNTIL_KEY = "HiccupUntil";
    public static final int HICCUP_DURATION_TICKS = 600;

    public JumpyBugGummiesItem(Properties properties) {
        super(properties, true);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide) {
            entity.getPersistentData().putLong(HICCUP_UNTIL_KEY, level.getGameTime() + HICCUP_DURATION_TICKS);
        }
        return result;
    }
}
