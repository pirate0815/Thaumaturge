package dev.overgrown.thaumaturge.spell.tier;

import dev.overgrown.thaumaturge.spell.modifier.ModifierEffect;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Delivery for self-cast spells.
 */
public final class SelfSpellDelivery {

    private final ServerPlayerEntity caster;
    private List<ModifierEffect> modifiers = List.of();

    public SelfSpellDelivery(ServerPlayerEntity caster) {
        this.caster = Objects.requireNonNull(caster, "caster");
    }

    // Back-compat with old callsites that passed unused context args.
    @SuppressWarnings("unused")
    public SelfSpellDelivery(ServerPlayerEntity caster, Object _unused1, Object _unused2) {
        this(caster);
    }

    public ServerPlayerEntity getCaster() {
        return caster;
    }

    public List<ModifierEffect> getModifiers() {
        return modifiers;
    }

    public void setModifiers(List<ModifierEffect> mods) {
        if (mods == null || mods.isEmpty()) {
            this.modifiers = List.of();
        } else {
            this.modifiers = List.copyOf(new ArrayList<>(mods));
        }
    }
}
