package dev.overgrown.thaumaturge.spell.input;

import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

/**
 * Simple visitor used by the gauntlet input mixin to distinguish an ATTACK
 * packet from INTERACT / INTERACT_AT packets.
 *
 * <p>Lives outside the mixin package because Mixin forbids non-{@code @Mixin}
 * classes in the mixin package from being referenced directly at runtime.
 */
public final class AttackDetector implements PlayerInteractEntityC2SPacket.Handler {

    boolean isAttack = false;

    @Override
    public void interact(Hand hand) {
        // right-click interact — not an attack
    }

    @Override
    public void interactAt(Hand hand, Vec3d pos) {
        // right-click interact-at — not an attack
    }

    @Override
    public void attack() {
        isAttack = true;
    }
}
