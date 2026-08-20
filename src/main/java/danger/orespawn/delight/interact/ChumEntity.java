package danger.orespawn.delight.interact;

import danger.orespawn.entity.AttackSquid;
import danger.orespawn.entity.Hammerhead;
import danger.orespawn.entity.Kraken;
import danger.orespawn.entity.SeaViper;
import danger.orespawn.entity.Urchin;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * The thrown chum bucket (frozen entity id: chum). Flies like a snowball;
 * the moment it touches water it anchors at the splash point and runs a
 * ~20 second lure loop: every second, every hammerhead / sea_viper /
 * attack_squid / urchin (and, for the first six seconds only, kraken) within
 * 24 blocks clears its target and paths to the splash. Pure decoy - no damage.
 *
 * Water detection happens in tick() rather than onHit() because projectile
 * ray-tracing uses ClipContext.Fluid.NONE (verified against 1.21.1 bytecode:
 * ProjectileUtil.getHitResultOnMoveVector never reports fluid hits), so a
 * thrown bucket simply flies INTO water; onHit only fires on solid ground or
 * the seafloor. Both paths funnel into startChumming().
 *
 * The chumming flag is synched so the client stops simulating ballistics on an
 * anchored bucket, and the whole lure state (flag, elapsed ticks, splash pos)
 * is saved to NBT so a chunk reload resumes the loop instead of leaking a
 * frozen entity.
 */
public class ChumEntity extends ThrowableItemProjectile {

    private static final EntityDataAccessor<Boolean> DATA_CHUMMING =
            SynchedEntityData.defineId(ChumEntity.class, EntityDataSerializers.BOOLEAN);

    /** Ground-fish red. */
    private static final DustParticleOptions CHUM_CLOUD =
            new DustParticleOptions(new Vector3f(0.75F, 0.12F, 0.10F), 1.2F);

    public static final int CHUM_DURATION_TICKS = 400; // ~20s per spec
    private static final int KRAKEN_LURE_TICKS = 120;  // the kraken is only briefly fooled
    private static final double LURE_RADIUS = 24.0;    // ~24 blocks per spec

    private int chumTicks;
    private Vec3 splashPos = Vec3.ZERO;

    public ChumEntity(EntityType<? extends ChumEntity> type, Level level) {
        super(type, level);
    }

    public ChumEntity(Level level, LivingEntity shooter) {
        super(InteractionsInit.CHUM.get(), shooter, level);
    }

    @Override
    protected Item getDefaultItem() {
        return InteractionsInit.CHUM_BUCKET.get();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_CHUMMING, false);
    }

    public boolean isChumming() {
        return this.entityData.get(DATA_CHUMMING);
    }

    @Override
    public void tick() {
        if (isChumming()) {
            // Anchored at the splash: no ballistics, just the lure loop.
            this.baseTick();
            if (!level().isClientSide) {
                serverChumTick();
            }
            return;
        }
        super.tick();
        if (!level().isClientSide && !isRemoved() && !isChumming() && isInWater()) {
            startChumming();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (level().isClientSide || isChumming() || isRemoved()) {
            return;
        }
        if (isInWater() || level().getFluidState(BlockPos.containing(result.getLocation())).is(FluidTags.WATER)) {
            startChumming();
        } else if (level() instanceof ServerLevel serverLevel) {
            // Dry land: an expensive, disgusting splat and nothing else.
            serverLevel.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, getItem()),
                    getX(), getY(), getZ(), 8, 0.15, 0.15, 0.15, 0.05);
            serverLevel.playSound(null, getX(), getY(), getZ(),
                    SoundEvents.SLIME_SQUISH, SoundSource.NEUTRAL, 0.8F, 0.7F);
            discard();
        }
    }

    private void startChumming() {
        this.entityData.set(DATA_CHUMMING, true);
        this.chumTicks = 0;
        this.splashPos = position();
        setDeltaMovement(Vec3.ZERO);
        setNoGravity(true);
        level().playSound(null, getX(), getY(), getZ(),
                SoundEvents.PLAYER_SPLASH, SoundSource.NEUTRAL, 1.0F, 0.7F);
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(CHUM_CLOUD, splashPos.x, splashPos.y, splashPos.z, 40, 1.2, 0.5, 1.2, 0.02);
            serverLevel.sendParticles(ParticleTypes.BUBBLE, splashPos.x, splashPos.y, splashPos.z, 20, 0.8, 0.4, 0.8, 0.1);
        }
    }

    private void serverChumTick() {
        chumTicks++;
        ServerLevel serverLevel = (ServerLevel) level();
        if (chumTicks % 5 == 0) {
            serverLevel.sendParticles(CHUM_CLOUD, splashPos.x, splashPos.y, splashPos.z, 4, 0.9, 0.4, 0.9, 0.01);
        }
        if (chumTicks % 20 == 0) {
            lureSeaMonsters(serverLevel);
        }
        if (chumTicks >= CHUM_DURATION_TICKS) {
            serverLevel.sendParticles(ParticleTypes.BUBBLE, splashPos.x, splashPos.y, splashPos.z, 12, 0.5, 0.3, 0.5, 0.05);
            discard();
        }
    }

    /**
     * Clears targets and paths every listed sea horror to the splash. Runs once
     * a second so freshly aggroed sharks are re-cleared for the full window.
     */
    private void lureSeaMonsters(ServerLevel serverLevel) {
        AABB range = new AABB(splashPos, splashPos).inflate(LURE_RADIUS);
        for (Mob mob : serverLevel.getEntitiesOfClass(Mob.class, range, ChumEntity::isChummable)) {
            if (mob instanceof Kraken && chumTicks > KRAKEN_LURE_TICKS) {
                continue; // "briefly kraken" - after six seconds it loses interest
            }
            mob.setTarget(null);
            mob.getNavigation().moveTo(splashPos.x, splashPos.y, splashPos.z, 1.25);
        }
    }

    private static boolean isChummable(Mob mob) {
        return mob.isAlive() && (mob instanceof Hammerhead || mob instanceof SeaViper
                || mob instanceof AttackSquid || mob instanceof Urchin || mob instanceof Kraken);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Chumming", isChumming());
        tag.putInt("ChumTicks", chumTicks);
        tag.putDouble("SplashX", splashPos.x);
        tag.putDouble("SplashY", splashPos.y);
        tag.putDouble("SplashZ", splashPos.z);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_CHUMMING, tag.getBoolean("Chumming"));
        this.chumTicks = tag.getInt("ChumTicks");
        this.splashPos = new Vec3(tag.getDouble("SplashX"), tag.getDouble("SplashY"), tag.getDouble("SplashZ"));
        if (isChumming()) {
            setNoGravity(true);
        }
    }
}
