package dev.overgrown.thaumaturge.spell.component;

import dev.overgrown.aspectslib.aether.AetherAPI;
import dev.overgrown.aspectslib.aether.AetherManager;
import dev.overgrown.aspectslib.aspects.data.AspectData;
import dev.overgrown.aspectslib.spell.SpellContext;
import dev.overgrown.aspectslib.spell.SpellMetadata;
import dev.overgrown.aspectslib.spell.SpellPattern;
import dev.overgrown.aspectslib.spell.aether.PersonalAetherPool;
import dev.overgrown.aspectslib.spell.cost.AetherCostCalculator;
import dev.overgrown.aspectslib.spell.cost.ResonanceDiscountCalculator;
import dev.overgrown.aspectslib.spell.cost.SpellCostParams;
import dev.overgrown.aspectslib.spell.law.SpellLawValidator;
import dev.overgrown.aspectslib.spell.modifier.ModifierRegistry;
import dev.overgrown.aspectslib.spell.modifier.SpellModifier;
import dev.overgrown.aspectslib.spell.unraveling.UnravelingStage;
import dev.overgrown.aspectslib.spell.unraveling.UnravelingTracker;
import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.item.focus.FocusItem;
import dev.overgrown.thaumaturge.spell.focal.SpellComponentDefinition;
import dev.overgrown.thaumaturge.spell.focal.SpellComponentType;
import dev.overgrown.thaumaturge.spell.focal.SpellNode;
import dev.overgrown.thaumaturge.spell.input.ComboPattern;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stateless helper that drives the full gauntlet cast pipeline.
 * <p>
 * Target acquisition is now driven by the medium chain in the focus's spell tree,
 * not by focus tier. Cooldown is proportional to spell complexity.
 */
public final class GauntletCastHelper {

    private GauntletCastHelper() {}

    /** Cooldown tracking */
    private static final Map<UUID, Long> COOLDOWNS = new ConcurrentHashMap<>();

    /** Linear cooldown: 0.5s base + 0.1s per complexity point. */
    public static int computeCooldownTicks(int complexity) {
        return 10 + complexity * 2;
    }

    public static void clearCooldown(UUID playerId) {
        COOLDOWNS.remove(playerId);
    }

