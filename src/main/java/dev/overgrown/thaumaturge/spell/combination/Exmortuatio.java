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
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.lang.reflect.Field;

public class Exmortuatio implements SpellEntry.SpellExecutor {
    public static final Identifier ID = Thaumaturge.identifier("exmortuatio");

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

            if (target instanceof VillagerEntity villager) {
                Entity newEntity = createTransformedEntity(villager, caster.isSneaking());
                newEntity.copyPositionAndRotation(villager);
                world.spawnEntity(newEntity);
                villager.discard();
                playSuccessSound(world, target);
            } else if (target instanceof PigEntity pig) {
                handlePigTransformation(pig, world);
                playSuccessSound(world, target);
            } else if (target instanceof CreeperEntity creeper) {
                unchargeCreeper(creeper);
                playSuccessSound(world, target);
            }
        }
    }

    private Entity createTransformedEntity(VillagerEntity villager, boolean isSneaking) {
        World world = villager.getWorld();
        if (isSneaking) {
            return new WitchEntity(EntityType.WITCH, world);
        } else {
            ZombieVillagerEntity zombieVillager = new ZombieVillagerEntity(EntityType.ZOMBIE_VILLAGER, world);
            zombieVillager.setVillagerData(villager.getVillagerData());
            zombieVillager.setExperience(villager.getExperience());
            zombieVillager.setBaby(villager.isBaby());
            return zombieVillager;
        }
    }

    private void handlePigTransformation(PigEntity pig, World world) {
        ZombifiedPiglinEntity zombifiedPiglin = new ZombifiedPiglinEntity(EntityType.ZOMBIFIED_PIGLIN, world);
        zombifiedPiglin.copyPositionAndRotation(pig);
        zombifiedPiglin.setBaby(pig.isBaby());
        world.spawnEntity(zombifiedPiglin);
        pig.discard();
    }

    @SuppressWarnings("unchecked")
    private void unchargeCreeper(CreeperEntity creeper) {
        try {
            Field chargedField = CreeperEntity.class.getDeclaredField("CHARGED");
            chargedField.setAccessible(true);
            TrackedData<Boolean> chargedTracker = (TrackedData<Boolean>) chargedField.get(null);
            creeper.getDataTracker().set(chargedTracker, false); // Set to false instead of true
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Thaumaturge.LOGGER.error("Failed to uncharge Creeper", e);
        }
    }

    private void playSuccessSound(World world, Entity target) {
        world.playSound(
                null,
                target.getX(),
                target.getY(),
                target.getZ(),
                SoundEvents.ENTITY_ZOMBIE_VILLAGER_CONVERTED,
                SoundCategory.NEUTRAL,
                1.0F,
                1.0F
        );
    }
}