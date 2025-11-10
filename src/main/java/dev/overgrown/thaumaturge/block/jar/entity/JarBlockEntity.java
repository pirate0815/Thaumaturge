package dev.overgrown.thaumaturge.block.jar.entity;

import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.block.api.AspectContainer;
import dev.overgrown.thaumaturge.registry.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class JarBlockEntity extends BlockEntity implements AspectContainer {

    private static final int MAX_ASPECT_LEVEL = 250;

    private @Nullable Identifier aspect;
    private int level;

    public JarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.JAR_BLOCK_ENTITY, pos, state);
        aspect = null;
        level = 0;
    }


    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        if (aspect != null) {
            nbt.putString("Aspect", aspect.toString());
        }
        nbt.putInt("Level", level);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        aspect = nbt.contains("Aspect") ? Identifier.tryParse(nbt.getString("Aspect")) : null;
        if (nbt.contains("Level")) {
            level = nbt.getInt("Level");
        }
    }

    private boolean matchesAspect(Identifier aspect) {
        if (this.aspect != null) {
            return this.aspect.equals(aspect);
        }
        return false;
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound nbt = new NbtCompound();
        this.writeNbt(nbt);
        return nbt;
    }


    @Override
    public Set<Identifier> getAspects() {
        return aspect != null ? Set.of(aspect) : Set.of();
    }

    @Override
    public int getAspectLevel(@NotNull Identifier aspect) {
        if (matchesAspect(aspect)) {
            return level;
        }
        return 0;
    }

    @Override
    public @Nullable Integer getDesiredAspectLeve(@NotNull Identifier aspect) {
        return matchesAspect(aspect) ? MAX_ASPECT_LEVEL : 0;
    }

    @Override
    public boolean canReduceAspectLevels() {
        return true;
    }

    @Override
    public int getReducibleAspectLevel(@NotNull Identifier aspect) {
        return getAspectLevel(aspect);
    }

    @Override
    public void reduceAspectLevel(@NotNull Identifier aspect, int amount) {
        if (matchesAspect(aspect)) {
            level = level - amount;
            if (level <= 0) {
                level = 0;
                this.aspect = null;
                Thaumaturge.LOGGER.info("unset jar aspect");
            }
            syncToClient();
            markDirty();
        }
    }

    @Override
    public int increaseAspectLevel(@NotNull Identifier aspect, int amount) {
        if(this.aspect == null) {
            this.aspect = aspect;
            this.level = Math.min(amount, MAX_ASPECT_LEVEL);
            syncToClient();
            markDirty();
            return this.level;
        } else {
            if (this.aspect.equals(aspect)) {
                int newLevel = Math.min(this.level + amount, MAX_ASPECT_LEVEL);
                int change = newLevel - this.level;
                this.level = newLevel;
                syncToClient();
                markDirty();
                return change;
            }
        }
        return 0;
    }

    private void syncToClient() {
        if (world != null && !world.isClient) {
            world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_LISTENERS);
        }
    }

    public int comparatorLevel() {
        return (level*15) / MAX_ASPECT_LEVEL;
    }

    public int getLevel() {
        return level;
    }

    public int getMaxLevel() {
        return MAX_ASPECT_LEVEL;
    }
}
