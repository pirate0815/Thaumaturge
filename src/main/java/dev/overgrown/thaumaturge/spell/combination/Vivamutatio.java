package dev.overgrown.thaumaturge.spell.combination;

import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.spell.registry.SpellEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.lang.reflect.Field;

public class Vivamutatio implements SpellEntry.SpellExecutor {
    public static final Identifier ID = Thaumaturge.identifier("vivamutatio");

    @Override
    public void execute(ServerPlayerEntity caster) {
        Vec3d eyePos = caster.getEyePos();
        Vec3d lookVec = caster.getRotationVec(1.0F);
        double reach = 20.0;
        Vec3d end = eyePos.add(lookVec.multiply(reach));
        Box box = caster.getBoundingBox().stretch(lookVec.multiply(reach)).expand(1.0);

        EntityHitResult hit = ProjectileUtil.raycast(
                caster,
                eyePos,
                end,
                box,
                entity -> !entity.isSpectator() && entity.canHit(),
                reach * reach
        );

        if (hit != null) {
            Entity target = hit.getEntity();
            World world = caster.getWorld();

            if (target instanceof ZombieVillagerEntity zombieVillager) {
                VillagerEntity villager = new VillagerEntity(EntityType.VILLAGER, world);
                villager.copyPositionAndRotation(zombieVillager);
                villager.setVillagerData(zombieVillager.getVillagerData());
                villager.setExperience(zombieVillager.getExperience());
                world.spawnEntity(villager);
                zombieVillager.discard();
                playSuccessSound(world, target);
            } else if (target instanceof WitchEntity witch) {
                VillagerEntity villager = new VillagerEntity(EntityType.VILLAGER, world);
                villager.copyPositionAndRotation(witch);
                // Fix: Remove redundant cast
                world.getRegistryManager().getOptional(RegistryKeys.VILLAGER_PROFESSION)
                        .flatMap(professionRegistry -> professionRegistry.getRandom(world.getRandom()))
                        .ifPresent(profession -> villager.setVillagerData(villager.getVillagerData().withProfession(profession)));
                world.spawnEntity(villager);
                witch.discard();
                playSuccessSound(world, target);
            } else if (target instanceof ZombifiedPiglinEntity zombifiedPiglin) {
                PigEntity pig = new PigEntity(EntityType.PIG, world);
                pig.copyPositionAndRotation(zombifiedPiglin);
                world.spawnEntity(pig);
                zombifiedPiglin.discard();
                playSuccessSound(world, target);
            } else if (target instanceof CreeperEntity creeper) {
                try {
                    Field chargedField = CreeperEntity.class.getDeclaredField("CHARGED");
                    chargedField.setAccessible(true);

                    @SuppressWarnings("unchecked")
                    TrackedData<Boolean> chargedTracker = (TrackedData<Boolean>) chargedField.get(null);

                    creeper.getDataTracker().set(chargedTracker, true);
                    playSuccessSound(world, target);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    // Use proper logging instead of printStackTrace()
                    Thaumaturge.LOGGER.error("Failed to set Creeper charged state", e);
                }
            }
        }
    }

    private void playSuccessSound(World world, Entity target) {
        world.playSound(
                null,
                target.getX(),
                target.getY(),
                target.getZ(),
                SoundEvents.ENTITY_ZOMBIE_VILLAGER_CURE,
                SoundCategory.NEUTRAL,
                1.0F,
                1.0F
        );
    }
}