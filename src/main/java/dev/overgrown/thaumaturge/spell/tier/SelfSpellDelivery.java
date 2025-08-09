package dev.overgrown.thaumaturge.spell.tier;

import dev.overgrown.thaumaturge.spell.pattern.AspectEffect;
import dev.overgrown.thaumaturge.spell.modifier.ModifierEffect;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;
import java.util.Objects;

/**
 * Delivery strategy for self-cast spells.
 */
public final class SelfSpellDelivery {

    private final ServerPlayerEntity caster;

    public SelfSpellDelivery(ServerPlayerEntity caster) {
        this.caster = Objects.requireNonNull(caster, "caster");
    }

    // Back-compat with older call sites that passed unused context args.
    @SuppressWarnings("unused")
    public SelfSpellDelivery(ServerPlayerEntity caster, Object _unused1, Object _unused2) {
        this(caster);
    }

    public void deliver(AspectEffect aspect, List<ModifierEffect> modifiers) {
        aspect.castOnSelf(caster, modifiers == null ? List.of() : modifiers);
    }

    public ServerPlayerEntity getCaster() { return caster; }
}
