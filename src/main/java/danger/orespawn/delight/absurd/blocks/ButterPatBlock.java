package danger.orespawn.delight.absurd.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Butter Pat (ABSURD-21 "Butter Pat") - a carpet-height slick of butter.
 *
 * Extends vanilla CarpetBlock (public ctor, source-verified) for the 1/16
 * shape + needs-support behavior.
 *
 * SLIPPERINESS: the block IS registered with ice-tier friction 0.98F
 * (Blocks.ICE verified at friction(0.98F) in the 1.21.1 source), but the
 * spec's "pure block property" plan hits a vanilla wall: LivingEntity.travel
 * samples friction at getOnPos(0.500001F), which for anything standing on a
 * 1/16-tall block floors to the block BELOW the pat (Entity.getOnPos,
 * source-verified). So the property alone is inert at carpet height. The
 * fix kept here is minimal: entityInside (which fires every tick for any
 * entity whose feet are inside the pat's block cell, i.e. anything standing
 * on it) rescales horizontal delta movement by iceFriction/belowFriction, so
 * the net per-tick decay comes out at exactly ice's 0.98 * 0.91. The factor
 * is clamped and only applied when it exceeds 1, so the pat can never ADD
 * energy - butter on ice is just ice.
 *
 * MELTING (spec: "despawns in the Nether or next to fire"):
 * - onPlace / neighborChanged: if hot, schedule a melt tick 2s out;
 * - randomTick: backstop for heat sources that never fire a neighbor update;
 * - melt = destroyBlock with no drops + extinguish sizzle + smoke puff.
 */
public class ButterPatBlock extends CarpetBlock {

    /** Vanilla ice friction, verified against Blocks.ICE in the 1.21.1 source. */
    public static final float ICE_FRICTION = 0.98F;
    /** Never scale a tick's motion by more than this (guards odd sub-0.5 frictions). */
    private static final double MAX_SLIDE_BOOST = 2.0;
    public static final int MELT_DELAY_TICKS = 40; // butter takes a moment to melt

    public ButterPatBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    // --- the skating rink ---

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!entity.onGround()) {
            return;
        }
        BlockPos below = pos.below();
        float belowFriction = level.getBlockState(below).getFriction(level, below, entity);
        if (belowFriction < ICE_FRICTION && belowFriction > 0.0F) {
            double boost = Math.min(ICE_FRICTION / (double) belowFriction, MAX_SLIDE_BOOST);
            Vec3 motion = entity.getDeltaMovement();
            entity.setDeltaMovement(motion.x * boost, motion.y, motion.z * boost);
        }
    }

    // --- melting ---

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        scheduleMeltIfHot(level, pos);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        scheduleMeltIfHot(level, pos);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (isHot(level, pos)) {
            melt(level, pos);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.is(this) && isHot(level, pos)) {
            melt(level, pos);
        }
    }

    private void scheduleMeltIfHot(Level level, BlockPos pos) {
        if (!level.isClientSide && isHot(level, pos) && !level.getBlockTicks().hasScheduledTick(pos, this)) {
            level.scheduleTick(pos, this, MELT_DELAY_TICKS);
        }
    }

    /** Hot = ultrawarm dimension (the Nether check the spec asks for) or an adjacent heat source. */
    private static boolean isHot(Level level, BlockPos pos) {
        if (level.dimensionType().ultraWarm()) {
            return true;
        }
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            BlockState neighbor = level.getBlockState(neighborPos);
            if (neighbor.is(BlockTags.FIRE)
                    || neighbor.is(Blocks.MAGMA_BLOCK)
                    || CampfireBlock.isLitCampfire(neighbor)
                    || level.getFluidState(neighborPos).is(FluidTags.LAVA)) {
                return true;
            }
        }
        return false;
    }

    private static void melt(ServerLevel level, BlockPos pos) {
        level.destroyBlock(pos, false); // melted butter drops nothing
        level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 1.6F);
        level.sendParticles(ParticleTypes.SMOKE,
                pos.getX() + 0.5, pos.getY() + 0.2, pos.getZ() + 0.5,
                8, 0.25, 0.05, 0.25, 0.01);
    }
}
