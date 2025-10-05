package dev.overgrown.thaumaturge.block.entity;

import dev.overgrown.aspectslib.api.AspectsAPI;
import dev.overgrown.aspectslib.data.Aspect;
import dev.overgrown.aspectslib.data.AspectData;
import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.block.VesselBlock;
import dev.overgrown.thaumaturge.recipe.VesselReactionRecipe;
import dev.overgrown.thaumaturge.registry.ModBlocks;
import dev.overgrown.thaumaturge.recipe.VesselRecipe;
import dev.overgrown.aspectslib.aether.DynamicAetherDensityManager;
import dev.overgrown.aspectslib.aether.CorruptionManager;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.block.Block;
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

import java.util.*;
import java.util.stream.Collectors;

public class VesselBlockEntity extends BlockEntity implements Inventory {
    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(6, ItemStack.EMPTY);
    public static final Identifier VITIUM_ASPECT = new Identifier("aspectslib", "vitium");
    private final Map<String, Integer> aspects = new HashMap<>();
    private ItemStack catalyst = ItemStack.EMPTY;
    private boolean heating = false;
    private int processTime = 0;
    private static final double AMBIENT_TEMPERATURE = 8.0;
    private double temperature = AMBIENT_TEMPERATURE;

    public enum TemperatureRange {
        LUKEWARM(0),
        WARM(1),
        HOT(2),
        SCOLDING(3),
        BOILING(4);

        private final int value;
        TemperatureRange(int i) {
            value = i;
        }

        public static TemperatureRange fromTemp(double temp) {
            if (temp < 25.0) {
                return LUKEWARM;
            } else if (temp < 50.0) {
                return WARM;
            } else  if (temp < 75.0) {
                return HOT;
            } else if (temp < 100.0) {
                return SCOLDING;
            } else {
                return BOILING;
            }
        }

        public int getValue() {
            return value;
        }
    }

