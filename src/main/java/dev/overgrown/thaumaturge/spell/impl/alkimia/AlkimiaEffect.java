package dev.overgrown.thaumaturge.spell.impl.alkimia;

import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.spell.pattern.AspectEffect;
import dev.overgrown.thaumaturge.spell.tier.TargetedSpellDelivery;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class AlkimiaEffect implements AspectEffect {
    @Override
    public void apply(TargetedSpellDelivery delivery) {
        delivery.addOnHitEffect(new EntityCloudSpawnerConsumer(this, delivery));
        delivery.addBlockHitEffect(new BlockCloudSpawnerConsumer(this, delivery));
    }

    private void createCloud(ServerWorld world, Vec3d pos, TargetedSpellDelivery delivery) {
        // Filter out cloud spawners to prevent recursion
        List<Consumer<Entity>> filteredEntityEffects = new ArrayList<>(delivery.getOnHitEffects());
        filteredEntityEffects.removeIf(e -> e instanceof EntityCloudSpawnerConsumer);

        List<Consumer<BlockHitResult>> filteredBlockEffects = new ArrayList<>(delivery.getOnBlockHitEffects());
        filteredBlockEffects.removeIf(e -> e instanceof BlockCloudSpawnerConsumer);

        StationaryEffectCloud cloud = new StationaryEffectCloud(world, pos);
        cloud.setRadius(3.0F);
        cloud.setDuration(100);
        cloud.setOwner(delivery.getCaster());
        cloud.setEffects(filteredEntityEffects, filteredBlockEffects);

        world.spawnEntity(cloud);
    }

    private record EntityCloudSpawnerConsumer(AlkimiaEffect effect, TargetedSpellDelivery delivery) implements Consumer<Entity> {
        @Override
        public void accept(Entity target) {
            effect.createCloud((ServerWorld) target.getWorld(), target.getPos(), delivery);
        }
    }

    private record BlockCloudSpawnerConsumer(AlkimiaEffect effect, TargetedSpellDelivery delivery) implements Consumer<BlockHitResult> {
        @Override
        public void accept(BlockHitResult hit) {
            effect.createCloud(delivery.getCaster().getWorld(), hit.getPos(), delivery);
        }
    }

    private static class StationaryEffectCloud extends AreaEffectCloudEntity {
        private List<Consumer<Entity>> entityEffects = new ArrayList<>();
        private List<Consumer<BlockHitResult>> blockEffects = new ArrayList<>();
        private final BlockPos centerBlock;

        public StationaryEffectCloud(ServerWorld world, Vec3d pos) {
            super(EntityType.AREA_EFFECT_CLOUD, world);
            this.setPosition(pos);
            this.setWaitTime(0);
            this.centerBlock = BlockPos.ofFloored(pos);
        }

        public void setEffects(List<Consumer<Entity>> entityEffects, List<Consumer<BlockHitResult>> blockEffects) {
            this.entityEffects = new ArrayList<>(entityEffects);
            this.blockEffects = new ArrayList<>(blockEffects);
        }

        @Override
        public void tick() {
            super.tick();

            if (this.age % 5 == 0) {
                ServerWorld world = (ServerWorld) this.getWorld();
                Box box = this.getBoundingBox();

                // Apply entity effects to entities in the cloud
                if (!entityEffects.isEmpty()) {
                    for (Entity entity : world.getOtherEntities(null, box, e -> e instanceof LivingEntity)) {
                        for (Consumer<Entity> effect : entityEffects) {
                            try {
                                effect.accept(entity);
                            } catch (Exception e) {
                                Thaumaturge.LOGGER.error("Failed to apply entity effect in cloud", e);
                            }
                        }
                    }
                }

                // Apply block effects to all blocks in the cloud's area
                if (!blockEffects.isEmpty()) {
                    int radius = (int) this.getRadius();
                    BlockPos min = centerBlock.add(-radius, -radius, -radius);
                    BlockPos max = centerBlock.add(radius, radius, radius);

                    for (BlockPos pos : BlockPos.iterate(min, max)) {
                        if (pos.isWithinDistance(centerBlock, radius)) {
                            BlockHitResult blockHit = new BlockHitResult(
                                    Vec3d.ofCenter(pos),
                                    Direction.UP,
                                    pos,
                                    false
                            );
                            for (Consumer<BlockHitResult> effect : blockEffects) {
                                try {
                                    effect.accept(blockHit);
                                } catch (Exception e) {
                                    Thaumaturge.LOGGER.error("Failed to apply block effect in cloud", e);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}