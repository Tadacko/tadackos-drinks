package net.tadacko.tadackosdrinks.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.tadacko.tadackosdrinks.item.ModItems;

public class TrellisWireBlock extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final int SUPPORT_CHECK_DELAY = 2; // ticks to wait before verifying support

    public TrellisWireBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        boolean east = isConnectorFacing(level, pos.east(), Direction.WEST);
        boolean west = isConnectorFacing(level, pos.west(), Direction.EAST);
        boolean north = isConnectorFacing(level, pos.north(), Direction.SOUTH);
        boolean south = isConnectorFacing(level, pos.south(), Direction.NORTH);

        // survive if connected on both sides along X (east+west) OR along Z (north+south)
        return (east && west) || (north && south);
    }

    // Destroy when supports removed
    @Override
    public BlockState updateShape(BlockState state, Direction dir, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (dir.getAxis().isHorizontal()) level.scheduleTick(pos, this, SUPPORT_CHECK_DELAY);
        return super.updateShape(state, dir, neighborState, level, pos, neighborPos);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH, SOUTH -> Block.box(6.0D, 6.0D, 0.0D, 10.0D, 10.0D, 16.0D);
            case EAST, WEST -> Block.box(0.0D, 6.0D, 6.0D, 16.0D, 10.0D, 10.0D);
            default -> Block.box(6.0D, 6.0D, 6.0D, 10.0D, 10.0D, 10.0D);
        };
    }

    @Override
    public boolean canBeReplaced(BlockState pState, BlockPlaceContext pUseContext) {
        Item item = pUseContext.getItemInHand().getItem();
        return item == ModItems.HOP_SEEDS.get() || item == ModBlocks.HOP_CROP.get().asItem();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING); }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand) {
        if (!this.canSurvive(state, level, pos)) level.destroyBlock(pos, true);
    }

    @Override
    public Item asItem() { return ModItems.TRELLIS_WIRE_ITEM.get(); }

    public static boolean isConnectorFacing(BlockGetter world, BlockPos checkPos, Direction requiredFacing) {
        BlockState state = world.getBlockState(checkPos);

        if (state.getBlock() instanceof GrapeCropBlock || state.getBlock() instanceof TrellisBlock) return true;

        if (state.getBlock() instanceof TrellisWireBlock) {
            Direction neighborFacing = state.getValue(TrellisWireBlock.FACING);
            return neighborFacing == requiredFacing || neighborFacing == requiredFacing.getOpposite();
        } else if (state.getBlock() instanceof GrapeWireCropBlock) {
            Direction neighborFacing = state.getValue(GrapeWireCropBlock.FACING);
            return neighborFacing == requiredFacing || neighborFacing == requiredFacing.getOpposite();
        }

        return false;
    }
}
