package dev.overgrown.thaumaturge.spell.impl.alkimia;

import dev.overgrown.thaumaturge.spell.pattern.AspectEffect;
import dev.overgrown.thaumaturge.spell.tier.TargetedSpellDelivery;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class AlkimiaEffect implements AspectEffect {
    @Override
    public void apply(TargetedSpellDelivery delivery) {
        delivery.addOnHitEffect(new CloudSpawnerConsumer(delivery));
    }

    private record CloudSpawnerConsumer(TargetedSpellDelivery delivery) implements Consumer<Entity> {
        @Override
        public void accept(Entity target) {
            if (target.getWorld() instanceof ServerWorld world) {
                List<Consumer<Entity>> filteredEffects = new ArrayList<>(delivery.getOnHitEffects());
                filteredEffects.removeIf(e -> e instanceof CloudSpawnerConsumer);

                StationaryEffectCloud cloud = new StationaryEffectCloud(world, target.getPos());
                cloud.setRadius(3.0F);
                cloud.setDuration(100);
                cloud.setOwner(delivery.getCaster());
                cloud.setEffects(filteredEffects);

                world.spawnEntity(cloud);
            }
        }
    }

    private static class StationaryEffectCloud extends AreaEffectCloudEntity {
        private List<Consumer<Entity>> effects = new ArrayList<>();

        public StationaryEffectCloud(ServerWorld world, Vec3d pos) {
            super(EntityType.AREA_EFFECT_CLOUD, world);
            this.setPosition(pos);
            this.setWaitTime(0);
        }

        public void setEffects(List<Consumer<Entity>> effects) {
            this.effects = new ArrayList<>(effects);
        }

        @Override
        public void tick() {
            super.tick();

            if (this.age % 5 == 0 && !effects.isEmpty() && this.getWorld() instanceof ServerWorld) {
                this.getWorld().getOtherEntities(
                        null, this.getBoundingBox(),
                        e -> e instanceof LivingEntity
                ).forEach(entity -> effects.forEach(effect -> effect.accept(entity)));
            }
        }
    }
}