    /** Main entry point */
    public static void tryFire(ItemStack gauntletStack,
                               NbtList foci,
                               ComboPattern pattern,
                               ServerPlayerEntity player) {

        int slotIndex = pattern.getSlotIndex();
        Thaumaturge.LOGGER.info("[Gauntlet] tryFire: pattern={}, slotIndex={}, fociCount={}",
                pattern, slotIndex, foci.size());

        if (slotIndex >= foci.size()) {
            player.sendMessage(Text.translatable("gauntlet.thaumaturge.no_focus", slotIndex + 1), true);
            return;
        }

        // 1. Deserialize focus
        NbtCompound focusNbt = foci.getCompound(slotIndex);
        ItemStack focusStack = ItemStack.fromNbt(focusNbt);
        if (!(focusStack.getItem() instanceof FocusItem focus)) {
            player.sendMessage(Text.translatable("gauntlet.thaumaturge.invalid_focus"), true);
            return;
        }

        String tier = focus.getTier();
        Identifier aspectId = focus.getAspect(focusStack);
        Identifier modId = focus.getModifier(focusStack);

        Thaumaturge.LOGGER.info("[Gauntlet] Focus: tier={}, aspect={}, modifier={}", tier, aspectId, modId);

        if (aspectId == null || aspectId.equals(Thaumaturge.identifier("null"))) {
            player.sendMessage(Text.translatable("gauntlet.thaumaturge.no_aspect"), true);
            return;
        }

        // 2. Look up the effect
        Optional<GauntletSpellEffect> effectOpt = GauntletSpellEffectRegistry.get(aspectId);
        if (effectOpt.isEmpty()) {
            player.sendMessage(Text.translatable("gauntlet.thaumaturge.no_effect", aspectId), true);
            return;
        }
        GauntletSpellEffect effect = effectOpt.get();
        ServerWorld world = (ServerWorld) player.getWorld();

        // 3. Deserialize spell tree and compute complexity
        NbtCompound stackNbt = focusStack.getNbt();
        SpellNode spellTree = null;
        int complexity = 0;
        if (stackNbt != null && stackNbt.contains("SpellTree")) {
            spellTree = SpellNode.fromNbt(stackNbt.getCompound("SpellTree"));
            if (spellTree != null) {
                complexity = spellTree.computeComplexity();
            }
        }
        if (complexity == 0 && stackNbt != null) {
            complexity = stackNbt.getInt("SpellComplexity");
        }

        // 4. Cooldown check
        long now = world.getTime();
        Long cooldownExpires = COOLDOWNS.get(player.getUuid());
        if (cooldownExpires != null && now < cooldownExpires) {
            long remaining = cooldownExpires - now;
            player.sendMessage(Text.translatable("gauntlet.thaumaturge.cooldown",
                    String.format("%.1f", remaining / 20.0)), true);
            return;
        }

        // 5. Dead-zone guard
        ChunkPos chunkPos = new ChunkPos(player.getBlockPos());
        if (AetherAPI.isDeadZone(world, chunkPos)) {
            player.sendMessage(Text.translatable("spell.aspectslib.fail.dead_zone"), true);
            return;
        }

        // 6. Unraveling hard-block (OPENING)
        if (UnravelingTracker.getStage(player) == UnravelingStage.OPENING) {
            player.sendMessage(Text.translatable("gauntlet.thaumaturge.unraveling_blocked"), true);
            return;
        }

        // 7. Resolve SpellModifier
        SpellModifier modifier = modId != null ? ModifierRegistry.get(modId) : null;

        // 8. Build SpellMetadata and apply modifier transforms
        SpellMetadata metadata = SpellMetadata.DEFAULT.copy();
        metadata.set(SpellMetadata.STABILITY, effect.getStabilityBase());

        if (modifier != null) {
            GauntletSpellAdapter adapter = new GauntletSpellAdapter(effect);
            SpellContext metaCtx = new SpellContext.Builder(world, player, adapter)
                    .conduit(gauntletStack)
                    .metadata(metadata)
                    .build();
            metadata = modifier.modifyMetadata(metadata, metaCtx);
        }

        // 9. Compute Aether cost (using modifier-adjusted metadata)
        double resonanceDiscount = ResonanceDiscountCalculator.compute(
                effect.getAspectIntensities().keySet(), isAtLeyNode(world, player));

        SpellCostParams.Builder costBuilder = new SpellCostParams.Builder()
                .range(effect.getDefaultRange())
                .duration(effect.getDefaultDuration())
                .resonanceDiscount(resonanceDiscount);
        effect.getAspectIntensities().forEach(costBuilder::aspect);
        SpellCostParams costParams = costBuilder.build();

        double totalCost = AetherCostCalculator.compute(costParams);

        // Apply modifier cost adjustments
        double metaCostMult = metadata.getAetherCost() / SpellMetadata.DEFAULT.copy().getAetherCost();
        if (metaCostMult != 1.0) {
            totalCost *= metaCostMult;
        }
        metadata.set(SpellMetadata.AETHER_COST, totalCost);

        // 10. Build full SpellContext for Law validation
        GauntletSpellAdapter adapter = new GauntletSpellAdapter(effect);
        Map<Identifier, Identifier> patternMap = new LinkedHashMap<>();
        patternMap.put(aspectId, modId);
        SpellPattern spellPattern = new SpellPattern(tier, patternMap);

        List<SpellModifier> modifierList = modifier != null ? List.of(modifier) : List.of();

        SpellContext lawCtx = new SpellContext.Builder(world, player, adapter)
                .conduit(gauntletStack)
                .castOrigin(player.getEyePos())
                .pattern(spellPattern)
                .modifiers(modifierList)
                .metadata(metadata)
                .build();

        // 11. Run Law validation
        SpellLawValidator.ValidationReport lawReport = SpellLawValidator.validate(lawCtx);
        if (lawReport.hasHardBlock()) {
            for (SpellLawValidator.Violation v : lawReport.hardViolations()) {
                Thaumaturge.LOGGER.warn("[Gauntlet] Law violation: {} — {}", v.law(), v.message());
                player.sendMessage(Text.literal(v.message()), true);
            }
            return;
        }
        totalCost = metadata.getAetherCost();

        // 12. Stability / misfire check
        double stability = metadata.getStability();
        if (stability < 1.0 && world.random.nextDouble() > stability) {
            player.sendMessage(Text.translatable("spell.aspectslib.fail.misfire"), true);
            Thaumaturge.LOGGER.info("[Gauntlet] Misfire! stability={}", stability);
            return;
        }

        // 13. Personal + Ambient Aether draw
        double ambientFraction = computeAmbientFraction(world, chunkPos);
        double[] split = AetherCostCalculator.personalAetherDraw(totalCost, ambientFraction);
        double personalDraw = split[0];
        double ambientDraw  = split[1];

        if (!(player instanceof PersonalAetherPool pool) ||
                pool.aspectslib$getPersonalAether() < personalDraw) {
            player.sendMessage(Text.translatable("spell.aspectslib.fail.no_aether"), true);
            return;
        }

        pool.aspectslib$drawPersonalAether(personalDraw);

        if (ambientDraw > 0) {
            AetherAPI.castSpell(world, player.getBlockPos(), AspectData.DEFAULT);
        }

        // 14. Modifier pre-execute hook
        if (modifier != null) {
            modifier.onPreExecute(lawCtx);
        }

        // 15. Acquire targets via spell tree mediums and apply effect
        List<Entity> entityTargets;
        List<BlockPos> blockTargets;

        if (spellTree != null) {
            MediumTargetResult targets = resolveTargetsFromTree(spellTree, player, world);
            entityTargets = targets.entities;
            blockTargets = targets.blocks;
        } else {
            // Fallback: no spell tree, target self
            entityTargets = List.of(player);
            blockTargets = List.of(player.getBlockPos());
        }

        List<Identifier> modifierIds = modId != null ? List.of(modId) : List.of();
        double potency = metadata.getPotency();

        // Apply potency from spell tree parameters if available
        if (spellTree != null) {
            potency *= getTreePotencyMultiplier(spellTree);
        }

        GauntletSpellEffect.GauntletCastContext castCtx = new GauntletSpellEffect.GauntletCastContext(
                player,
                world,
                player.getEyePos(),
                entityTargets,
                blockTargets,
                tier,
                modifierIds,
                potency,
                false
        );

        Thaumaturge.LOGGER.info("[Gauntlet] Applying effect {} with {} entity targets, {} block targets, tier={}, potency={}",
                aspectId, entityTargets.size(), blockTargets.size(), tier, potency);
        boolean succeeded = effect.apply(castCtx);

        if (succeeded) {
            Thaumaturge.LOGGER.info("[Gauntlet] Effect {} applied successfully", aspectId);
        } else {
            Thaumaturge.LOGGER.info("[Gauntlet] Effect {} returned false (no targets / no-op)", aspectId);
        }

        // 16. Modifier post-execute hook
        if (modifier != null) {
            modifier.onPostExecute(lawCtx, succeeded);
        }

        // 17. Set cooldown
        int cooldownTicks = computeCooldownTicks(complexity);
        COOLDOWNS.put(player.getUuid(), now + cooldownTicks);

        // 18. Unraveling stress
        if (player instanceof PersonalAetherPool p2) {
            PersonalAetherPool.PoolState state = p2.aspectslib$getPoolState();
            if (state == PersonalAetherPool.PoolState.CRITICAL
                    || state == PersonalAetherPool.PoolState.EXHAUSTED) {
                float overdraw = state == PersonalAetherPool.PoolState.EXHAUSTED ? 0.1f : 0.03f;
                UnravelingTracker.recordOverdraw(player, overdraw);
            }
        }
    }

