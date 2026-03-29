package dev.overgrown.thaumaturge.spell.impl.alkimia.entity;

import dev.overgrown.thaumaturge.spell.pattern.AspectEffect;
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
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.*;

public class AlkimiaCloudEntity extends Entity {
    private static final TrackedData<Integer> DURATION = DataTracker.registerData(AlkimiaCloudEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Float> RADIUS = DataTracker.registerData(AlkimiaCloudEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Integer> APPLY_INTERVAL = DataTracker.registerData(AlkimiaCloudEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> AGE = DataTracker.registerData(AlkimiaCloudEntity.class, TrackedDataHandlerRegistry.INTEGER);

    private UUID casterUuid;
    private final Map<Identifier, Identifier> storedAspects;
    private int tickCounter = 0;
    private List<dev.overgrown.thaumaturge.spell.modifier.ModifierEffect> modifiers;

    public AlkimiaCloudEntity(EntityType<?> type, World world) {
        super(type, world);
        this.storedAspects = new LinkedHashMap<>();
        this.modifiers = new ArrayList<>();
        this.setInvulnerable(true);
        this.setNoGravity(true);
    }

    public AlkimiaCloudEntity(World world, ServerPlayerEntity caster, Map<Identifier, Identifier> aspects,
                              List<dev.overgrown.thaumaturge.spell.modifier.ModifierEffect> modifiers,
                              int duration, float radius) {
        this(dev.overgrown.thaumaturge.registry.ModEntities.ALKIMIA_CLOUD, world);
        this.casterUuid = caster.getUuid();
        this.storedAspects.putAll(aspects);
        this.modifiers = new ArrayList<>(modifiers);

        // Apply power modifier to duration
        boolean hasPower = modifiers.stream().anyMatch(mod -> mod instanceof dev.overgrown.thaumaturge.spell.modifier.PowerModifierEffect);
        int finalDuration = hasPower ? duration * 2 : duration;

        this.dataTracker.set(DURATION, finalDuration);
        this.dataTracker.set(RADIUS, radius);
        this.dataTracker.set(APPLY_INTERVAL, 5); // Apply effects every 5 ticks
        this.dataTracker.set(AGE, 0);

        this.setPosition(caster.getX(), caster.getY() + 1.0, caster.getZ());
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(DURATION, 100); // 5 seconds default
        this.dataTracker.startTracking(RADIUS, 3.0f);
        this.dataTracker.startTracking(APPLY_INTERVAL, 5);
        this.dataTracker.startTracking(AGE, 0);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.getWorld().isClient) {
            // Client-side particle effects
            spawnParticles();
            return;
        }

        // Server-side logic
        int age = this.dataTracker.get(AGE);
        int duration = this.dataTracker.get(DURATION);

        if (age >= duration) {
            this.discard();
            return;
        }

        this.dataTracker.set(AGE, age + 1);
        this.tickCounter++;

        // Apply effects every 5 ticks
        if (this.tickCounter >= this.dataTracker.get(APPLY_INTERVAL)) {
            this.tickCounter = 0;
            this.applyCloudEffects();
        }
    }

    private void spawnParticles() {
        float radius = this.getRadius();
        World world = this.getWorld();

        // Spawn particles in a sphere pattern
        for (int i = 0; i < 10; i++) {
            double angle = world.random.nextDouble() * Math.PI * 2;
            double distance = world.random.nextDouble() * radius;
            double yOffset = (world.random.nextDouble() - 0.5) * radius * 0.5;

            double x = this.getX() + Math.cos(angle) * distance;
            double y = this.getY() + yOffset + 0.5;
            double z = this.getZ() + Math.sin(angle) * distance;

            world.addParticle(ParticleTypes.CLOUD, x, y, z, 0, 0.02, 0);
        }
    }

    private void applyCloudEffects() {
        ServerPlayerEntity caster = this.getCaster();
        if (caster == null || this.storedAspects.isEmpty()) return;

        float radius = this.getRadius();
        Box effectBox = new Box(
                this.getX() - radius, this.getY() - radius, this.getZ() - radius,
                this.getX() + radius, this.getY() + radius, this.getZ() + radius
        );

        // Apply to entities in range
        List<Entity> entities = this.getWorld().getOtherEntities(this, effectBox,
                entity -> entity.isAlive() && !entity.isSpectator() && entity.distanceTo(this) <= radius);

        for (Entity entity : entities) {
            this.applyEffectsToTarget(caster, entity);
        }

        // Apply to blocks in range (for aspects that affect blocks)
        this.applyEffectsToBlocks(caster, radius);
    }

    private void applyEffectsToTarget(ServerPlayerEntity caster, Entity target) {
        for (Map.Entry<Identifier, Identifier> entry : this.storedAspects.entrySet()) {
            var aspectOpt = dev.overgrown.thaumaturge.spell.pattern.AspectRegistry.get(entry.getKey());
            if (!aspectOpt.isPresent()) continue;

            AspectEffect aspect = aspectOpt.get();
            dev.overgrown.thaumaturge.spell.modifier.ModifierEffect modifier =
                    dev.overgrown.thaumaturge.spell.modifier.ModifierRegistry.get(entry.getValue());

            List<dev.overgrown.thaumaturge.spell.modifier.ModifierEffect> mods = modifier != null ?
                    Collections.singletonList(modifier) : this.modifiers;

            if (target instanceof LivingEntity livingTarget) {
                TargetedSpellDelivery delivery = new TargetedSpellDelivery(caster, livingTarget);
                delivery.setModifiers(mods);
                aspect.applyTargeted(delivery);
            }
        }
    }

    private void applyEffectsToBlocks(ServerPlayerEntity caster, float radius) {
        BlockPos center = this.getBlockPos();
        int r = (int) Math.ceil(radius);

        // Check blocks in a cube around the cloud
        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    BlockPos targetPos = center.add(x, y, z);
                    if (center.getSquaredDistance(targetPos) <= radius * radius) {
                        this.applyEffectsToBlock(caster, targetPos);
                    }
                }
            }
        }
    }

    private void applyEffectsToBlock(ServerPlayerEntity caster, BlockPos pos) {
        for (Map.Entry<Identifier, Identifier> entry : this.storedAspects.entrySet()) {
            var aspectOpt = dev.overgrown.thaumaturge.spell.pattern.AspectRegistry.get(entry.getKey());
            if (!aspectOpt.isPresent()) continue;

            AspectEffect aspect = aspectOpt.get();
            dev.overgrown.thaumaturge.spell.modifier.ModifierEffect modifier =
                    dev.overgrown.thaumaturge.spell.modifier.ModifierRegistry.get(entry.getValue());

            List<dev.overgrown.thaumaturge.spell.modifier.ModifierEffect> mods = modifier != null ?
                    Collections.singletonList(modifier) : this.modifiers;

            // Create block-targeted delivery
            TargetedSpellDelivery delivery = new TargetedSpellDelivery(caster, pos, net.minecraft.util.math.Direction.UP);
            delivery.setModifiers(mods);
            aspect.applyTargeted(delivery);
        }
    }

    private ServerPlayerEntity getCaster() {
        if (this.casterUuid != null && this.getWorld() instanceof ServerWorld) {
            return ((ServerWorld) this.getWorld()).getServer().getPlayerManager().getPlayer(this.casterUuid);
        }
        return null;
    }

    public float getRadius() {
        return this.dataTracker.get(RADIUS);
    }

    public int getDuration() {
        return this.dataTracker.get(DURATION);
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        this.dataTracker.set(DURATION, nbt.getInt("Duration"));
        this.dataTracker.set(RADIUS, nbt.getFloat("Radius"));
        this.dataTracker.set(APPLY_INTERVAL, nbt.getInt("ApplyInterval"));
        this.dataTracker.set(AGE, nbt.getInt("Age"));

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

        // Load modifiers
        this.modifiers.clear();
        NbtList modifiersList = nbt.getList("Modifiers", 10);
        for (int i = 0; i < modifiersList.size(); i++) {
            NbtCompound modifierCompound = modifiersList.getCompound(i);
            Identifier modifierId = new Identifier(modifierCompound.getString("ModifierId"));
            dev.overgrown.thaumaturge.spell.modifier.ModifierEffect modifier =
                    dev.overgrown.thaumaturge.spell.modifier.ModifierRegistry.get(modifierId);
            if (modifier != null) {
                this.modifiers.add(modifier);
            }
        }
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        nbt.putInt("Duration", this.dataTracker.get(DURATION));
        nbt.putFloat("Radius", this.dataTracker.get(RADIUS));
        nbt.putInt("ApplyInterval", this.dataTracker.get(APPLY_INTERVAL));
        nbt.putInt("Age", this.dataTracker.get(AGE));

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

        // Save modifiers
        NbtList modifiersList = new NbtList();
        for (dev.overgrown.thaumaturge.spell.modifier.ModifierEffect modifier : this.modifiers) {
            // Find the modifier ID - this is a bit hacky but works with current registry
            var modifierId = dev.overgrown.thaumaturge.spell.modifier.ModifierRegistry.ids().stream()
                    .filter(id -> dev.overgrown.thaumaturge.spell.modifier.ModifierRegistry.get(id) == modifier)
                    .findFirst();
            if (modifierId.isPresent()) {
                NbtCompound modifierCompound = new NbtCompound();
                modifierCompound.putString("ModifierId", modifierId.get().toString());
                modifiersList.add(modifierCompound);
            }
        }
        nbt.put("Modifiers", modifiersList);
    }

    @Override
    public boolean isInvisible() {
        return true; // Cloud is invisible, only particles are visible
    }

    @Override
    public boolean isInvulnerable() {
        return true;
    }

    public boolean collides() {
        return false; // Cloud doesn't collide with anything
    }
}