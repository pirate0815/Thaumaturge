package dev.overgrown.thaumaturge.registry;

import dev.overgrown.thaumaturge.Thaumaturge;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class ModSounds {
    public static final SoundEvent ASPECT_CLUSTER_AMBIENT = register("aspect_cluster_ambient");
    public static final SoundEvent AER_SPELL_CAST = register("aer_spell_cast");
    public static final SoundEvent MOTUS_SPELL_CAST = register("motus_spell_cast");
    public static final SoundEvent VICTUS_SPELL_CAST = register("victus_spell_cast");
    public static final SoundEvent ALIENIS_SPELL_CAST = register("alienis_spell_cast");
    public static final SoundEvent GELUM_SPELL_CAST = register("gelum_spell_cast");
    public static final SoundEvent POTENTIA_SPELL_CAST = register("potentia_spell_cast");

    private static SoundEvent register(String path) {
        Identifier id = Thaumaturge.identifier(path);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void initialize() {
        Thaumaturge.LOGGER.info("Registering Thaumaturge sounds");
    }
}