package dev.overgrown.thaumaturge.spell.impl.vinculum;

import dev.overgrown.aspectslib.AspectsLib;
import dev.overgrown.thaumaturge.spell.impl.vinculum.entity.ArcaneMineEntity;
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

public class VinculumEffect implements AspectEffect {

    @Override
    public void applySelf(SelfSpellDelivery delivery) {
        ServerPlayerEntity caster = delivery.getCaster();
        // Only deploy mine, don't apply other aspects immediately
        deployMine(caster, caster.getBlockPos(), delivery.getModifiers(), "lesser");
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
            // Only deploy mine, don't apply other aspects immediately
            deployMine(caster, targetPos, delivery.getModifiers(), "advanced");
        }
    }

    @Override
    public void applyAoe(AoeSpellDelivery delivery) {
        ServerPlayerEntity caster = delivery.getCaster();
        BlockPos center = delivery.getCenter();
        float radius = delivery.getRadius();

        int mineCount = 3 + delivery.getModifiers().size();

        for (int i = 0; i < mineCount; i++) {
            double angle = 2 * Math.PI * i / mineCount;
            double x = center.getX() + 0.5 + Math.cos(angle) * radius;
            double z = center.getZ() + 0.5 + Math.sin(angle) * radius;

            BlockPos minePos = new BlockPos((int) x, center.getY(), (int) z);
            // Only deploy mine, don't apply other aspects immediately
            deployMine(caster, minePos, delivery.getModifiers(), "greater");
        }
    }

    private void deployMine(ServerPlayerEntity caster, BlockPos pos,
                            java.util.List<dev.overgrown.thaumaturge.spell.modifier.ModifierEffect> modifiers, String tier) {

        // Get the current spell pattern from the gauntlet
        var gauntlet = SpellHandler.findGauntlet(caster);
        if (gauntlet.isEmpty()) return;

        var pattern = dev.overgrown.thaumaturge.spell.pattern.SpellPattern.fromGauntlet(gauntlet, tier);
        if (pattern == null || pattern.getAspects().isEmpty()) return;

        // Filter out Vinculum aspect and store the other aspects
        Map<net.minecraft.util.Identifier, net.minecraft.util.Identifier> storedEffects = new LinkedHashMap<>();

        for (var entry : pattern.getAspects().entrySet()) {
            // Skip Vinculum itself to prevent recursion
            if (entry.getKey().equals(AspectsLib.identifier("vinculum"))) continue;

            // Only store valid aspects that exist in the registry
            var aspectOpt = dev.overgrown.thaumaturge.spell.pattern.AspectRegistry.get(entry.getKey());
            if (aspectOpt.isPresent()) {
                storedEffects.put(entry.getKey(), entry.getValue());
            }
        }

        if (storedEffects.isEmpty()) return;

        // Create and spawn the mine with stored effects
        ArcaneMineEntity mine = new ArcaneMineEntity(caster.getWorld(), caster, storedEffects, tier);
        mine.setPosition(Vec3d.ofCenter(pos));
        caster.getWorld().spawnEntity(mine);
    }
}