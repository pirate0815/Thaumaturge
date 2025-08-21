package dev.overgrown.thaumaturge.block.entity;

import dev.overgrown.aspectslib.api.AspectsAPI;
import dev.overgrown.aspectslib.data.AspectData;
import dev.overgrown.thaumaturge.block.VesselBlock;
import dev.overgrown.thaumaturge.registry.ModBlocks;
import dev.overgrown.thaumaturge.recipe.VesselRecipe;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
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
import java.util.Optional;

public class VesselBlockEntity extends BlockEntity implements Inventory {
    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(6, ItemStack.EMPTY);
    private final Map<String, Integer> aspects = new HashMap<>();
    private ItemStack catalyst = ItemStack.EMPTY;
    private boolean boiling = false;
    private int processTime = 0;

    public VesselBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.VESSEL_BLOCK_ENTITY, pos, state);
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, VesselBlockEntity blockEntity) {
        if (blockEntity.boiling) {
            if (world.getTime() % 10 == 0) {
                ((ServerWorld) world).spawnParticles(ParticleTypes.BUBBLE_POP,
                        pos.getX() + 0.5, pos.getY() + 0.3, pos.getZ() + 0.5,
                        2, 0.2, 0.0, 0.2, 0.05);
            }

            blockEntity.processTime++;
            if (blockEntity.processTime >= 100) { // Process every 5 seconds (100 ticks)
                blockEntity.processTime = 0;
                blockEntity.processItem();
            }
        }
    }

    public boolean addItem(ItemStack stack) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).isEmpty()) {
                items.set(i, stack.copy());
                markDirty();
                // Process the item immediately when added
                processItem();
                return true;
            }
        }
        return false;
    }


    public void processItem() {
        if (items.stream().allMatch(ItemStack::isEmpty)) return;

        World world = getWorld();
        if (world == null) return;

        // Try to find a recipe match first
        Optional<VesselRecipe> match = world.getRecipeManager()
                .getFirstMatch(VesselRecipe.Type.INSTANCE, this, world);

        if (match.isPresent()) {
            VesselRecipe recipe = match.get();
            // Craft the recipe
            if (craftWithCatalyst(recipe)) {
                return;
            }
        }

        // If no recipe, process items for aspects
        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = items.get(i);
            if (stack.isEmpty()) continue;

            AspectData aspectData = AspectsAPI.getAspectData(stack);
            if (!aspectData.isEmpty()) {
                // Convert item to aspects
                for (var entry : aspectData.getMap().object2IntEntrySet()) {
                    String aspectName = AspectsAPI.getAspect(entry.getKey())
                            .map(aspect -> aspect.name())
                            .orElse(entry.getKey().toString());

                    aspects.merge(aspectName, entry.getIntValue(), Integer::sum);
                }
                // REMOVED: Water consumption for item processing
            } else {
                // Drop item unchanged
                ItemEntity itemEntity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, stack.copy());
                world.spawnEntity(itemEntity);
            }

            items.set(i, ItemStack.EMPTY);
            markDirty();
            break; // Process one item at a time
        }
    }

    public boolean isCatalyst(ItemStack stack) {
        // Check if this item can be used as a catalyst
        World world = getWorld();
        if (world == null) return false;

        return world.getRecipeManager()
                .listAllOfType(VesselRecipe.Type.INSTANCE)
                .stream()
                .anyMatch(recipe -> ItemStack.areItemsEqual(recipe.getCatalyst(), stack));
    }

    public void setCatalyst(ItemStack stack) {
        this.catalyst = stack;
        markDirty();
    }

    public ItemStack getCatalyst() {
        return catalyst;
    }

    public boolean craftWithCatalyst(VesselRecipe recipe) {
        World world = getWorld();
        if (world == null) return false;

        // Check if we have the correct catalyst (if the recipe requires one)
        if (!recipe.getCatalyst().isEmpty()) {
            ItemStack vesselCatalyst = getCatalyst();
            if (vesselCatalyst.isEmpty() || !ItemStack.areItemsEqual(recipe.getCatalyst(), vesselCatalyst)) {
                return false;
            }
        }

        // Check if we have enough aspects
        if (recipe.getAspects().entrySet().stream()
                .allMatch(entry -> aspects.getOrDefault(entry.getKey(), 0) >= entry.getValue())) {
            // Consume aspects
            recipe.getAspects().forEach((aspect, amount) ->
                    aspects.computeIfPresent(aspect, (k, v) -> v >= amount ? v - amount : v));

            // Remove catalyst if consumed
            if (recipe.consumesCatalyst() && !catalyst.isEmpty()) {
                catalyst.decrement(1);
                if (catalyst.isEmpty()) {
                    catalyst = ItemStack.EMPTY;
                }
            }

            // Spawn result
            ItemStack output = recipe.getOutput(world.getRegistryManager()).copy();
            ItemEntity result = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, output);
            world.spawnEntity(result);

            // Random chance to reduce water level (only for successful recipes)
            BlockState state = world.getBlockState(pos);
            if (state.getBlock() instanceof VesselBlock && world.random.nextFloat() < 0.3f) {
                int waterLevel = state.get(VesselBlock.WATER_LEVEL);
                if (waterLevel > 0) {
                    world.setBlockState(pos, state.with(VesselBlock.WATER_LEVEL, waterLevel - 1));
                }
            }

            markDirty();
            return true;
        }
        return false;
    }

    public Map<String, Integer> getAspects() {
        return aspects;
    }

    public void setBoiling(boolean boiling) {
        this.boiling = boiling;
        markDirty();
    }

    public boolean isBoiling() {
        return boiling;
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, items);
        nbt.put("Catalyst", catalyst.writeNbt(new NbtCompound()));

        NbtCompound aspectsNbt = new NbtCompound();
        for (Map.Entry<String, Integer> entry : aspects.entrySet()) {
            aspectsNbt.putInt(entry.getKey(), entry.getValue());
        }
        nbt.put("Aspects", aspectsNbt);
        nbt.putBoolean("Boiling", boiling);
        nbt.putInt("ProcessTime", processTime);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, items);
        catalyst = ItemStack.fromNbt(nbt.getCompound("Catalyst"));

        aspects.clear();
        NbtCompound aspectsNbt = nbt.getCompound("Aspects");
        for (String key : aspectsNbt.getKeys()) {
            aspects.put(key, aspectsNbt.getInt(key));
        }
        boiling = nbt.getBoolean("Boiling");
        processTime = nbt.getInt("ProcessTime");
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
    public int size() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getStack(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return Inventories.splitStack(items, slot, amount);
    }

    @Override
    public ItemStack removeStack(int slot) {
        return Inventories.removeStack(items, slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        items.set(slot, stack);
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void clear() {
        items.clear();
    }
}