    // Spell tree medium-based target resolution
    private record MediumTargetResult(List<Entity> entities, List<BlockPos> blocks) {}

    /**
     * Walks the spell tree to find the first medium (child of root) and uses it
     * to determine how targets are acquired. Falls back to targeting the caster
     * if no medium is found (effect linked directly to root).
     */
    private static MediumTargetResult resolveTargetsFromTree(SpellNode root,
                                                              ServerPlayerEntity player,
                                                              ServerWorld world) {
        // Find the first medium in the tree (direct child of root)
        SpellNode mediumNode = null;
        for (SpellNode child : root.getChildren()) {
            SpellComponentDefinition def = child.getDefinition();
            if (def != null && def.type() == SpellComponentType.MEDIUM) {
                mediumNode = child;
                break;
            }
        }

        // No medium, effect linked directly to root targets the caster
        if (mediumNode == null) {
            return new MediumTargetResult(List.of(player), List.of(player.getBlockPos()));
        }

        return resolveTargetsForMedium(mediumNode, player, world);
    }

    private static MediumTargetResult resolveTargetsForMedium(SpellNode mediumNode,
                                                               ServerPlayerEntity player,
                                                               ServerWorld world) {
        SpellComponentDefinition def = mediumNode.getDefinition();
        if (def == null) return new MediumTargetResult(List.of(), List.of());

        String mediumPath = def.id().getPath();
        float range = mediumNode.getParameter("range");
        if (range <= 0) range = 5.0f;

        return switch (mediumPath) {
            case "aversio" -> // Touch: short range raycast
                    resolveRaycastTargets(player, world, range);
            case "potentia" -> // Bolt: long range raycast
                    resolveRaycastTargets(player, world, range);
            case "vinculum" -> // Mine: block targeting
                    resolveMineTargets(player, world, range);
            case "alkimia" -> { // Cloud: AoE at trajectory endpoint
                float radius = mediumNode.getParameter("radius");
                if (radius <= 0) radius = 3.0f;
                yield resolveCloudTargets(player, world, range, radius);
            }
            case "bestia" -> // Spellbat: same as bolt for now (homing projectile TBD)
                    resolveRaycastTargets(player, world, range);
            case "fabrico" -> // Architect: block placement
                    resolveMineTargets(player, world, range);
            default -> new MediumTargetResult(List.of(), List.of());
        };
    }

