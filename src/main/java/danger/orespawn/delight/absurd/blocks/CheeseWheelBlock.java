package danger.orespawn.delight.absurd.blocks;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
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
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Battle-Tower Cheese Wheel (ABSURD-21 "Battle-Tower Cheese Wheel").
 *
 * Cake-style 6-wedge block (BITES 0..5, the same layout the port's BlockPizza
 * uses - jar-verified via javap) that players can eat, plus the RAT BAIT:
 *
 * MECHANISM CHOICE (per the task's "choose the cheapest reliable mechanism and
 * document"): a self-rescheduling SCHEDULED BLOCK TICK, not a BlockEntity.
 * - onPlace arms a tick 20 ticks out; every tick() re-arms itself while the
 *   wheel survives, so the scan runs once per second.
 * - Pending block ticks are saved with the chunk (vanilla LevelChunkTicks), so
 *   the loop survives chunk unload/reload with no BE registration, no
 *   syncing, and no renderer concerns.
 * - randomTick (properties must enable randomTicks()) is a belt-and-braces
 *   re-arm in case a wheel ever lands in the world without onPlace firing
 *   (e.g. structure placement); the hasScheduledTick guard keeps it idempotent.
 *
 * Each 1s scan: every orespawn:rat (entity id verified against ORESPAWN-IDS
 * .json; class jar-verified as a Monster => Mob) within 12 blocks has its
 * attack target cleared ("rats stay passive while wedges remain") and is
 * pathed to the wheel; a rat standing at the wheel nibbles a wedge roughly
 * every 4 seconds with its own ambient squeak + an eat sound. The last wedge
 * removes the block, ending the truce.
 */
public class CheeseWheelBlock extends Block {

    public static final int MAX_BITES = 5; // 6 wedges: bites 0..5, the 6th nibble removes the block
    public static final IntegerProperty BITES = IntegerProperty.create("bites", 0, 5);

    /** Nutrition when a PLAYER eats a wedge. */
    public static final int WEDGE_NUTRITION = 3;
    public static final float WEDGE_SATURATION_MOD = 0.4F;

    public static final int TICK_INTERVAL = 20;      // scan cadence: 1s
    public static final double LURE_RANGE = 12.0;    // spec: rats within ~12 blocks
    public static final double NIBBLE_DIST_SQ = 4.0; // rat within 2 blocks of the wheel's center
    public static final int NIBBLE_CHANCE = 4;       // 1-in-4 per scan => a wedge every ~4s per visit
    public static final double RAT_WALK_SPEED = 1.1;

    private static final ResourceLocation RAT_ID = ResourceLocation.fromNamespaceAndPath("orespawn", "rat");
    @Nullable
    private static EntityType<?> cachedRatType;
    private static boolean ratLookupDone;

    // Same west-to-east slicing as the port's pizza models (slice N starts at x = 1 + 2N).
    protected static final VoxelShape[] SHAPE_BY_BITE = new VoxelShape[]{
            Block.box(1.0, 0.0, 1.0, 15.0, 5.0, 15.0),
            Block.box(3.0, 0.0, 1.0, 15.0, 5.0, 15.0),
            Block.box(5.0, 0.0, 1.0, 15.0, 5.0, 15.0),
            Block.box(7.0, 0.0, 1.0, 15.0, 5.0, 15.0),
            Block.box(9.0, 0.0, 1.0, 15.0, 5.0, 15.0),
            Block.box(11.0, 0.0, 1.0, 15.0, 5.0, 15.0)
    };

    public CheeseWheelBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(BITES, 0));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE_BY_BITE[state.getValue(BITES)];
    }

    // --- player eating (CakeBlock/BlockPizza pattern) ---

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            if (eatWedge(level, pos, state, player).consumesAction()) {
                return InteractionResult.SUCCESS;
            }
            if (player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
                return InteractionResult.CONSUME;
            }
        }
        return eatWedge(level, pos, state, player);
    }

    protected static InteractionResult eatWedge(LevelAccessor level, BlockPos pos, BlockState state, Player player) {
        if (!player.canEat(false)) {
            return InteractionResult.PASS;
        }
        player.getFoodData().eat(WEDGE_NUTRITION, WEDGE_SATURATION_MOD);
        level.gameEvent(player, GameEvent.EAT, pos);
        if (level instanceof Level realLevel && !realLevel.isClientSide) {
            realLevel.playSound(null, pos, SoundEvents.GENERIC_EAT, SoundSource.BLOCKS, 0.8F, 1.0F);
        }
        consumeWedge(level, pos, state);
        if (state.getValue(BITES) >= MAX_BITES) {
            level.gameEvent(player, GameEvent.BLOCK_DESTROY, pos);
        }
        return InteractionResult.SUCCESS;
    }

    /** Advances BITES by one wedge, removing the block after the last one. */
    private static void consumeWedge(LevelAccessor level, BlockPos pos, BlockState state) {
        int bites = state.getValue(BITES);
        if (bites < MAX_BITES) {
            level.setBlock(pos, state.setValue(BITES, bites + 1), 3);
        } else {
            level.removeBlock(pos, false);
        }
    }

    // --- rat bait loop (scheduled-tick mechanism, see class javadoc) ---

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!level.isClientSide && !oldState.is(this) && !level.getBlockTicks().hasScheduledTick(pos, this)) {
            level.scheduleTick(pos, this, TICK_INTERVAL);
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Backstop re-arm only; the real work stays on the 1s scheduled tick.
        if (!level.getBlockTicks().hasScheduledTick(pos, this)) {
            level.scheduleTick(pos, this, TICK_INTERVAL);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.is(this)) {
            return; // replaced since scheduling; let the loop die
        }
        EntityType<?> ratType = ratType();
        if (ratType != null) {
            Vec3 center = Vec3.atCenterOf(pos);
            List<Mob> rats = level.getEntitiesOfClass(Mob.class, new AABB(pos).inflate(LURE_RANGE),
                    mob -> mob.getType() == ratType && mob.isAlive());
            boolean nibbled = false;
            for (Mob rat : rats) {
                rat.setTarget(null); // passive while wedges remain (spec)
                if (rat.distanceToSqr(center) > NIBBLE_DIST_SQ) {
                    rat.getNavigation().moveTo(center.x, center.y, center.z, RAT_WALK_SPEED);
                } else if (!nibbled && random.nextInt(NIBBLE_CHANCE) == 0) {
                    nibbled = true; // at most one wedge per scan, however big the swarm
                    rat.getNavigation().stop();
                    rat.playAmbientSound(); // the rat's actual squeak (public on Mob, source-verified)
                    level.playSound(null, pos, SoundEvents.GENERIC_EAT, SoundSource.BLOCKS, 0.7F, 1.2F);
                    boolean lastWedge = state.getValue(BITES) >= MAX_BITES;
                    consumeWedge(level, pos, state);
                    if (lastWedge) {
                        level.gameEvent(rat, GameEvent.BLOCK_DESTROY, pos);
                        return; // wheel is gone - no reschedule, truce over
                    }
                    state = level.getBlockState(pos);
                }
            }
        }
        level.scheduleTick(pos, this, TICK_INTERVAL);
    }

    /**
     * Registry lookup by id keeps this compilable without the orespawn jar and
     * safe under Connector remapping. ENTITY_TYPE is a DefaultedRegistry (falls
     * back to pig!), so containsKey MUST gate the get.
     */
    @Nullable
    private static EntityType<?> ratType() {
        if (!ratLookupDone) {
            cachedRatType = BuiltInRegistries.ENTITY_TYPE.containsKey(RAT_ID)
                    ? BuiltInRegistries.ENTITY_TYPE.get(RAT_ID)
                    : null;
            ratLookupDone = true;
        }
        return cachedRatType;
    }

    // --- support plumbing (CakeBlock pattern) ---

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
}
