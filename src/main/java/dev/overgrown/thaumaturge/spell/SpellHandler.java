package dev.overgrown.thaumaturge.spell;

import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.component.GauntletComponent;
import dev.overgrown.thaumaturge.component.ModComponents;
import dev.overgrown.thaumaturge.entity.ModEntities;
import dev.overgrown.thaumaturge.item.ModItems;
import dev.overgrown.thaumaturge.networking.SpellCastPacket;
import dev.overgrown.thaumaturge.spell.impl.potentia.entity.SpellBoltEntity;
import dev.overgrown.thaumaturge.spell.pattern.AspectEffect;
import dev.overgrown.thaumaturge.spell.pattern.AspectRegistry;
import dev.overgrown.thaumaturge.spell.pattern.ModifierEffect;
import dev.overgrown.thaumaturge.spell.pattern.ModifierRegistry;
import dev.overgrown.thaumaturge.spell.tier.AoeSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.SelfSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.TargetedSpellDelivery;
import dev.overgrown.thaumaturge.utils.ModSounds;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class SpellHandler {
    private static final Identifier POTENTIA_ID = Thaumaturge.identifier("potentia");
    private static final Identifier VINCULUM_ID = Thaumaturge.identifier("vinculum");
    private static final Identifier VICTUS_ID = Thaumaturge.identifier("victus");

    public static void tryCastSpell(ServerPlayerEntity player, SpellCastPacket.SpellTier tier) {
        List<GauntletComponent.FociEntry> entries = getEquippedFociEntries(player, tier);
        if (entries.isEmpty()) return;

        boolean hasVictus = entries.stream()
                .anyMatch(entry -> entry.aspectId().equals(VICTUS_ID));

        List<GauntletComponent.FociEntry> nonVinculumEntries = new ArrayList<>();
        List<GauntletComponent.FociEntry> vinculumEntries = new ArrayList<>();
        for (GauntletComponent.FociEntry entry : entries) {
            if (entry.aspectId().equals(VINCULUM_ID)) {
                vinculumEntries.add(entry);
            } else {
                nonVinculumEntries.add(entry);
            }
        }

        boolean hasPotentia = entries.stream().anyMatch(entry -> entry.aspectId().equals(POTENTIA_ID));

        if (hasPotentia) {
            handlePotentiaSpell(player, tier, nonVinculumEntries, vinculumEntries);
        } else {
            Object delivery = createDelivery(tier);
            for (GauntletComponent.FociEntry entry : nonVinculumEntries) {
                AspectEffect aspectEffect = AspectRegistry.get(entry.aspectId());
                ModifierEffect modifierEffect = ModifierRegistry.get(entry.modifierId());

                if (modifierEffect != null) {
                    applyEffect(delivery, modifierEffect);
                }

                if (aspectEffect != null) {
                    applyEffect(delivery, aspectEffect);
                }
            }

            for (GauntletComponent.FociEntry entry : vinculumEntries) {
                AspectEffect aspectEffect = AspectRegistry.get(entry.aspectId());
                ModifierEffect modifierEffect = ModifierRegistry.get(entry.modifierId());

                if (modifierEffect != null) {
                    applyEffect(delivery, modifierEffect);
                }

                if (aspectEffect != null) {
                    applyEffect(delivery, aspectEffect);
                }
            }
            executeDelivery(delivery, player);
        }

        if (hasVictus) {
            player.getWorld().playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    ModSounds.VICTUS_SPELL_CAST,
                    SoundCategory.PLAYERS,
                    1.0f,
                    1.0f
            );
        }
    }

    private static boolean hasPotentiaFoci(PlayerEntity player) {
        // Check gauntlet for Potentia aspect
        return true;
    }

    public static void castPotentiaSpell(PlayerEntity caster, int tier) {
        if (hasPotentiaFoci(caster)) {
            SpellBoltEntity bolt = new SpellBoltEntity(ModEntities.SPELL_BOLT, caster.getWorld());
            bolt.setCaster(caster);
            bolt.setPosition(caster.getEyePos());
            Vec3d rotation = caster.getRotationVector().normalize();
            bolt.setVelocity(rotation.multiply(1.5));
            bolt.setTier(tier);
            caster.getWorld().spawnEntity(bolt);
        }
    }

    private static void handlePotentiaSpell(ServerPlayerEntity player, SpellCastPacket.SpellTier tier,
                                            List<GauntletComponent.FociEntry> nonVinculumEntries,
                                            List<GauntletComponent.FociEntry> vinculumEntries) {
        TargetedSpellDelivery dummyDelivery = new TargetedSpellDelivery(tier);
        dummyDelivery.setCaster(player);

        // Apply effects from non-Vinculum Foci
        for (GauntletComponent.FociEntry entry : nonVinculumEntries) {
            if (entry.aspectId().equals(POTENTIA_ID)) continue;

            AspectEffect aspectEffect = AspectRegistry.get(entry.aspectId());
            ModifierEffect modifierEffect = ModifierRegistry.get(entry.modifierId());

            if (modifierEffect != null) modifierEffect.apply(dummyDelivery);
            if (aspectEffect != null) aspectEffect.apply(dummyDelivery);
        }

        // Apply effects from Vinculum Foci
        for (GauntletComponent.FociEntry entry : vinculumEntries) {
            AspectEffect aspectEffect = AspectRegistry.get(entry.aspectId());
            ModifierEffect modifierEffect = ModifierRegistry.get(entry.modifierId());

            if (modifierEffect != null) modifierEffect.apply(dummyDelivery);
            if (aspectEffect != null) aspectEffect.apply(dummyDelivery);
        }

        ProjectileEntity.spawnWithVelocity((world, shooter, stack) -> {
            SpellBoltEntity bolt = new SpellBoltEntity(ModEntities.SPELL_BOLT, player.getWorld());
            bolt.setCaster(player);
            bolt.setPosition(player.getEyePos());
            bolt.setTier(tier.ordinal());

            // Set BOTH entity and block effects
            bolt.setOnHitEffects(dummyDelivery.getOnHitEffects());
            bolt.setOnBlockHitEffects(dummyDelivery.getOnBlockHitEffects());

            // Play sound when the bolt is cast
            ServerWorld serverWorld = player.getWorld();
            serverWorld.playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSounds.POTENTIA_SPELL_CAST, SoundCategory.PLAYERS, 1.0f, 1.0f);

            return bolt;
        }, player.getWorld(), ItemStack.EMPTY, player, 0.0F, 1.5F, 1.0F);
    }

    private static Object createDelivery(SpellCastPacket.SpellTier tier) {
        return switch (tier) {
            case LESSER -> new SelfSpellDelivery(tier);
            case ADVANCED -> new TargetedSpellDelivery(tier);
            case GREATER -> new AoeSpellDelivery(tier);
        };
    }

    private static void applyEffect(Object delivery, Object effect) {
        if (delivery instanceof SelfSpellDelivery self) {
            if (effect instanceof AspectEffect ae) ae.apply(self);
            if (effect instanceof ModifierEffect me) me.apply(self);
        } else if (delivery instanceof TargetedSpellDelivery targeted) {
            if (effect instanceof AspectEffect ae) ae.apply(targeted);
            if (effect instanceof ModifierEffect me) me.apply(targeted);
        } else if (delivery instanceof AoeSpellDelivery aoe) {
            if (effect instanceof AspectEffect ae) ae.apply(aoe);
            if (effect instanceof ModifierEffect me) me.apply(aoe);
        }
    }

    private static void executeDelivery(Object delivery, ServerPlayerEntity player) {
        if (delivery instanceof SelfSpellDelivery self) self.execute(player);
        else if (delivery instanceof TargetedSpellDelivery targeted) targeted.execute(player);
        else if (delivery instanceof AoeSpellDelivery aoe) aoe.execute(player);
    }

    public static List<GauntletComponent.FociEntry> getEquippedFociEntries(ServerPlayerEntity player, SpellCastPacket.SpellTier tier) {
        List<GauntletComponent.FociEntry> entries = new ArrayList<>();
        for (Hand hand : Hand.values()) {
            ItemStack stack = player.getStackInHand(hand);
            if (stack.contains(ModComponents.MAX_FOCI)) {
                GauntletComponent component = stack.get(ModComponents.GAUNTLET_STATE);
                if (component != null) {
                    entries.addAll(component.entries().stream()
                            .filter(e -> e.tier() == tier)
                            .toList());
                }
            }
        }
        return entries;
    }

    public static SpellCastPacket.SpellTier getFociTier(Item item) {
        if (item == ModItems.LESSER_FOCI) return SpellCastPacket.SpellTier.LESSER;
        if (item == ModItems.ADVANCED_FOCI) return SpellCastPacket.SpellTier.ADVANCED;
        if (item == ModItems.GREATER_FOCI) return SpellCastPacket.SpellTier.GREATER;
        return null;
    }
}