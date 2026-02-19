package dev.overgrown.aspectslib.spell.conduit;

import dev.overgrown.aspectslib.AspectsLib;
import dev.overgrown.aspectslib.spell.Spell;
import dev.overgrown.aspectslib.spell.SpellContext;
import dev.overgrown.aspectslib.spell.SpellRegistry;
import dev.overgrown.aspectslib.spell.modifier.SpellModifier;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.Collections;
import java.util.List;

public final class ConduitDispatcher {

    private ConduitDispatcher() {}

    public static boolean dispatch(LivingEntity caster, ItemStack conduitStack) {
        if (!(caster.getWorld() instanceof ServerWorld serverWorld)) {
            AspectsLib.LOGGER.warn("ConduitDispatcher.dispatch called on client side — ignored.");
            return false;
        }
        return dispatchInternal(caster, conduitStack, serverWorld);
    }

    public static boolean dispatch(LivingEntity caster, ItemStack conduitStack,
                                   ServerWorld serverWorld) {
        return dispatchInternal(caster, conduitStack, serverWorld);
    }

    private static boolean dispatchInternal(LivingEntity caster,
                                            ItemStack conduitStack,
                                            ServerWorld world) {
        if (!ConduitRegistry.isValidConduit(conduitStack)) {
            AspectsLib.LOGGER.debug("Dispatch rejected: {} is not a valid conduit.",
                    conduitStack.getItem().getTranslationKey());
            return false;
        }

        var spellIdStr = conduitStack.getOrCreateNbt().getString("SpellId");
        var spellId    = spellIdStr.isBlank() ? null
                : net.minecraft.util.Identifier.tryParse(spellIdStr);

        if (conduitStack.getItem() instanceof IConduit ic) {
            spellId = ic.getStoredSpellId(conduitStack);
        }

        if (spellId == null) {
            AspectsLib.LOGGER.debug("Dispatch rejected: conduit has no stored spell id.");
            return false;
        }

        Spell spell = SpellRegistry.get(spellId).orElse(null);
        if (spell == null) {
            AspectsLib.LOGGER.warn("Dispatch rejected: spell {} is not registered.", spellId);
            return false;
        }

        if (conduitStack.getItem() instanceof IConduit ic && !ic.canCast(conduitStack, caster)) {
            AspectsLib.LOGGER.debug("Dispatch rejected: IConduit#canCast returned false.");
            return false;
        }

        List<SpellModifier> modifiers = resolveModifiers(conduitStack);

        Vec3d castOrigin = caster.getEyePos();
        SpellContext ctx = buildContext(world, caster, castOrigin, conduitStack, modifiers, spell);

        boolean succeeded = spell.cast(ctx);

        if (conduitStack.getItem() instanceof IConduit ic) {
            ic.onSpellCast(conduitStack, ctx);
        }

        return succeeded;
    }

    private static List<SpellModifier> resolveModifiers(ItemStack stack) {
        if (stack.getItem() instanceof IConduit ic) {
            return ic.getStoredModifiers(stack);
        }
        return Collections.emptyList();
    }

    private static SpellContext buildContext(ServerWorld world,
                                             LivingEntity caster,
                                             Vec3d castOrigin,
                                             ItemStack conduitStack,
                                             List<SpellModifier> modifiers,
                                             Spell spell) {
        double range = spell.getBaseMetadata().getRange();
        Vec3d lookDir = caster.getRotationVec(1.0f);
        Vec3d rayEnd  = castOrigin.add(lookDir.multiply(range));

        Box sweepBox = caster.getBoundingBox()
                .stretch(lookDir.multiply(range))
                .expand(1.0);

        Entity primaryTarget = null;
        double closestSq     = range * range + 1;

        for (Entity candidate : world.getOtherEntities(caster, sweepBox,
                e -> e instanceof LivingEntity && e.isAlive())) {
            Box expandedBox = candidate.getBoundingBox().expand(0.3);
            var hitResult   = expandedBox.raycast(castOrigin, rayEnd);
            if (hitResult.isPresent()) {
                double dSq = castOrigin.squaredDistanceTo(hitResult.get());
                if (dSq < closestSq) {
                    closestSq     = dSq;
                    primaryTarget = candidate;
                }
            }
        }

        // Pass the spell to the builder constructor
        SpellContext.Builder builder = new SpellContext.Builder(world, caster, spell)
                .conduit(conduitStack)
                .castOrigin(castOrigin)
                .modifiers(modifiers);

        if (primaryTarget != null) {
            builder.addEntityTarget(primaryTarget);
        }

        return builder.build();
    }
}