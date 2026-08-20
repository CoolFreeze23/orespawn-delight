package danger.orespawn.delight.absurd.food;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import vectorwing.farmersdelight.common.item.ConsumableItem;

/**
 * Three glazed vortex eyes on a skewer, still faintly rotating. You ate the eye
 * of a teleporting mob; you teleport. Saturation/nausea/speed come from
 * AbsurdFoods.VORTEX_DANGO; the chorus-style random teleport below is copied
 * from 1.21.1 ChorusFruitItem.finishUsingItem (local neoForm source), minus the
 * Fox branch. randomTeleport(.., true) broadcasts entity event 46, which spawns
 * the portal particle cloud client-side, exactly like chorus fruit.
 */
public class VortexDangoItem extends ConsumableItem {

    public VortexDangoItem(Properties properties) {
        super(properties, true);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide) {
            for (int i = 0; i < 16; i++) {
                double d0 = entity.getX() + (entity.getRandom().nextDouble() - 0.5) * 16.0;
                double d1 = Mth.clamp(
                        entity.getY() + (double) (entity.getRandom().nextInt(16) - 8),
                        (double) level.getMinBuildHeight(),
                        (double) (level.getMinBuildHeight() + ((ServerLevel) level).getLogicalHeight() - 1)
                );
                double d2 = entity.getZ() + (entity.getRandom().nextDouble() - 0.5) * 16.0;
                if (entity.isPassenger()) {
                    entity.stopRiding();
                }

                Vec3 vec3 = entity.position();
                EntityTeleportEvent.ChorusFruit event = EventHooks.onChorusFruitTeleport(entity, d0, d1, d2);
                if (event.isCanceled()) {
                    return result;
                }
                if (entity.randomTeleport(event.getTargetX(), event.getTargetY(), event.getTargetZ(), true)) {
                    level.gameEvent(GameEvent.TELEPORT, vec3, GameEvent.Context.of(entity));
                    level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                            SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS);
                    entity.resetFallDistance();
                    break;
                }
            }

            if (entity instanceof Player player) {
                player.resetCurrentImpulseContext();
                player.getCooldowns().addCooldown(this, 20);
            }
        }
        return result;
    }
}
