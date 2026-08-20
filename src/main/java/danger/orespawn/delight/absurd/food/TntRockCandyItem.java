package danger.orespawn.delight.absurd.food;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Rock candy crystallized from TNT rocks. The candy itself is plain food
 * (AbsurdFoods.TNT_ROCK_CANDY, no effects); eating it only arms the burp.
 *
 * CONTRACT (read by the interactions agent's burp tick handler):
 * key {@link #TNT_BURP_AT_KEY} = "TntBurpAt" (long) - absolute game time
 * (level.getGameTime() basis) at which the burp goes off. Written here as
 * now + {@link #BURP_FUSE_TICKS} (40 = the spec's ~2s). When gameTime >=
 * TntBurpAt and the key is present, the handler fires the harmless burp
 * explosion (Level.explode power 1.5, fire=false, ExplosionInteraction.NONE),
 * applies wind_charged 60s + strength I 60s, and removes the key.
 */
public class TntRockCandyItem extends Item {
    public static final String TNT_BURP_AT_KEY = "TntBurpAt";
    public static final int BURP_FUSE_TICKS = 40;

    public TntRockCandyItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide) {
            entity.getPersistentData().putLong(TNT_BURP_AT_KEY, level.getGameTime() + BURP_FUSE_TICKS);
        }
        return result;
    }
}
