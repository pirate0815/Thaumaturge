package dev.overgrown.thaumaturge.block.faucet.entity;

import dev.overgrown.thaumaturge.block.api.AspectContainer;
import dev.overgrown.thaumaturge.block.faucet.FaucetBlock;
import dev.overgrown.thaumaturge.registry.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class FaucetBlockEntity extends BlockEntity {

    private static class DummyRaycastEntity extends Entity {

        public static final DummyRaycastEntity INSTANCE = new DummyRaycastEntity();

        public DummyRaycastEntity() {
            super(EntityType.MARKER, null);
            this.setPos(0.0,-Double.MAX_VALUE,0.0);
        }

        @Override
        public boolean isDescending() {
            return false;
        }

        @Override
        protected void initDataTracker() {
        }

        @Override
        protected void readCustomDataFromNbt(NbtCompound nbt) {
            throw new AssertionError();
        }

        @Override
        protected void writeCustomDataToNbt(NbtCompound nbt) {
            throw new AssertionError();
        }
    }

    private static final int MAX_TRANSFER_AMOUNT = 4;
    private BlockPos target;
    private Long tickOffset;


    public FaucetBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.FAUCET_BLOCK_ENTITY, pos, state);
        tickOffset = null;
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, FaucetBlockEntity blockEntity) {
        if (blockEntity.tickOffset == null) {blockEntity.tickOffset = world.getTime() % 20;}

        if (world.getTime() % 20 == blockEntity.tickOffset) {
            if (blockEntity.target != null) {
                BlockEntity targetEntity = world.getBlockEntity(blockEntity.target);
                if (targetEntity instanceof AspectContainer target) {
                    BlockEntity sourceEntity = world.getBlockEntity(pos.offset(state.get(FaucetBlock.FACING)));
                    if (sourceEntity instanceof AspectContainer source) {

                        RaycastContext context = new RaycastContext(convert(pos), convert(blockEntity.target), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.ANY, DummyRaycastEntity.INSTANCE);
                        BlockHitResult blockHitResult = world.raycast(context);

                        if (blockHitResult.getType() == HitResult.Type.ENTITY) {
                            return;
                        }
                        if (blockHitResult.getType() == HitResult.Type.BLOCK) {
                            if (!blockHitResult.getBlockPos().equals(blockEntity.target)) {
                                return;
                            }
                        }

                        Map<String, Integer> map = source.getAspects();
                        if (map.isEmpty()) return;

                        List<String> aspects = new ArrayList<>(map.keySet());
                        Collections.shuffle(aspects);

                        for (String aspect : aspects) {
                            int amount = Math.min(source.getRemovableAspectCount(aspect), MAX_TRANSFER_AMOUNT);
                            amount = target.addAditionalAspect(aspect, amount);
                            if (amount > 0) {
                                source.removeAspect(aspect, amount);
                                break;
                            }
                        }
                    }
                }
            }
        }

    }

    private static Vec3d convert(BlockPos pos) {
        return new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    }

    public void setTarget(BlockPos target) {
        this.target = target;
        this.markDirty();
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        if (target != null) {
            nbt.putLong("target", target.asLong());
        }
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.contains("target")) {
            target = BlockPos.fromLong(nbt.getLong("target"));
        }
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
}
