package dev.overgrown.thaumaturge.block.entity;

import dev.overgrown.aspectslib.api.AspectsAPI;
import dev.overgrown.aspectslib.data.AspectData;
import dev.overgrown.thaumaturge.block.VesselBlock;
import dev.overgrown.thaumaturge.registry.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class VesselBlockEntity extends BlockEntity {
    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(1, ItemStack.EMPTY);
    private final Map<String, Integer> aspects = new HashMap<>();

    public VesselBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.VESSEL_BLOCK_ENTITY, pos, state);
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, VesselBlockEntity blockEntity) {
        if (state.get(VesselBlock.BOILING)) {
            if (world.getTime() % 10 == 0) {
                ((ServerWorld) world).spawnParticles(ParticleTypes.BUBBLE_POP,
                        pos.getX() + 0.5, pos.getY() + 0.3, pos.getZ() + 0.5,
                        2, 0.2, 0.0, 0.2, 0.05);
            }
        }
    }

    public boolean addItem(ItemStack stack) {
        if (items.get(0).isEmpty()) {
            items.set(0, stack.copy());
            markDirty();
            return true;
        }
        return false;
    }

    public void processItem() {
        ItemStack stack = items.get(0);
        if (stack.isEmpty()) return;

        World world = getWorld();
        if (world == null) return;

        AspectData aspectData = AspectsAPI.getAspectData(stack);
        if (!aspectData.isEmpty()) {
            // Convert item to aspects
            for (var entry : aspectData.getMap().object2IntEntrySet()) {
                String aspectName = AspectsAPI.getAspect(entry.getKey())
                        .map(aspect -> aspect.name())
                        .orElse(entry.getKey().toString());

                aspects.merge(aspectName, entry.getIntValue(), Integer::sum);
            }
            // Consume some water
            BlockState state = getCachedState();
            int waterLevel = state.get(VesselBlock.WATER_LEVEL);
            if (waterLevel > 0) {
                world.setBlockState(pos, state.with(VesselBlock.WATER_LEVEL, waterLevel - 1));
            }
        } else {
            // Drop item unchanged
            ItemEntity itemEntity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, stack.copy());
            world.spawnEntity(itemEntity);
        }

        items.set(0, ItemStack.EMPTY);
        markDirty();
    }

    public Map<String, Integer> getAspects() {
        return aspects;
    }

    public boolean hasCatalyst(ItemStack catalyst) {
        // Implement catalyst checking logic here
        return false;
    }

    public boolean craftWithCatalyst(ItemStack catalyst) {
        // Implement crafting logic here
        return false;
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, items);

        NbtCompound aspectsNbt = new NbtCompound();
        for (Map.Entry<String, Integer> entry : aspects.entrySet()) {
            aspectsNbt.putInt(entry.getKey(), entry.getValue());
        }
        nbt.put("Aspects", aspectsNbt);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, items);

        aspects.clear();
        NbtCompound aspectsNbt = nbt.getCompound("Aspects");
        for (String key : aspectsNbt.getKeys()) {
            aspects.put(key, aspectsNbt.getInt(key));
        }
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }
}