package dev.overgrown.thaumaturge.block.jar;

import dev.overgrown.thaumaturge.block.jar.entity.JarBlockEntity;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class JarBlock extends BlockWithEntity {

    public static final VoxelShape SHAPE = VoxelShapes.union(
    createCuboidShape(1, 0, 1, 15, 14,15),
    createCuboidShape(3, 14, 3, 13, 16, 13));

    public JarBlock(Settings settings) {
        super(settings);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new JarBlockEntity(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        if (world != null) {
            BlockEntity entity = world.getBlockEntity(pos);
            return entity instanceof JarBlockEntity jarBlockEntity ? jarBlockEntity.comparatorLevel() : 0;
        }
        return 0;
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }
}
