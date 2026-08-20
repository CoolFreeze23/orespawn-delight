package danger.orespawn.delight.blocks;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import vectorwing.farmersdelight.common.block.FeastBlock;

/**
 * Shared block class for the OreSpawn Delight feast centerpieces.
 * Pattern extracted from oceansdelight's GuardianSoupBlock (jar-verified):
 * extend FD's FeastBlock, override only getShape - a flat tray shape once the
 * feast is down to leftovers (servings=0), a taller joined shape otherwise.
 * All serving/facing/comparator logic is inherited from FeastBlock.
 */
public class FeastCenterpieceBlock extends FeastBlock {

    protected static final VoxelShape TRAY_SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 2.0D, 15.0D);

    protected final VoxelShape fullShape;

    public FeastCenterpieceBlock(BlockBehaviour.Properties properties, Supplier<Item> servingItem,
                                 boolean hasLeftovers, int foodHeight) {
        super(properties, servingItem, hasLeftovers);
        this.fullShape = Shapes.join(TRAY_SHAPE,
                Block.box(2.0D, 2.0D, 2.0D, 14.0D, foodHeight, 14.0D), BooleanOp.OR);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(SERVINGS) == 0 ? TRAY_SHAPE : this.fullShape;
    }
}