    public VesselBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.VESSEL_BLOCK_ENTITY, pos, state);
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, VesselBlockEntity blockEntity) {
        int waterLevel = state.get(VesselBlock.WATER_LEVEL);

        if (waterLevel > 0) {
            blockEntity.processTime++;
            if (blockEntity.processTime >= 100) {
                blockEntity.processTime = 0;

                if (blockEntity.canBreakdownItems()) {
                    blockEntity.processItemForAspects();
                }

                if (blockEntity.heating) {
                    // ΔT = 100° / 2 min
                    // Heating up to 120°
                    // ΔT = -100° / 1min
                    // Colling over 120°
                    if (blockEntity.temperature < 120.0) {
                        blockEntity.temperature += 8.3333;
                    } else {
                        blockEntity.temperature -= 1.6667;
                    }
                } else {
                    // ΔT = ±100° / 3 min
                    // Cooling / Heating to ambient temperature
                    if (blockEntity.temperature > AMBIENT_TEMPERATURE) {
                        blockEntity.temperature -= 2.7777;
                    } else if (blockEntity.temperature < AMBIENT_TEMPERATURE) {
                        blockEntity.temperature += 2.7777;
                    }
                }
                blockEntity.markDirty();
                blockEntity.tryAspectReaction();
            }
        }

        // Check if water level is 0 and there are aspects
        if (waterLevel == 0 && !blockEntity.aspects.isEmpty()) {
            blockEntity.convertAspectsToVitium(world, pos);
        }

        // Add extra visual effects if the water is above 100°
        if ((waterLevel > 0) && (blockEntity.temperature >= 100.0)) {
            if (world.getTime() % 10 == 4) {
                ((ServerWorld) world).spawnParticles(ParticleTypes.BUBBLE_POP,
                        pos.getX() + 0.5, pos.getY() + 0.9, pos.getZ() + 0.5,
                        1, 0.1, 0.0, 0.1, 0.05);
            }
        }
        if (blockEntity.heating) {
            if (world.getTime() % 10 == 0) {
                ((ServerWorld) world).spawnParticles(ParticleTypes.BUBBLE_POP,
                        pos.getX() + 0.5, pos.getY() + 0.9, pos.getZ() + 0.5,
                        1, 0.1, 0.0, 0.1, 0.05);
            }
        }
    }

    private void convertAspectsToVitium(World world, BlockPos pos) {
        // Calculate total aspects
        int totalAspects = this.aspects.values().stream().mapToInt(Integer::intValue).sum();

        if (totalAspects == 0) {
            return;
        }

        // Get biome ID
        RegistryEntry<Biome> biomeEntry = world.getBiome(pos);
        Optional<RegistryKey<Biome>> optionalKey = biomeEntry.getKey();
        if (optionalKey.isEmpty()) {
            return;
        }
        Identifier biomeId = optionalKey.get().getValue();

        // Add vitium to biome and register this vessel as a corruption source
        DynamicAetherDensityManager.addModification(biomeId, VITIUM_ASPECT, totalAspects);
        CorruptionManager.addCorruptionSource(biomeId, pos, totalAspects);

        // Clear aspects from vessel
        this.aspects.clear();

        // Reset boiling state, process time and temperature
        this.heating = false;
        this.processTime = 0;
        this.temperature = AMBIENT_TEMPERATURE;

        // Visual and sound effects
        if (world instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(ParticleTypes.SMOKE,
                    pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                    10, 0.2, 0.2, 0.2, 0.0);
            world.playSound(null, pos, SoundEvents.BLOCK_LAVA_EXTINGUISH,
                    SoundCategory.BLOCKS, 1.0f, 1.0f);
        }

        markDirty();
        syncToClient();
    }

    public void processItemForAspects() {
        if (items.stream().allMatch(ItemStack::isEmpty)) return;

        World world = getWorld();
        if (world == null) return;

        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = items.get(i);
            if (stack.isEmpty()) continue;

            AspectData aspectData = AspectsAPI.getAspectData(stack);
            if (!aspectData.isEmpty()) {
                for (var entry : aspectData.getMap().object2IntEntrySet()) {
                    String aspectName = AspectsAPI.getAspect(entry.getKey())
                            .map(Aspect::name)
                            .orElse(entry.getKey().toString());

                    int totalAmount = entry.getIntValue() * stack.getCount();
                    aspects.merge(aspectName, totalAmount, Integer::sum);
                }
            } else {
                ItemEntity itemEntity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, stack.copy());
                world.spawnEntity(itemEntity);
            }

            items.set(i, ItemStack.EMPTY);
            markDirty();
            syncToClient();
            break;
        }
    }

    public boolean addItem(ItemStack stack) {
        World world = getWorld();
        if (world == null) return false;

        boolean isCatalyst = world.getRecipeManager()
                .listAllOfType(VesselRecipe.Type.INSTANCE)
                .stream()
                .anyMatch(recipe -> ItemStack.areItemsEqual(recipe.getCatalyst(), stack));

        if (isCatalyst) {
            boolean recipeMatched = tryCraftWithCatalystDropped(stack);
            if (recipeMatched) {
                return true;
            }
        }

        AspectData aspectData = AspectsAPI.getAspectData(stack);
        if (!aspectData.isEmpty()) {
            for (var entry : aspectData.getMap().object2IntEntrySet()) {
                String aspectName = AspectsAPI.getAspect(entry.getKey())
                        .map(Aspect::name)
                        .orElse(entry.getKey().toString());

                int totalAmount = entry.getIntValue() * stack.getCount();
                aspects.merge(aspectName, totalAmount, Integer::sum);
            }
            markDirty();
            syncToClient();
            return true;
        } else {
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).isEmpty()) {
                    items.set(i, stack.copy());
                    markDirty();
                    syncToClient();
                    return true;
                }
            }
        }
        return false;
    }

    public boolean tryCraftWithCatalystDropped(ItemStack catalystStack) {
        World world = getWorld();
        if (world == null) return false;

        boolean shouldConsume = tryCraftWithCatalyst(catalystStack);
        if (!shouldConsume && world.getRecipeManager()
                .listAllOfType(VesselRecipe.Type.INSTANCE)
                .stream()
                .anyMatch(recipe -> ItemStack.areItemsEqual(recipe.getCatalyst(), catalystStack) &&
                        recipe.getAspects().entrySet().stream()
                                .allMatch(entry -> {
                                    String storedAspectName = entry.getKey().substring(0, 1).toUpperCase() + entry.getKey().substring(1);
                                    return aspects.getOrDefault(storedAspectName, 0) >= entry.getValue();
                                }))) {
            ItemEntity catalystEntity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, catalystStack.copy());
            world.spawnEntity(catalystEntity);
            return true;
        }
        return shouldConsume;
    }

    public boolean tryCraftWithCatalyst(ItemStack catalystStack) {
        World world = getWorld();
        if (world == null) return false;

        Optional<VesselRecipe> match = world.getRecipeManager()
                .listAllOfType(VesselRecipe.Type.INSTANCE)
                .stream()
                .filter(recipe -> ItemStack.areItemsEqual(recipe.getCatalyst(), catalystStack))
                .filter(recipe -> recipe.getAspects().entrySet().stream()
                        .allMatch(entry -> {
                            String storedAspectName = entry.getKey().substring(0, 1).toUpperCase() + entry.getKey().substring(1);
                            return aspects.getOrDefault(storedAspectName, 0) >= entry.getValue();
                        }))
                .findFirst();

        if (match.isPresent()) {
            VesselRecipe recipe = match.get();

            recipe.getAspects().forEach((aspect, amount) -> {
                String storedAspectName = aspect.substring(0, 1).toUpperCase() + aspect.substring(1);
                aspects.computeIfPresent(storedAspectName, (k, v) -> v - amount);
            });
            aspects.entrySet().removeIf(entry -> entry.getValue() <= 0);

            ItemStack output = recipe.getOutput(world.getRegistryManager()).copy();
            ItemEntity result = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, output);
            world.spawnEntity(result);

            BlockState state = world.getBlockState(pos);
            if (state.getBlock() instanceof VesselBlock && world.random.nextFloat() < 0.3f) {
                int waterLevel = state.get(VesselBlock.WATER_LEVEL);
                if (waterLevel > 0) {
                    world.setBlockState(pos, state.with(VesselBlock.WATER_LEVEL, waterLevel - 1));
                }
            }

            markDirty();
            syncToClient();
            return recipe.consumesCatalyst();
        }
        return false;
    }

    public void tryAspectReaction() {
        World world = getWorld();
        if (world == null) return;

        TemperatureRange range = getTemperatureRange();
        List<VesselReactionRecipe> recipes = world.getRecipeManager().listAllOfType(VesselReactionRecipe.Type.INSTANCE).stream()
                .filter(recipe -> recipe.activeAt(range))
                .filter(recipe -> recipe.getAspectsIn().entrySet().stream()
                        .allMatch(entry -> {
                            String storedAspectName = entry.getKey().substring(0, 1).toUpperCase() + entry.getKey().substring(1);
                            return aspects.getOrDefault(storedAspectName, 0) >= entry.getValue();
                        })).toList();

        if (recipes.isEmpty()) {return;}
        VesselReactionRecipe recipe = recipes.get(world.getRandom().nextBetweenExclusive(0,recipes.size()));


        // Remove input aspects
        recipe.getAspectsIn().forEach((aspect, amount) -> {
            String storedAspectName = aspect.substring(0, 1).toUpperCase() + aspect.substring(1);
            aspects.computeIfPresent(storedAspectName, (k, v) -> v - amount);
        });
        aspects.entrySet().removeIf(entry -> entry.getValue() <= 0);

        // Add output aspects
        recipe.getAspectsOut().forEach((aspect, amount) -> {
            String storedAspectName = aspect.substring(0, 1).toUpperCase() + aspect.substring(1);
            if (aspects.containsKey(storedAspectName)) {
                aspects.computeIfPresent(storedAspectName, (k,v) -> v + amount);
            } else {
                aspects.put(storedAspectName, amount);
            }
        });
        // Apply temperature
        temperature += recipe.getDeltaT();

        markDirty();
        syncToClient();

    }

    public ItemStack getCatalyst() {
        return catalyst;
    }

    public Map<String, Integer> getAspects() {
        return aspects;
    }

    private void syncToClient() {
        if (world != null && !world.isClient) {
            world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_LISTENERS);
        }
    }

    public void setHeating(boolean heating) {
        this.heating = heating;
        this.processTime = 0;
        markDirty();
    }

    public TemperatureRange getTemperatureRange() {
        return TemperatureRange.fromTemp(this.temperature);
    }

    public boolean isHeating() {
        return heating;
    }

    public boolean canBreakdownItems() {
        return temperature >= 0;
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
        nbt.putBoolean("Heating", heating);
        nbt.putInt("ProcessTime", processTime);
        nbt.putDouble("Temperature", temperature);
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
        heating = nbt.getBoolean("Heating");
        processTime = nbt.getInt("ProcessTime");
        temperature = nbt.getDouble("Temperature");

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