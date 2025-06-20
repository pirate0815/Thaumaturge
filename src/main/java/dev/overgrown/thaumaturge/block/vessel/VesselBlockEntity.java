package dev.overgrown.thaumaturge.block.vessel;

import dev.overgrown.thaumaturge.block.ModBlockEntities;
import dev.overgrown.thaumaturge.component.AspectComponent;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

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
    protected void readData(ReadView view) {
        super.readData(view);
        aspectComponent.getMap().clear();
        Optional<AspectComponent> aspectsNbt = view.read("Aspects", AspectComponent.CODEC);
        if (aspectsNbt.isPresent()) {
            aspectComponent.getMap().putAll(aspectsNbt.get().getMap());
        }
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        view.put("Aspects", AspectComponent.CODEC, aspectComponent);
    }
}