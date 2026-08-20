package danger.orespawn.delight.absurd.blocks;

import java.util.Arrays;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;

/**
 * Yellowcake - a literal uranium cake (ABSURD-21 "Yellowcake").
 *
 * Cake-pattern block: the port's own BlockPizza (jar-verified via javap:
 * IntegerProperty BITES + useWithoutItem eat handler) is itself a mirror of
 * vanilla CakeBlock, so this class copies vanilla CakeBlock 1:1 (1.21.1
 * decompiled source verified: BITES 0..6 = 7 slices, SHAPE_BY_BITE, the
 * client/server dance in useWithoutItem, comparator signal) and adds:
 *
 * - each slice: good saturation (3 / 0.6F, above vanilla cake's 2 / 0.1F)
 *   + minecraft:glowing 60s ("still irradiated");
 * - 3+ slices inside a rolling 30s window: brief minecraft:nausea (the
 *   "acute dose") - tracked with gameTime timestamps in the eating player's
 *   persistent NBT, exactly as the spec asks (no capability, no attachment);
 * - green smoke while placed via {@link #animateTick} (client random display
 *   ticks): tinted dust particles drifting off the top.
 */
public class YellowcakeBlock extends Block {

    public static final int MAX_BITES = 6; // 7 slices, mirrors vanilla CakeBlock.MAX_BITES
    public static final IntegerProperty BITES = BlockStateProperties.BITES; // "bites" 0..6, source-verified

    /** Nutrition per slice; "good saturation" per the spec. */
    public static final int SLICE_NUTRITION = 3;
    public static final float SLICE_SATURATION_MOD = 0.6F;

    public static final int GLOW_TICKS = 1200;         // 60s, spec
    public static final int DOSE_WINDOW_TICKS = 600;   // 30s rolling window, spec
    public static final int ACUTE_DOSE_SLICES = 3;     // 3+ slices => nausea, spec
    public static final int NAUSEA_TICKS = 200;        // 10s = "brief"

    /** Persistent-NBT key holding the recent slice timestamps (gameTime longs). */
    public static final String DOSE_TAG = "orespawn_delight:yellowcake_doses";

    /** Sickly green-yellow "smoke": vanilla dust particle tinted uranium green. */
    private static final DustParticleOptions GREEN_SMOKE =
            new DustParticleOptions(new Vector3f(0.55F, 0.85F, 0.20F), 1.0F);

    // Copied verbatim from vanilla CakeBlock.SHAPE_BY_BITE (source-verified).
    protected static final VoxelShape[] SHAPE_BY_BITE = new VoxelShape[]{
            Block.box(1.0, 0.0, 1.0, 15.0, 8.0, 15.0),
            Block.box(3.0, 0.0, 1.0, 15.0, 8.0, 15.0),
            Block.box(5.0, 0.0, 1.0, 15.0, 8.0, 15.0),
            Block.box(7.0, 0.0, 1.0, 15.0, 8.0, 15.0),
            Block.box(9.0, 0.0, 1.0, 15.0, 8.0, 15.0),
            Block.box(11.0, 0.0, 1.0, 15.0, 8.0, 15.0),
            Block.box(13.0, 0.0, 1.0, 15.0, 8.0, 15.0)
    };

    public YellowcakeBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(BITES, 0));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE_BY_BITE[state.getValue(BITES)];
    }

    // Mirrors CakeBlock.useWithoutItem's client-prediction dance exactly.
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            if (eatSlice(level, pos, state, player).consumesAction()) {
                return InteractionResult.SUCCESS;
            }
            if (player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
                return InteractionResult.CONSUME;
            }
        }
        return eatSlice(level, pos, state, player);
    }

    protected static InteractionResult eatSlice(LevelAccessor level, BlockPos pos, BlockState state, Player player) {
        if (!player.canEat(false)) {
            return InteractionResult.PASS;
        }
        player.awardStat(Stats.EAT_CAKE_SLICE);
        player.getFoodData().eat(SLICE_NUTRITION, SLICE_SATURATION_MOD);
        level.gameEvent(player, GameEvent.EAT, pos);

        if (level instanceof Level realLevel && !realLevel.isClientSide) {
            realLevel.playSound(null, pos, SoundEvents.GENERIC_EAT, SoundSource.BLOCKS, 0.8F, 1.0F);
            player.addEffect(new MobEffectInstance(MobEffects.GLOWING, GLOW_TICKS, 0));
            trackAcuteDose(realLevel, player);
        }

        int bites = state.getValue(BITES);
        if (bites < MAX_BITES) {
            level.setBlock(pos, state.setValue(BITES, bites + 1), 3);
        } else {
            level.removeBlock(pos, false);
            level.gameEvent(player, GameEvent.BLOCK_DESTROY, pos);
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * Rolling-window dose tracker (spec: per-player persistent-data timestamps).
     * Keeps only timestamps newer than 30s; the {@code t <= now} guard discards
     * stale entries carried over from another save whose gameTime ran ahead.
     */
    private static void trackAcuteDose(Level level, Player player) {
        long now = level.getGameTime();
        long cutoff = now - DOSE_WINDOW_TICKS;
        CompoundTag data = player.getPersistentData();
        long[] previous = data.getLongArray(DOSE_TAG);
        long[] kept = new long[previous.length + 1];
        int n = 0;
        for (long t : previous) {
            if (t > cutoff && t <= now) {
                kept[n++] = t;
            }
        }
        kept[n++] = now;
        data.putLongArray(DOSE_TAG, Arrays.copyOf(kept, n));
        if (n >= ACUTE_DOSE_SLICES) {
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, NAUSEA_TICKS, 0));
        }
    }

    /** Green smoke while placed (spec) - client-side random display ticks. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3) == 0) {
            double x = pos.getX() + 0.2 + random.nextDouble() * 0.6;
            double y = pos.getY() + 0.55 + random.nextDouble() * 0.3;
            double z = pos.getZ() + 0.2 + random.nextDouble() * 0.6;
            level.addParticle(GREEN_SMOKE, x, y, z, 0.0, 0.02, 0.0);
        }
    }

    // --- support plumbing copied from vanilla CakeBlock (source-verified) ---

    @Override
    protected BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        return facing == Direction.DOWN && !state.canSurvive(level, currentPos)
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).isSolid();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BITES);
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return (7 - state.getValue(BITES)) * 2;
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }
}
