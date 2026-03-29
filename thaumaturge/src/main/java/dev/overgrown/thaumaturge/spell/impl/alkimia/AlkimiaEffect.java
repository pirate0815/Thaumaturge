package dev.overgrown.thaumaturge.spell.impl.alkimia;

import dev.overgrown.aspectslib.AspectsLib;
import dev.overgrown.thaumaturge.spell.impl.alkimia.entity.AlkimiaCloudEntity;
import dev.overgrown.thaumaturge.spell.pattern.AspectEffect;
import dev.overgrown.thaumaturge.spell.tier.AoeSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.SelfSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.TargetedSpellDelivery;
import dev.overgrown.thaumaturge.spell.utils.SpellHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.LinkedHashMap;
import java.util.Map;

public class AlkimiaEffect implements AspectEffect {

    @Override
    public void applySelf(SelfSpellDelivery delivery) {
        ServerPlayerEntity caster = delivery.getCaster();
        deployCloud(caster, caster.getBlockPos(), delivery.getModifiers(), "lesser");
    }

    @Override
    public void applyTargeted(TargetedSpellDelivery delivery) {
        ServerPlayerEntity caster = delivery.getCaster();
        if (caster == null) return;

        BlockPos targetPos = null;
        if (delivery.isEntityTarget() && delivery.getTargetEntity() != null) {
            targetPos = delivery.getTargetEntity().getBlockPos();
        } else if (delivery.isBlockTarget() && delivery.getBlockPos() != null) {
            targetPos = delivery.getBlockPos().offset(delivery.getFace());
        }

        if (targetPos != null) {
            deployCloud(caster, targetPos, delivery.getModifiers(), "advanced");
        }
    }

    @Override
    public void applyAoe(AoeSpellDelivery delivery) {
        ServerPlayerEntity caster = delivery.getCaster();
        BlockPos center = delivery.getCenter();

        // Check for scatter modifier
        boolean hasScatter = delivery.getModifiers().stream()
                .anyMatch(mod -> mod instanceof dev.overgrown.thaumaturge.spell.modifier.ScatterModifierEffect);

        if (hasScatter) {
            // Spawn 2-3 clouds in radius
            int cloudCount = 2 + caster.getWorld().random.nextInt(2); // 2 or 3 clouds
            float radius = delivery.getRadius();

            for (int i = 0; i < cloudCount; i++) {
                double angle = 2 * Math.PI * i / cloudCount;
                double x = center.getX() + 0.5 + Math.cos(angle) * radius;
                double z = center.getZ() + 0.5 + Math.sin(angle) * radius;
                BlockPos cloudPos = new BlockPos((int) x, center.getY(), (int) z);
                deployCloud(caster, cloudPos, delivery.getModifiers(), "greater");
            }
        } else {
            // Single cloud at center
            deployCloud(caster, center, delivery.getModifiers(), "greater");
        }
    }

    private void deployCloud(ServerPlayerEntity caster, BlockPos pos,
                             java.util.List<dev.overgrown.thaumaturge.spell.modifier.ModifierEffect> modifiers,
                             String tier) {

        // Get the current spell pattern from the gauntlet
        var gauntlet = SpellHandler.findGauntlet(caster);
        if (gauntlet.isEmpty()) return;

        var pattern = dev.overgrown.thaumaturge.spell.pattern.SpellPattern.fromGauntlet(gauntlet, tier);
        if (pattern == null || pattern.getAspects().isEmpty()) return;

        // Filter out Alkimia aspect and store the other aspects
        Map<net.minecraft.util.Identifier, net.minecraft.util.Identifier> storedEffects = new LinkedHashMap<>();

        for (var entry : pattern.getAspects().entrySet()) {
            // Skip Alkimia itself to prevent recursion
            if (entry.getKey().equals(AspectsLib.identifier("alkimia"))) continue;

            // Only store valid aspects that exist in the registry
            var aspectOpt = dev.overgrown.thaumaturge.spell.pattern.AspectRegistry.get(entry.getKey());
            if (aspectOpt.isPresent()) {
                storedEffects.put(entry.getKey(), entry.getValue());
            }
        }

        if (storedEffects.isEmpty()) return;

        // Create and spawn the cloud
        AlkimiaCloudEntity cloud = new AlkimiaCloudEntity(caster.getWorld(), caster, storedEffects, modifiers, 100, 3.0f);
        cloud.setPosition(Vec3d.ofCenter(pos));
        caster.getWorld().spawnEntity(cloud);
    }
}