    private static MediumTargetResult resolveRaycastTargets(ServerPlayerEntity player,
                                                             ServerWorld world,
                                                             double range) {
        Vec3d origin = player.getEyePos();
        Vec3d look = player.getRotationVec(1.0f);
        Vec3d end = origin.add(look.multiply(range));
        Box sweep = player.getBoundingBox().stretch(look.multiply(range)).expand(1.0);

        Entity best = null;
        double bestSq = range * range + 1;

        for (Entity candidate : world.getOtherEntities(player, sweep,
                e -> e instanceof LivingEntity && e.isAlive())) {
            Box expanded = candidate.getBoundingBox().expand(0.3);
            var hit = expanded.raycast(origin, end);
            if (hit.isPresent()) {
                double dSq = origin.squaredDistanceTo(hit.get());
                if (dSq < bestSq) {
                    bestSq = dSq;
                    best = candidate;
                }
            }
        }

        List<Entity> entities = best != null ? List.of(best) : List.of();

        // Also get block target
        BlockHitResult blockHit = world.raycast(new RaycastContext(
                origin, end,
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE,
                player));
        List<BlockPos> blocks = blockHit.getType() == HitResult.Type.BLOCK
                ? List.of(blockHit.getBlockPos()) : List.of();

        return new MediumTargetResult(entities, blocks);
    }

    private static MediumTargetResult resolveMineTargets(ServerPlayerEntity player,
                                                          ServerWorld world,
                                                          double range) {
        Vec3d origin = player.getEyePos();
        Vec3d look = player.getRotationVec(1.0f);
        Vec3d end = origin.add(look.multiply(range));

        BlockHitResult hit = world.raycast(new RaycastContext(
                origin, end,
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE,
                player));

        if (hit.getType() == HitResult.Type.BLOCK) {
            return new MediumTargetResult(List.of(), List.of(hit.getBlockPos()));
        }
        return new MediumTargetResult(List.of(), List.of());
    }

    private static MediumTargetResult resolveCloudTargets(ServerPlayerEntity player,
                                                           ServerWorld world,
                                                           double range,
                                                           double radius) {
        Vec3d origin = player.getEyePos();
        Vec3d look = player.getRotationVec(1.0f);
        Vec3d center = origin.add(look.multiply(range));
        Box box = Box.of(center, radius * 2, radius * 2, radius * 2);

        List<Entity> found = new ArrayList<>();
        for (Entity e : world.getOtherEntities(player, box,
                candidate -> candidate instanceof LivingEntity && candidate.isAlive())) {
            if (e.getPos().squaredDistanceTo(center) <= radius * radius) {
                found.add(e);
            }
        }
        return new MediumTargetResult(found, List.of());
    }

    /**
     * Collects potency parameter multipliers from all effect nodes in the tree.
     * Returns the average potency if multiple effects have potency set.
     */
    private static double getTreePotencyMultiplier(SpellNode root) {
        List<SpellNode> allNodes = root.flatten();
        double totalPotency = 0;
        int count = 0;
        for (SpellNode node : allNodes) {
            SpellComponentDefinition def = node.getDefinition();
            if (def != null && def.type() == SpellComponentType.EFFECT) {
                float potency = node.getParameter("potency");
                if (potency > 0) {
                    totalPotency += potency;
                    count++;
                }
            }
        }
        return count > 0 ? totalPotency / count : 1.0;
    }

    /** Helpers */
    private static double computeAmbientFraction(ServerWorld world, ChunkPos chunkPos) {
        var chunkData = AetherManager.getAetherData(world, chunkPos);
        double cur = 0, max = 0;
        for (var id : chunkData.getAspectIds()) {
            cur += chunkData.getCurrentAether(id);
            max += chunkData.getMaxAether(id);
        }
        return max > 0 ? Math.min(1.0, cur / max) : 0.0;
    }

    private static boolean isAtLeyNode(ServerWorld world, ServerPlayerEntity player) {
        var chunkData = AetherManager.getAetherData(world, new ChunkPos(player.getBlockPos()));
        return chunkData.getCurrentAether(new Identifier("aspectslib", "auram")) >= 200;
    }
}
