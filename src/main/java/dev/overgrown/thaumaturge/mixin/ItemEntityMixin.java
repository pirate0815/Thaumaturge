package dev.overgrown.thaumaturge.mixin;

import dev.overgrown.thaumaturge.block.VesselBlock;
import dev.overgrown.thaumaturge.block.entity.VesselBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {
    public ItemEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        World world = this.getWorld();
        if (!world.isClient && !this.isRemoved()) {
            // Check if the item is close to the ground
            if (this.getY() - this.getBlockY() < 0.1 || this.isOnGround()) {
                BlockPos pos = this.getBlockPos();
                BlockState state = world.getBlockState(pos);

                if (state.getBlock() instanceof VesselBlock && state.get(VesselBlock.WATER_LEVEL) > 0) {
                    if (world.getBlockEntity(pos) instanceof VesselBlockEntity vessel && vessel.isBoiling()) {
                        ItemEntity self = (ItemEntity) (Object) this;
                        if (vessel.addItem(self.getStack())) {
                            self.discard();
                        }
                    }
                }
            }
        }
    }
}