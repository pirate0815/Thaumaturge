package dev.overgrown.aspectslib.spell;

import dev.overgrown.aspectslib.spell.modifier.SpellModifier;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.*;

/**
 * {@code SpellContext} holds all the dynamic information about a single spell cast.
 * It is created by the conduit or casting handler
 * just before a spell is executed and is passed to every stage of the spell's
 * lifecycle (modifier hooks, execution, etc.).
 *
 * <p>The context combines three main categories of data:
 * <ul>
 *   <li><b>Casting environment</b> – who is casting, in which world, from what position.</li>
 *   <li><b>Spell definition</b> – the {@link Spell} being cast, its pattern and modifiers.</li>
 *   <li><b>Targets</b> – entities, blocks, or areas that the spell will affect.</li>
 *   <li><b>Spell parameters</b> – the mutable {@link SpellMetadata} that reflects the final stats after all modifiers have been applied.</li>
 * </ul>
 *
 * <p>In the current system, the context works alongside the existing delivery objects
 * ({@link SelfSpellDelivery}, {@link TargetedSpellDelivery}, {@link AoeSpellDelivery}).
 * Those deliveries contain the caster, world, and concrete target information; the
 * context adds the spell‑specific data and a flexible data map for temporary storage.
 *
 * <p>Modifiers can read from and write to the context's data map to pass
 * information between the pre‑ and post‑execute phases, or to alter the
 * behavior of the spell's execution.
 */
public final class SpellContext {

    // Core immutable fields
    private final ServerWorld world;
    private final LivingEntity caster;
    private final Spell spell;
    private final ItemStack conduit;
    private final Vec3d castOrigin;
    private final SpellPattern pattern;
    private final List<SpellModifier> modifiers;
    private final SpellMetadata metadata;
    private final EnvironmentalResonance resonance;

    // Targets
    private final List<Entity> entityTargets = new ArrayList<>();
    private final List<BlockTarget> blockTargets = new ArrayList<>();

    // Temporary data (for modifiers)
    private final Map<String, Object> data = new HashMap<>();

    // Private constructor (use Builder)
    private SpellContext(Builder builder) {
        this.world = builder.world;
        this.caster = builder.caster;
        this.spell = builder.spell;
        this.conduit = builder.conduit;
        this.castOrigin = builder.castOrigin;
        this.pattern = builder.pattern;
        this.modifiers = builder.modifiers != null
                ? List.copyOf(builder.modifiers)
                : List.of();
        this.metadata = builder.metadata != null
                ? builder.metadata
                : SpellMetadata.DEFAULT;
        this.resonance = builder.resonance != null
                ? builder.resonance
                : new EnvironmentalResonance();

        this.entityTargets.addAll(builder.entityTargets);
        this.blockTargets.addAll(builder.blockTargets);
    }

    // Getters
    public ServerWorld getWorld() {
        return world;
    }
    public LivingEntity getCaster() {
        return caster;
    }
    public Spell getSpell() {
        return spell;
    }
    public ItemStack getConduit() {
        return conduit;
    }
    public Vec3d getCastOrigin() {
        return castOrigin;
    }
    public SpellPattern getPattern() {
        return pattern;
    }
    public List<SpellModifier> getModifiers() {
        return modifiers;
    }
    public SpellMetadata getMetadata() {
        return metadata;
    }
    public EnvironmentalResonance getResonance() {
        return resonance;
    }

    // Target access
    public List<Entity> getEntityTargets() { return Collections.unmodifiableList(entityTargets); }
    public List<BlockTarget> getBlockTargets() { return Collections.unmodifiableList(blockTargets); }
    public List<Object> getAllTargets() {
        List<Object> all = new ArrayList<>(entityTargets.size() + blockTargets.size());
        all.addAll(entityTargets);
        all.addAll(blockTargets);
        return Collections.unmodifiableList(all);
    }
    public Optional<Entity> getFirstEntityTarget() {
        return entityTargets.isEmpty() ? Optional.empty() : Optional.of(entityTargets.get(0));
    }
    public Optional<BlockTarget> getFirstBlockTarget() {
        return blockTargets.isEmpty() ? Optional.empty() : Optional.of(blockTargets.get(0));
    }

    // Temporary data storage (for modifiers)
    public void putData(String key, Object value) { data.put(key, value); }
    @SuppressWarnings("unchecked")
    public <T> T getData(String key, T defaultValue) {
        return (T) data.getOrDefault(key, defaultValue);
    }
    public boolean hasData(String key) { return data.containsKey(key); }

    // Builder
    public static final class Builder {
        // Required
        private final ServerWorld world;
        private final LivingEntity caster;
        private final Spell spell;

        // Optional
        private ItemStack conduit = ItemStack.EMPTY;
        private Vec3d castOrigin;
        private SpellPattern pattern;
        private List<SpellModifier> modifiers;
        private SpellMetadata metadata;
        private EnvironmentalResonance resonance;
        private final List<Entity> entityTargets = new ArrayList<>();
        private final List<BlockTarget> blockTargets = new ArrayList<>();

        /**
         * Start building a context for a cast in the given world by the given caster
         * using the given spell.
         */
        public Builder(ServerWorld world, LivingEntity caster, Spell spell) {
            this.world = Objects.requireNonNull(world, "world must not be null");
            this.caster = Objects.requireNonNull(caster, "caster must not be null");
            this.spell = Objects.requireNonNull(spell, "spell must not be null");
            this.castOrigin = caster.getEyePos();
        }

        public Builder conduit(ItemStack stack) {
            this.conduit = stack != null ? stack : ItemStack.EMPTY;
            return this;
        }
        public Builder castOrigin(Vec3d origin) {
            this.castOrigin = Objects.requireNonNull(origin, "castOrigin must not be null");
            return this;
        }
        public Builder pattern(SpellPattern pattern) {
            this.pattern = pattern;
            return this;
        }
        public Builder modifiers(List<SpellModifier> modifiers) {
            this.modifiers = modifiers != null ? new ArrayList<>(modifiers) : null;
            return this;
        }
        public Builder metadata(SpellMetadata metadata) {
            this.metadata = metadata;
            return this;
        }
        public Builder resonance(EnvironmentalResonance resonance) {
            this.resonance = resonance;
            return this;
        }
        public Builder addEntityTarget(Entity entity) {
            if (entity != null) entityTargets.add(entity);
            return this;
        }
        public Builder addEntityTargets(Collection<? extends Entity> entities) {
            if (entities != null) entities.stream().filter(Objects::nonNull).forEach(entityTargets::add);
            return this;
        }
        public Builder addBlockTarget(BlockPos pos, Direction face) {
            if (pos != null && face != null) blockTargets.add(new BlockTarget(pos, face));
            return this;
        }
        public Builder addBlockTargets(Collection<BlockTarget> targets) {
            if (targets != null) targets.stream().filter(Objects::nonNull).forEach(blockTargets::add);
            return this;
        }

        public SpellContext build() {
            return new SpellContext(this);
        }
    }

    public record BlockTarget(BlockPos pos, Direction face) {}
}