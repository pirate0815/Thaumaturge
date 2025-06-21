package dev.overgrown.thaumaturge.utils;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class ModSounds {
    public static void initialize() {
        // Initialization logic if needed
    }

    private static SoundEvent register(String name) {
        Identifier id = Identifier.of("thaumaturge", name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static final SoundEvent AER_SPELL_CAST = register("aer_spell_cast");
    public static final SoundEvent ALIENIS_SPELL_CAST = register("alienis_spell_cast");
    public static final SoundEvent GELUM_SPELL_CAST = register("gelum_spell_cast");
    public static final SoundEvent POTENTIA_SPELL_CAST = register("potentia_spell_cast");
    public static final SoundEvent VICTUS_SPELL_CAST = register("victus_spell_cast");
}