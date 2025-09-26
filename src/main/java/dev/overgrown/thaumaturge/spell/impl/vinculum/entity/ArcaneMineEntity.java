package dev.overgrown.thaumaturge.spell.impl.vinculum.entity;

import dev.overgrown.thaumaturge.spell.pattern.AspectEffect;
import dev.overgrown.thaumaturge.spell.tier.AoeSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.SelfSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.TargetedSpellDelivery;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.*;

public class ArcaneMineEntity extends Entity {
    private static final TrackedData<Integer> ARMING_TIME = DataTracker.registerData(ArcaneMineEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Boolean> ARMED = DataTracker.registerData(ArcaneMineEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> TRIGGERED = DataTracker.registerData(ArcaneMineEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    private UUID casterUuid;
    private final Map<Identifier, Identifier> storedAspects;
    private String spellTier;
    private int triggerCooldown = 0;

    public ArcaneMineEntity(EntityType<?> type, World world) {
        super(type, world);
        this.storedAspects = new LinkedHashMap<>();
        this.spellTier = "advanced"; // Default to advanced for mine deployment
        this.setInvulnerable(true);
        this.setNoGravity(true);
    }

    public ArcaneMineEntity(World world, ServerPlayerEntity caster, Map<Identifier, Identifier> aspects, String tier) {
        this(dev.overgrown.thaumaturge.registry.ModEntities.ARCANE_MINE, world);
        this.casterUuid = caster.getUuid();
        this.storedAspects.putAll(aspects);
        this.spellTier = tier; // Make sure to set the tier
        this.setPosition(caster.getX(), caster.getY(), caster.getZ());
        this.dataTracker.set(ARMING_TIME, 40); // 2 seconds at 20 ticks/second
        this.dataTracker.set(ARMED, false);
        this.dataTracker.set(TRIGGERED, false);
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(ARMING_TIME, 0);
        this.dataTracker.startTracking(ARMED, false);
        this.dataTracker.startTracking(TRIGGERED, false);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.getWorld().isClient) {
            // Client-side particle effects
            if (!this.isArmed() && !this.isTriggered()) {
                // Arming particles - purple swirling effect
                for (int i = 0; i < 2; i++) {
                    double offsetX = (this.random.nextDouble() - 0.5) * 0.5;
                    double offsetZ = (this.random.nextDouble() - 0.5) * 0.5;
                    this.getWorld().addParticle(ParticleTypes.REVERSE_PORTAL,
                            this.getX() + offsetX, this.getY() + 0.1, this.getZ() + offsetZ,
                            0, 0.1, 0);
                }
            }
            return;
        }

        // Server-side logic
        if (this.isTriggered()) {
            this.triggerCooldown--;
            if (this.triggerCooldown <= 0) {
                this.discard();
            }
            return;
        }

        if (!this.isArmed()) {
            int armingTime = this.dataTracker.get(ARMING_TIME);
            if (armingTime > 0) {
                this.dataTracker.set(ARMING_TIME, armingTime - 1);
            } else {
                this.dataTracker.set(ARMED, true);
                // Play arming complete sound
                this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.BLOCK_RESPAWN_ANCHOR_SET_SPAWN,
                        getSoundCategory(), 0.5f, 1.0f);
            }
        } else {
            // Check for entities above the mine
            Box detectionBox = new Box(
                    this.getX() - 0.5, this.getY(), this.getZ() - 0.5,
                    this.getX() + 0.5, this.getY() + 0.5, this.getZ() + 0.5
            );

            List<Entity> entities = this.getWorld().getOtherEntities(this, detectionBox,
                    entity -> entity.isAlive() && !entity.isSpectator());

            if (!entities.isEmpty()) {
                this.trigger(entities.get(0));
            }
        }
    }

    private void trigger(Entity triggerEntity) {
        if (this.isTriggered() || !this.isArmed()) return;

        this.dataTracker.set(TRIGGERED, true);
        this.triggerCooldown = 10; // Half second before removal

        // Play activation effects
        this.getWorld().playSound(null, this.getBlockPos(), SoundEvents.ENTITY_GENERIC_EXPLODE,
                getSoundCategory(), 0.7f, 1.2f);

        // Explosion particles
        if (this.getWorld() instanceof net.minecraft.server.world.ServerWorld serverWorld) {
            serverWorld.spawnParticles(ParticleTypes.EXPLOSION,
                    this.getX(), this.getY() + 0.5, this.getZ(),
                    5, 0.5, 0.5, 0.5, 0.1);
        }

        // Apply stored spell effects
        ServerPlayerEntity caster = this.getCaster();
        if (caster != null && !storedAspects.isEmpty()) {
            this.applyStoredEffects(caster, triggerEntity);
        }
    }

    private void applyStoredEffects(ServerPlayerEntity caster, Entity triggerEntity) {
        if (storedAspects.isEmpty()) return;

        // Apply each stored aspect effect based on focus tier
        for (Map.Entry<Identifier, Identifier> entry : this.storedAspects.entrySet()) {
            var aspectOpt = dev.overgrown.thaumaturge.spell.pattern.AspectRegistry.get(entry.getKey());
            if (!aspectOpt.isPresent()) continue;

            AspectEffect aspect = aspectOpt.get();
            dev.overgrown.thaumaturge.spell.modifier.ModifierEffect modifier =
                    dev.overgrown.thaumaturge.spell.modifier.ModifierRegistry.get(entry.getValue());

            List<dev.overgrown.thaumaturge.spell.modifier.ModifierEffect> modifiers = modifier != null ?
                    Collections.singletonList(modifier) : Collections.emptyList();

            // Apply effects based on focus tier
            switch (this.spellTier) {
                case "lesser" -> {
                    // Lesser focus: apply self effects on the original caster
                    if (caster != null && caster.isAlive()) {
                        SelfSpellDelivery delivery = new SelfSpellDelivery(caster);
                        delivery.setModifiers(modifiers);
                        aspect.applySelf(delivery);
                    }
                }
                case "advanced" -> {
                    // Advanced focus: apply targeted effects on the trigger entity
                    if (triggerEntity instanceof ServerPlayerEntity playerTrigger) {
                        // For player triggers, use targeted delivery
                        TargetedSpellDelivery delivery = new TargetedSpellDelivery(caster, triggerEntity);
                        delivery.setModifiers(modifiers);
                        aspect.applyTargeted(delivery);
                    } else if (triggerEntity instanceof LivingEntity livingTrigger) {
                        // For living entity triggers
                        TargetedSpellDelivery delivery = new TargetedSpellDelivery(caster, triggerEntity);
                        delivery.setModifiers(modifiers);
                        aspect.applyTargeted(delivery);
                    } else {
                        // For other entities or block triggers, use AOE around the mine
                        AoeSpellDelivery delivery = new AoeSpellDelivery(caster, this.getBlockPos(), 2.0f);
                        delivery.setModifiers(modifiers);
                        aspect.applyAoe(delivery);
                    }
                }
                case "greater" -> {
                    // Greater focus: apply AOE effects around the mine
                    AoeSpellDelivery delivery = new AoeSpellDelivery(caster, this.getBlockPos(), 3.0f);
                    delivery.setModifiers(modifiers);
                    aspect.applyAoe(delivery);

                    // 10% chance to also affect the caster
                    if (caster != null && caster.isAlive() && this.getWorld().random.nextFloat() < 0.1f) {
                        SelfSpellDelivery selfDelivery = new SelfSpellDelivery(caster);
                        selfDelivery.setModifiers(modifiers);
                        aspect.applySelf(selfDelivery);
                    }
                }
            }
        }
    }

    private ServerPlayerEntity getCaster() {
        if (this.casterUuid != null && this.getWorld() instanceof net.minecraft.server.world.ServerWorld) {
            return ((net.minecraft.server.world.ServerWorld) this.getWorld()).getServer().getPlayerManager().getPlayer(this.casterUuid);
        }
        return null;
    }

    public boolean isArmed() {
        return this.dataTracker.get(ARMED);
    }

    public boolean isTriggered() {
        return this.dataTracker.get(TRIGGERED);
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        // Only read if we're not disabled
        if (!this.getType().isSaveable()) return;

        this.dataTracker.set(ARMING_TIME, nbt.getInt("ArmingTime"));
        this.dataTracker.set(ARMED, nbt.getBoolean("Armed"));
        this.dataTracker.set(TRIGGERED, nbt.getBoolean("Triggered"));
        this.spellTier = nbt.getString("SpellTier");

        if (nbt.containsUuid("CasterUUID")) {
            this.casterUuid = nbt.getUuid("CasterUUID");
        }

        // Load stored aspects
        this.storedAspects.clear();
        NbtList aspectsList = nbt.getList("StoredAspects", 10);
        for (int i = 0; i < aspectsList.size(); i++) {
            NbtCompound aspectCompound = aspectsList.getCompound(i);
            Identifier aspectId = new Identifier(aspectCompound.getString("Aspect"));
            Identifier modifierId = new Identifier(aspectCompound.getString("Modifier"));
            this.storedAspects.put(aspectId, modifierId);
        }
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        // Only write if we're not disabled
        if (!this.getType().isSaveable()) return;

        nbt.putInt("ArmingTime", this.dataTracker.get(ARMING_TIME));
        nbt.putBoolean("Armed", this.dataTracker.get(ARMED));
        nbt.putBoolean("Triggered", this.dataTracker.get(TRIGGERED));
        nbt.putString("SpellTier", this.spellTier);

        if (this.casterUuid != null) {
            nbt.putUuid("CasterUUID", this.casterUuid);
        }

        // Save stored aspects
        NbtList aspectsList = new NbtList();
        for (Map.Entry<Identifier, Identifier> entry : this.storedAspects.entrySet()) {
            NbtCompound aspectCompound = new NbtCompound();
            aspectCompound.putString("Aspect", entry.getKey().toString());
            aspectCompound.putString("Modifier", entry.getValue().toString());
            aspectsList.add(aspectCompound);
        }
        nbt.put("StoredAspects", aspectsList);
    }

    @Override
    public boolean isInvisible() {
        return true; // Mine is invisible
    }

    @Override
    public boolean isInvulnerable() {
        return true; // Invulnerable to prevent accidental triggers
    }

    public boolean collides() {
        return !this.isTriggered(); // Only collide when not triggered
    }
}