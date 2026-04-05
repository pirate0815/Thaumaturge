package dev.overgrown.aspectslib.spell.perception;

/**
 * Holds the Aether Sight development state for a practitioner entity.
 *
 * <h3>Aether Sight stages</h3>
 * <table border="1">
 *   <tr><th>Stage</th><th>Required practice</th><th>Capability</th></tr>
 *   <tr><td>0 — None</td><td>None</td><td>Nothing; world is magically opaque</td></tr>
 *   <tr><td>1 — Flicker</td><td>~3 years Staff use</td>
 *       <td>Involuntary glimpses; triggers on strong Aspect concentrations only</td></tr>
 *   <tr><td>2 — Ambient</td><td>~7 years</td>
 *       <td>Controlled activation (short bursts); dominant Aspects only</td></tr>
 *   <tr><td>3 — Functional</td><td>~12 years</td>
 *       <td>Sustained; full Aspect color; basic flow direction</td></tr>
 *   <tr><td>4 — Deep</td><td>~20 years</td>
 *       <td>Near always-on; full flow mapping; historical echo begins</td></tr>
 *   <tr><td>5 — Immersive</td><td>~30+ years</td>
 *       <td>Cannot be reliably switched off; membrane stress detection</td></tr>
 * </table>
 *
 * <p>Stage 0–1 is granted by Aspect Lens / Resonance Goggles (tools).
 * Stage 2+ is acquired organically through the
 * {@link dev.overgrown.aspectslib.mixin.spell.LivingEntityAetherMixin}
 * accumulation mechanic.
 */
public final class AetherSightData {

    private int stage;
    /** Accumulated Staff-use ticks that advance stage development. */
    private long practiceTicksAccumulated;
    /** Whether Sight is currently voluntarily active (toggled by the practitioner). */
    private boolean active;
    /**
     * Cognitive fatigue in [0.0, 1.0].
     * Stage 4–5 practitioners accumulate fatigue when the world contains
     * very complex Aspect fields; high fatigue reduces functional detail.
     */
    private float cognitiveFatigue;

    public AetherSightData() {
        this.stage                    = 0;
        this.practiceTicksAccumulated = 0L;
        this.active                   = false;
        this.cognitiveFatigue         = 0f;
    }

    // Stage thresholds in ticks
    // 1 MC day ≈ 24 000 ticks; assume ~6 h play/day on average
    private static final long[] STAGE_THRESHOLDS = {
            0L,
            216_000_000L,  // Stage 1: ~3 in-world years of Staff use
            504_000_000L,  // Stage 2: ~7 years
            864_000_000L,  // Stage 3: ~12 years
            1_440_000_000L,  // Stage 4: ~20 years
            2_160_000_000L   // Stage 5: ~30 years
    };

    /**
     * Adds practice ticks and re-evaluates stage.
     * Call once per tick while the entity has a Staff equipped.
     */
    public void addPracticeTicks(long ticks) {
        practiceTicksAccumulated += ticks;
        recalculateStage();
    }

    private void recalculateStage() {
        for (int s = STAGE_THRESHOLDS.length - 1; s >= 0; s--) {
            if (practiceTicksAccumulated >= STAGE_THRESHOLDS[s]) {
                stage = s;
                return;
            }
        }
        stage = 0;
    }

    /**
     * Attempts to activate Aether Sight.
     *
     * @return {@code true} if activation succeeded (stage ≥ 2 required for
     *         voluntary activation; Stage 5 is always active)
     */
    public boolean activate() {
        if (stage == 0 || stage == 1) return false; // Cannot voluntarily activate yet
        this.active = true;
        return true;
    }

    public void deactivate() {
        if (stage < 5) this.active = false;
        // Stage 5: cannot reliably deactivate (request ignored silently)
    }

    /**
     * Returns whether Aether Sight is currently rendering for this entity.
     * Always {@code true} at Stage 5.
     */
    public boolean isActive() {
        return stage == 5 || active;
    }

    // Cognitive fatigue:
    /**
     * Increases cognitive fatigue when exposed to high-complexity Aspect fields
     * (used by rendering/UI systems).
     */
    public void addFatigue(float amount) {
        cognitiveFatigue = Math.min(1f, cognitiveFatigue + amount);
    }

    public void recoverFatigue(float amount) {
        cognitiveFatigue = Math.max(0f, cognitiveFatigue - amount);
    }

    /** Accessors */
    public int getStage() {
        return stage;
    }

    public long getPracticeTicksAccumulated() {
        return practiceTicksAccumulated;
    }

    public float getCognitiveFatigue() {
        return cognitiveFatigue;
    }

    /** Returns true when the entity has any Aether Sight capability (stage ≥ 1). */
    public boolean hasSight() {
        return stage >= 1;
    }

    /** Returns true when Aether Sight can show basic Resonance relationships (stage ≥ 3). */
    public boolean canSeeResonance() {
        return stage >= 3;
    }

    /** Returns true when historical echo is available (stage ≥ 4). */
    public boolean canSeeHistory() {
        return stage >= 4;
    }

    /** NBT persistence */
    public net.minecraft.nbt.NbtCompound toNbt() {
        net.minecraft.nbt.NbtCompound nbt = new net.minecraft.nbt.NbtCompound();
        nbt.putInt ("AetherSightStage", stage);
        nbt.putLong("AetherSightPractice", practiceTicksAccumulated);
        nbt.putFloat("AetherSightFatigue", cognitiveFatigue);
        return nbt;
    }

    public static AetherSightData fromNbt(net.minecraft.nbt.NbtCompound nbt) {
        AetherSightData d = new AetherSightData();
        if (nbt.contains("AetherSightStage")) d.stage = nbt.getInt("AetherSightStage");
        if (nbt.contains("AetherSightPractice")) d.practiceTicksAccumulated = nbt.getLong("AetherSightPractice");
        if (nbt.contains("AetherSightFatigue")) d.cognitiveFatigue = nbt.getFloat("AetherSightFatigue");
        d.recalculateStage();
        return d;
    }
}