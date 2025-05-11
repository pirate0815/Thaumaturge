package dev.overgrown.thaumaturge.block.vessel;

import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.block.ModBlockEntities;
import dev.overgrown.thaumaturge.component.AspectComponent;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;

public class VesselBlockEntity extends BlockEntity {
    private final AspectComponent aspectComponent = new AspectComponent(new Object2IntOpenHashMap<>());

    public VesselBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.VESSEL, pos, state);
    }

    public AspectComponent getAspectComponent() {
        return aspectComponent;
    }

    public void reset() {
        aspectComponent.getMap().clear();
        markDirty();
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        aspectComponent.getMap().clear();
        NbtElement aspectsNbt = nbt.get("Aspects");
        if (aspectsNbt != null) {
            AspectComponent.CODEC.parse(registries.getOps(NbtOps.INSTANCE), aspectsNbt)
                    .resultOrPartial(Thaumaturge.LOGGER::error)
                    .ifPresent(comp -> aspectComponent.getMap().putAll(comp.getMap()));
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        AspectComponent.CODEC.encodeStart(registries.getOps(NbtOps.INSTANCE), aspectComponent)
                .resultOrPartial(Thaumaturge.LOGGER::error)
                .ifPresent(element -> nbt.put("Aspects", element));
    }
}