package dev.overgrown.thaumaturge.spell.component;

import dev.overgrown.aspectslib.spell.Spell;
import dev.overgrown.aspectslib.spell.SpellContext;
import dev.overgrown.aspectslib.spell.SpellShape;
import dev.overgrown.aspectslib.spell.cost.SpellCostParams;
import dev.overgrown.aspectslib.spell.notation.TerminationCondition;
import net.minecraft.util.Identifier;

/**
 * Lightweight {@link Spell} adapter that wraps a {@link GauntletSpellEffect}
 * so the full AspectsLib casting pipeline (Law validation, modifier hooks,
 * stability checks) can be used from the gauntlet system.
 *
 * <p>This class is <em>not</em> registered in {@code SpellRegistry} since it exists only to provide a valid {@link SpellContext} for a single cast
 */
public final class GauntletSpellAdapter extends Spell {

    private final GauntletSpellEffect effect;
    private GauntletSpellEffect.GauntletCastContext pendingCtx;
    private boolean lastResult;

    public GauntletSpellAdapter(GauntletSpellEffect effect) {
        super(SpellShape.point());
        this.effect = effect;
    }

    @Override
    public Identifier getId() {
        return effect.getAspectId();
    }

    @Override
    protected SpellCostParams buildCostParams(SpellContext ctx) {
        SpellCostParams.Builder builder = new SpellCostParams.Builder()
                .range(effect.getDefaultRange())
                .duration(effect.getDefaultDuration());
        effect.getAspectIntensities().forEach(builder::aspect);
        return builder.build();
    }

    @Override
    public TerminationCondition createTerminationCondition() {
        // Most gauntlet effects are INSTANT with no termination needed
        return null;
    }

    @Override
    public boolean execute(SpellContext ctx) {
        if (pendingCtx == null) return false;
        lastResult = effect.apply(pendingCtx);
        return lastResult;
    }

    /**
     * Sets the gauntlet cast context that will be used when {@link #execute} is called by the AspectsLib pipeline.
     */
    public void setPendingContext(GauntletSpellEffect.GauntletCastContext ctx) {
        this.pendingCtx = ctx;
    }

    public boolean getLastResult() {
        return lastResult;
    }
}
