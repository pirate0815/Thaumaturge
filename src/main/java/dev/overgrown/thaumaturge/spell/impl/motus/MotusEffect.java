package dev.overgrown.thaumaturge.spell.impl.motus;

import dev.overgrown.thaumaturge.spell.pattern.AspectEffect;
import dev.overgrown.thaumaturge.spell.tier.SelfSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.TargetedSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.AoeSpellDelivery;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class MotusEffect implements AspectEffect {

    @Override
    public void apply(SelfSpellDelivery delivery) {
        delivery.addEffect(caster -> {
            // Get the direction the player is looking (horizontal only)
            Vec3d lookVec = caster.getRotationVector();
            lookVec = new Vec3d(lookVec.x, 0, lookVec.z).normalize().multiply(1.5);

            // Apply velocity in the looking direction
            caster.addVelocity(lookVec.x, lookVec.y, lookVec.z);
            caster.velocityModified = true;

            // Play breeze shoot sound effect
            caster.getWorld().playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                    SoundEvents.ENTITY_BREEZE_SHOOT, SoundCategory.PLAYERS, 1.0f, 1.0f);
        });
    }

    @Override
    public void apply(TargetedSpellDelivery delivery) {
        delivery.addOnHitEffect(target -> {
            // Push target away from caster's look direction
            Vec3d lookVec = target.getRotationVector();
            target.addVelocity(lookVec.x * 0.5, lookVec.y * 0.5, lookVec.z * 0.5);
            target.velocityModified = true;
        });
    }

    @Override
    public void apply(AoeSpellDelivery delivery) {
        delivery.addEffect(pos -> {
            // Push all entities away from center
            delivery.getCasterWorld().getEntitiesByClass(Entity.class, new Box(pos).expand(5), e -> true)
                    .forEach(entity -> {
                        Vec3d center = Vec3d.ofCenter(pos);
                        Vec3d direction = entity.getPos().subtract(center).normalize();
                        entity.addVelocity(direction.x * 0.5, direction.y * 0.5, direction.z * 0.5);
                        entity.velocityModified = true;
                    });
        });
    }
}