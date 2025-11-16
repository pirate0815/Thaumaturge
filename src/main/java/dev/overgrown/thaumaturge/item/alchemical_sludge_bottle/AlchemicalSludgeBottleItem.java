package dev.overgrown.thaumaturge.item.alchemical_sludge_bottle;

import dev.overgrown.thaumaturge.registry.ModItems;
import dev.overgrown.thaumaturge.util.AspectMap;
import dev.overgrown.thaumaturge.util.CorruptionHelper;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;

public class AlchemicalSludgeBottleItem extends Item {

    private static final String ASPECT_NBT_TAG = "Aspects";

    public AlchemicalSludgeBottleItem() {
        super(new FabricItemSettings().maxCount(1));
    }

    @Override
    public void onItemEntityDestroyed(ItemEntity entity) {
        super.onItemEntityDestroyed(entity);
        AspectMap map = toAspectMap(entity.getStack());
        if (!map.isEmpty()) {
            int sum = map.getTotalAspectLevel();
            if (entity.getWorld() instanceof ServerWorld serverWorld) {
                CorruptionHelper.addCorruption(serverWorld, entity.getBlockPos(), sum);
            }
        }
    }

    public static ItemStack fromAspectMap(AspectMap aspects) {
        ItemStack stack = new ItemStack(ModItems.ALCHEMICAL_SLUDGE_BOTTLE);
        NbtCompound nbtCompound = new NbtCompound();
        NbtCompound aspectsNbt = aspects.toCompound();
        nbtCompound.put(ASPECT_NBT_TAG, aspectsNbt);
        stack.setNbt(nbtCompound);
        return stack;
    }

    public static AspectMap toAspectMap(ItemStack stack) {
        AspectMap aspects = new AspectMap();
        addToAspectMap(stack, aspects);
        return aspects;
    }

    public static void addToAspectMap(ItemStack stack, AspectMap map) {
        if (stack.hasNbt()) {
            NbtCompound compound = stack.getOrCreateNbt();
            if (compound.contains(ASPECT_NBT_TAG)) {
                NbtCompound aspectsNbt = compound.getCompound(ASPECT_NBT_TAG);
                map.fromNbt(aspectsNbt);
            }
        }
    }
}
