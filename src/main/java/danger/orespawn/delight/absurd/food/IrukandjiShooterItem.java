package danger.orespawn.delight.absurd.food;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * The world's most venomous jellyfish, served raw in a brine shot.
 * One exclusive random roll in finishUsingItem (per ABSURD-21 spec):
 * 60% -> strength II + speed II 90s ("survived the sting");
 * 40% -> poison II 10s + nausea 15s + blindness 5s (dinner fights back).
 */
public class IrukandjiShooterItem extends DrinkItem {

    public IrukandjiShooterItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide) {
            if (entity.getRandom().nextFloat() < 0.6F) {
                entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 1800, 1));
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1800, 1));
            } else {
                entity.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 1));
                entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 300, 0));
                entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
            }
        }
        return result;
    }
}
