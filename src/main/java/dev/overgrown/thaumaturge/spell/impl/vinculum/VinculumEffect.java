package dev.overgrown.thaumaturge.spell.impl.vinculum;

import dev.overgrown.thaumaturge.entity.ModEntities;
import dev.overgrown.thaumaturge.spell.impl.vinculum.entity.ArcaneMineEntity;
import dev.overgrown.thaumaturge.spell.pattern.AspectEffect;
import dev.overgrown.thaumaturge.spell.tier.TargetedSpellDelivery;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class VinculumEffect implements AspectEffect {
    @Override
    public void apply(TargetedSpellDelivery delivery) {
        // Capture current effects
        List<Consumer<Entity>> onHitEffects = new ArrayList<>(delivery.getOnHitEffects());
        List<Consumer<BlockHitResult>> onBlockHitEffects = new ArrayList<>(delivery.getOnBlockHitEffects());

        // Clear existing effects to prevent immediate application
        delivery.getOnHitEffects().clear();
        delivery.getOnBlockHitEffects().clear();

        // Add effect to spawn mine on block hit
        delivery.addBlockHitEffect(blockHit -> {
            if (delivery.getCaster() != null) {
                ServerWorld world = delivery.getCaster().getWorld();
                ArcaneMineEntity mine = new ArcaneMineEntity(ModEntities.ARCANE_MINE, world);
                mine.setPosition(blockHit.getPos());
                mine.setArmTime(40);
                mine.setOnHitEffects(onHitEffects);
                mine.setOnBlockHitEffects(onBlockHitEffects);
                world.spawnEntity(mine);
            }
        });
    }
}