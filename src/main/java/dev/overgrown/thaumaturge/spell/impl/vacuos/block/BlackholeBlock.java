package dev.overgrown.thaumaturge.spell.impl.vacuos.block;

import dev.overgrown.thaumaturge.block.ModBlockEntities;
import dev.overgrown.thaumaturge.spell.impl.vacuos.entity.BlackholeBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class BlackholeBlock extends Block implements BlockEntityProvider {
    public BlackholeBlock(Settings settings) {
        super(settings);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BlackholeBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (world.isClient) {
            return null;
        }

        if (type == ModBlockEntities.BLACKHOLE) {
            return (world1, pos1, state1, blockEntity) ->
                    BlackholeBlockEntity.tick(world1, pos1, state1, (BlackholeBlockEntity) blockEntity);
        }

        return null;
    }
}