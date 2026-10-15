package net.tadacko.tadackosdrinks.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.tadacko.tadackosdrinks.item.ModItems;

public class TrellisBlock extends Block implements SimpleWaterloggedBlock {
    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final EnumProperty<TrellisConnection> DOWN  = EnumProperty.create("down", TrellisConnection.class);
    public static final EnumProperty<TrellisConnection> NORTH = EnumProperty.create("north", TrellisConnection.class);
    public static final EnumProperty<TrellisConnection> SOUTH = EnumProperty.create("south", TrellisConnection.class);
    public static final EnumProperty<TrellisConnection> EAST  = EnumProperty.create("east", TrellisConnection.class);
    public static final EnumProperty<TrellisConnection> WEST  = EnumProperty.create("west", TrellisConnection.class);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private static final VoxelShape CENTER    = Block.box(6,  6,  6,  10, 10, 10);
    private static final VoxelShape ARM_UP    = Block.box(6,  10, 6,  10, 16, 10);
    private static final VoxelShape ARM_DOWN  = Block.box(6,  0,  6,  10, 6,  10);
    private static final VoxelShape ARM_NORTH = Block.box(6,  6,  0,  10, 10, 6);
    private static final VoxelShape ARM_SOUTH = Block.box(6,  6,  10, 10, 10, 16);
    private static final VoxelShape ARM_EAST  = Block.box(10, 6,  6,  16, 10, 10);
    private static final VoxelShape ARM_WEST  = Block.box(0,  6,  6,  6,  10, 10);

    public TrellisBlock(Properties props) {
        super(props);
        // default: no connections
        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(UP, false)
                        .setValue(DOWN, TrellisConnection.NONE)
                        .setValue(NORTH, TrellisConnection.NONE)
                        .setValue(SOUTH, TrellisConnection.NONE)
                        .setValue(EAST, TrellisConnection.NONE)
                        .setValue(WEST, TrellisConnection.NONE)
                        .setValue(WATERLOGGED, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b) { b.add(UP, DOWN, NORTH, SOUTH, EAST, WEST, WATERLOGGED); }

    // onPlace instead of getStateForPlacement because it runs on setBlock
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (level.isClientSide) return;
        BlockState newState = state;
        for (Direction dir : Direction.values()) {
            if (connectsTo(level, pos.relative(dir))) {
                if (dir == Direction.UP) newState = newState.setValue(getProp(dir, false), true);
                else newState = newState.setValue(getProp(dir, false), TrellisConnection.TRELLIS);
            }
            if (dir.getAxis().isHorizontal()) newState = updateWireConnection(newState, level, pos, dir);
        }


        boolean water = level.getFluidState(pos).getType() == Fluids.WATER;
        newState = newState.setValue(WATERLOGGED, water);

        if (newState != state) level.setBlock(pos, newState, Block.UPDATE_ALL); // check prevents looping
    }

    @Override
    public FluidState getFluidState(BlockState state) { return state.getValue(WATERLOGGED) ? Fluids.WATER.defaultFluidState() : super.getFluidState(state); }

    // update only the changed side, leave others alone
    @Override
    public BlockState updateShape(BlockState state, Direction dir, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        BlockState updated = state;

        Property dirProperty = getProp(dir, false);
        if (connectsTo(level, neighborPos)) {
            if (dir == Direction.UP) updated = updated.setValue(dirProperty, true);
            else updated = updated.setValue(dirProperty, TrellisConnection.TRELLIS);
        } else {
            if (dir == Direction.UP) updated = updated.setValue(dirProperty, false);
            else updated = updated.setValue(dirProperty, TrellisConnection.NONE);
        }

        updated = updateWireConnection(updated, level, pos, dir);

        if (dir == Direction.DOWN) {
            Block below = level.getBlockState(pos.below()).getBlock();
            if (below instanceof RopeBlock || below instanceof HopCropBlock) updated = updated.setValue(DOWN, TrellisConnection.OTHER);
        }

        // schedule water tick if waterlogged so flowing works
        if (updated.getValue(WATERLOGGED)) level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));

        return updated;
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        VoxelShape shape = CENTER;

        if (pState.getValue(UP))                                 shape = Shapes.or(shape, ARM_UP);
        if (pState.getValue(DOWN) == TrellisConnection.TRELLIS)  shape = Shapes.or(shape, ARM_DOWN);
        if (pState.getValue(NORTH) == TrellisConnection.TRELLIS) shape = Shapes.or(shape, ARM_NORTH);
        if (pState.getValue(SOUTH) == TrellisConnection.TRELLIS) shape = Shapes.or(shape, ARM_SOUTH);
        if (pState.getValue(WEST) == TrellisConnection.TRELLIS)  shape = Shapes.or(shape, ARM_WEST);
        if (pState.getValue(EAST) == TrellisConnection.TRELLIS)  shape = Shapes.or(shape, ARM_EAST);

        return shape;
    }

    @Override
    public boolean canBeReplaced(BlockState pState, BlockPlaceContext pUseContext) {
        Item item = pUseContext.getItemInHand().getItem();
        return item == ModItems.GRAPE_SEEDS_RED.get() || item == ModItems.GRAPE_SEEDS_WHITE.get() ||
                item == ModBlocks.GRAPE_CROP_RED.get().asItem() || item == ModBlocks.GRAPE_CROP_WHITE.get().asItem();
    }

    public static Property getProp(Direction dir, boolean wire) {
        if (wire) {
            return switch (dir) {
                case UP, DOWN -> null;
                case NORTH -> GrapeCropBlock.WIRE_NORTH;
                case SOUTH -> GrapeCropBlock.WIRE_SOUTH;
                case EAST -> GrapeCropBlock.WIRE_EAST;
                case WEST -> GrapeCropBlock.WIRE_WEST;
            };
        }
        return switch(dir) {
            case UP    -> UP;
            case DOWN  -> DOWN;
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case EAST  -> EAST;
            case WEST  -> WEST;
        };
    }

    private boolean connectsTo(BlockGetter world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return state.isSolid() || state.getBlock() instanceof TrellisBlock || state.getBlock() instanceof GrapeCropBlock;
    }

    public static BlockState updateWireConnection(BlockState state, LevelAccessor level, BlockPos pos, Direction dir) {
        BlockState neighborState = level.getBlockState(pos.relative(dir));
        boolean hasWire = false;

        // Accept facing toward this block OR away from this block
        if (neighborState.getBlock() instanceof TrellisWireBlock) {
            Direction neighborFacing = neighborState.getValue(TrellisWireBlock.FACING);
            if (neighborFacing == dir.getOpposite() || neighborFacing == dir) hasWire = true;
        } else if (neighborState.getBlock() instanceof GrapeWireCropBlock) {
            Direction neighborFacing = neighborState.getValue(GrapeWireCropBlock.FACING);
            if (neighborFacing == dir.getOpposite() || neighborFacing == dir) hasWire = true;
        }

        // this is a mess lol
        BlockState updated = state;
        if (state.getBlock() instanceof TrellisBlock) {
            if (hasWire) updated = updated.setValue(getProp(dir, false), TrellisConnection.OTHER);
        } else {
            if (hasWire) updated = updated.setValue(getProp(dir, true), true);
            else updated = updated.setValue(getProp(dir, true), false);
        }
        return updated;
    }

    public enum TrellisConnection implements StringRepresentable {
        NONE("none"),
        TRELLIS("trellis"),
        OTHER("other");

        private final String name;

        TrellisConnection(String name) { this.name = name; }

        @Override
        public String getSerializedName() { return name; }

        @Override
        public String toString() { return name; }
    }
}
