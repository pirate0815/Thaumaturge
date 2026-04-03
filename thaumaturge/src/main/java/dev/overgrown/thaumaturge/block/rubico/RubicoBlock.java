package dev.overgrown.thaumaturge.block.rubico;

import dev.overgrown.aspectslib.aether.AetherChunkData;
import dev.overgrown.aspectslib.aether.AetherManager;
import dev.overgrown.thaumaturge.util.Corruption;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.Random;

import java.util.Optional;

public class RubicoBlock extends Block {
    public RubicoBlock(Settings settings) {
        super(settings);
    }

    @Override
    public boolean hasRandomTicks(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {

        AetherChunkData aetherChunkData = AetherManager.getAetherData(world, new ChunkPos(pos));
        var list = aetherChunkData.getAspectIds().stream().toList();
        if (!list.isEmpty()) {
            int index = random.nextInt(list.size());
            Identifier aspect = list.get(index);
            if (!aspect.equals(Corruption.VITIUM_ID)) {
                aetherChunkData.modifyAspectLevel(aspect, -1);
            }
        }

        if (random.nextInt(5) == 0) {
            BlockPos targetPos = pos.add(random.nextBetweenExclusive(-2, 2), random.nextBetweenExclusive(-2, 2), random.nextBetweenExclusive(-2, 2));
            if (world.isChunkLoaded(new ChunkPos(targetPos).toLong())) {
                BlockState targetBlockState = world.getBlockState(targetPos);
                Optional<BlockState> futureBlockState = Corruption.getCorruptedVersion(targetBlockState);
                futureBlockState.ifPresent(blockState -> world.setBlockState(targetPos, blockState));
            }
        }
    }
}
