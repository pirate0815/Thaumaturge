package dev.overgrown.thaumaturge.spell.impl.vinculum.entity;

import dev.overgrown.thaumaturge.spell.component.GauntletSpellEffect;
import dev.overgrown.thaumaturge.spell.component.GauntletSpellEffectRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.*;

/**
 * Proximity-triggered arcane mine that stores a focus's spell payload and
 * fires it when any entity steps within the detection box.
 *
 * <h3>Stored data</h3>
 * <ul>
 *   <li>{@code storedAspects} — maps Aspect ID to Modifier ID (mirrors what
 *       the gauntlet's focus holds at deploy time).</li>
 *   <li>{@code spellTier} — the focus tier used at deploy time.</li>
 * </ul>
 *
 * <p>When triggered, {@link #applyStoredEffects} looks up each aspect in
 * {@link GauntletSpellEffectRegistry} and calls
 * {@link GauntletSpellEffect#apply} with a context targeting the trigger entity.
 */
public class ArcaneMineEntity extends Entity {

    private static final TrackedData<Integer> ARMING_TIME =
            DataTracker.registerData(ArcaneMineEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Boolean> ARMED =
            DataTracker.registerData(ArcaneMineEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> TRIGGERED =
            DataTracker.registerData(ArcaneMineEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    private UUID casterUuid;
    private final Map<Identifier, Identifier> storedAspects; // aspect → modifier
    private String spellTier;
    private int triggerCooldown = 0;

    // ── Constructors ──────────────────────────────────────────────────────────

    public ArcaneMineEntity(EntityType<?> type, World world) {
        super(type, world);
        this.storedAspects = new LinkedHashMap<>();
        this.spellTier     = "advanced";
        this.setInvulnerable(true);
        this.setNoGravity(true);
    }

    public ArcaneMineEntity(World world, ServerPlayerEntity caster,
                            Map<Identifier, Identifier> aspects, String tier) {
        this(dev.overgrown.thaumaturge.registry.ModEntities.ARCANE_MINE, world);
        this.casterUuid = caster.getUuid();
        this.storedAspects.putAll(aspects);
        this.spellTier = tier;
        this.setPosition(caster.getX(), caster.getY(), caster.getZ());
        this.dataTracker.set(ARMING_TIME, 40);
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
            if (!this.isArmed() && !this.isTriggered()) {
                for (int i = 0; i < 2; i++) {
                    double ox = (this.random.nextDouble() - 0.5) * 0.5;
                    double oz = (this.random.nextDouble() - 0.5) * 0.5;
                    this.getWorld().addParticle(ParticleTypes.REVERSE_PORTAL,
                            this.getX() + ox, this.getY() + 0.1, this.getZ() + oz,
                            0, 0.1, 0);
                }
            }
            return;
        }

        if (this.isTriggered()) {
            if (--triggerCooldown <= 0) this.discard();
            return;
        }

        if (!this.isArmed()) {
            int t = this.dataTracker.get(ARMING_TIME);
            if (t > 0) {
                this.dataTracker.set(ARMING_TIME, t - 1);
            } else {
                this.dataTracker.set(ARMED, true);
                this.getWorld().playSound(null, this.getBlockPos(),
                        SoundEvents.BLOCK_RESPAWN_ANCHOR_SET_SPAWN,
                        getSoundCategory(), 0.5f, 1.0f);
            }
        } else {
            Box det = new Box(getX() - 0.5, getY(), getZ() - 0.5,
                    getX() + 0.5, getY() + 0.5, getZ() + 0.5);
            List<Entity> candidates = this.getWorld().getOtherEntities(this, det,
                    e -> e.isAlive() && !e.isSpectator());
            if (!candidates.isEmpty()) {
                trigger(candidates.get(0));
            }
        }
    }

    private void trigger(Entity triggerEntity) {
        if (this.isTriggered() || !this.isArmed()) return;

        this.dataTracker.set(TRIGGERED, true);
        this.triggerCooldown = 10;

        this.getWorld().playSound(null, this.getBlockPos(),
                SoundEvents.ENTITY_GENERIC_EXPLODE, getSoundCategory(), 0.7f, 1.2f);

        if (this.getWorld() instanceof ServerWorld sw) {
            sw.spawnParticles(ParticleTypes.EXPLOSION,
                    getX(), getY() + 0.5, getZ(), 5, 0.5, 0.5, 0.5, 0.1);
        }

        ServerPlayerEntity caster = getCaster();
        if (caster != null && !storedAspects.isEmpty()) {
            applyStoredEffects(caster, triggerEntity);
        }
    }

    /**
     * Applies each stored aspect's effect to the trigger entity using the {@link GauntletSpellEffectRegistry}.
     */
    private void applyStoredEffects(ServerPlayerEntity caster, Entity triggerEntity) {
        if (!(getWorld() instanceof ServerWorld serverWorld)) return;

        for (Map.Entry<Identifier, Identifier> entry : storedAspects.entrySet()) {
            Identifier aspectId   = entry.getKey();
            Identifier modifierId = entry.getValue();

            GauntletSpellEffectRegistry.get(aspectId).ifPresent(effect -> {
                List<Entity> targets = switch (spellTier) {
                    case "lesser"  -> caster.isAlive() ? List.of(caster) : List.of();
                    case "greater" -> {
                        // AoE around the mine
                        Box aoeBox = new Box(getX() - 3, getY() - 1, getZ() - 3,
                                getX() + 3, getY() + 2, getZ() + 3);
                        yield getWorld().getOtherEntities(caster, aoeBox,
                                e -> e.isAlive() && !e.isSpectator());
                    }
                    default        -> triggerEntity.isAlive() ? List.of(triggerEntity) : List.of();
                };

                List<Identifier> mods = modifierId != null ? List.of(modifierId) : List.of();

                GauntletSpellEffect.GauntletCastContext ctx =
                        new GauntletSpellEffect.GauntletCastContext(
                                caster,
                                serverWorld,
                                getPos(),
                                targets,
                                List.of(),
                                spellTier,
                                mods,
                                1.0,
                                false
                        );

                effect.apply(ctx);
            });
        }
    }

    public boolean isArmed() {
        return this.dataTracker.get(ARMED);
    }

    public boolean isTriggered(){
        return this.dataTracker.get(TRIGGERED);
    }

    @Override
    public boolean isInvisible() {
        return true;
    }

    @Override
    public boolean isInvulnerable() {
        return true;
    }

    public boolean collides() {
        return !this.isTriggered();
    }

    private ServerPlayerEntity getCaster() {
        if (casterUuid != null && getWorld() instanceof ServerWorld sw) {
            return sw.getServer().getPlayerManager().getPlayer(casterUuid);
        }
        return null;
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        if (!getType().isSaveable()) return;
        this.dataTracker.set(ARMING_TIME, nbt.getInt("ArmingTime"));
        this.dataTracker.set(ARMED, nbt.getBoolean("Armed"));
        this.dataTracker.set(TRIGGERED, nbt.getBoolean("Triggered"));
        this.spellTier = nbt.getString("SpellTier");
        if (nbt.containsUuid("CasterUUID")) this.casterUuid = nbt.getUuid("CasterUUID");

        this.storedAspects.clear();
        NbtList list = nbt.getList("StoredAspects", 10);
        for (int i = 0; i < list.size(); i++) {
            NbtCompound c = list.getCompound(i);
            this.storedAspects.put(new Identifier(c.getString("Aspect")),
                    new Identifier(c.getString("Modifier")));
        }
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        if (!getType().isSaveable()) return;
        nbt.putInt    ("ArmingTime", this.dataTracker.get(ARMING_TIME));
        nbt.putBoolean("Armed",      this.dataTracker.get(ARMED));
        nbt.putBoolean("Triggered",  this.dataTracker.get(TRIGGERED));
        nbt.putString ("SpellTier",  this.spellTier);
        if (this.casterUuid != null) nbt.putUuid("CasterUUID", this.casterUuid);

        NbtList list = new NbtList();
        for (Map.Entry<Identifier, Identifier> e : storedAspects.entrySet()) {
            NbtCompound c = new NbtCompound();
            c.putString("Aspect",   e.getKey().toString());
            c.putString("Modifier", e.getValue().toString());
            list.add(c);
        }
        nbt.put("StoredAspects", list);
    }
}