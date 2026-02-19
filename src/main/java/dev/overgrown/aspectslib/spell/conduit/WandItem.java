package dev.overgrown.aspectslib.spell.conduit;

import dev.overgrown.aspectslib.spell.SpellContext;
import dev.overgrown.aspectslib.spell.modifier.SpellModifier;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A concrete, ready-to-register conduit item that implements the classic
 * "wand" archetype:
 *
 * <ul>
 *   <li>Stores a bound spell id and modifier list in NBT via {@link ConduitItem}.</li>
 *   <li>Consumes durability on every cast ({@code durabilityCostPerCast} points).</li>
 *   <li>Enforces a per-wand cooldown in ticks ({@code cooldownTicks}) by storing
 *       the "ready at" game-time in NBT.</li>
 *   <li>Dispatches casts through {@link ConduitDispatcher} on right-click.</li>
 *   <li>Shows spell name + modifier list in the item tooltip.</li>
 * </ul>
 *
 * <h3>Creating a wand type</h3>
 * <pre>{@code
 * public static final WandItem OAK_WAND = new WandItem(
 *     "Oak Wand",
 *     new Item.Settings().maxDamage(64).fireproof(),
 *     // 3 durability per cast, 20-tick cooldown
 *     3, 20
 * );
 * }</pre>
 *
 * <h3>Binding a spell to an ItemStack at runtime</h3>
 * <pre>{@code
 * WandItem.setSpellId(wandStack, IgnisSpell.ID);
 * WandItem.addModifier(wandStack, new Identifier("aspectslib", "power"));
 * }</pre>
 */
public class WandItem extends ConduitItem {

    // NBT key for cooldown tracking
    private static final String KEY_READY_AT = "ReadyAt";

    // Config set at item construction
    /** Durability consumed per successful cast. */
    private final int durabilityCostPerCast;

    /** Ticks the wand must "rest" between casts. */
    private final int cooldownTicks;

    // Constructor
    /**
     * @param settings              item settings; use {@code .maxDamage(n)} for
     *                              a finite-use wand, omit for infinite
     * @param durabilityCostPerCast durability removed per cast (0 = infinite)
     * @param cooldownTicks         ticks between casts (0 = no cooldown)
     */
    public WandItem(Settings settings, int durabilityCostPerCast, int cooldownTicks) {
        super(settings);
        this.durabilityCostPerCast = durabilityCostPerCast;
        this.cooldownTicks = cooldownTicks;
    }

    // IConduit overrides
    /**
     * A wand can cast when:
     * <ol>
     *   <li>The stack is not empty.</li>
     *   <li>It has durability remaining (or has no durability cap).</li>
     *   <li>Its cooldown has elapsed.</li>
     * </ol>
     */
    @Override
    public boolean canCast(ItemStack stack, LivingEntity caster) {
        if (stack.isEmpty()) return false;
        if (stack.getMaxDamage() > 0 && stack.getDamage() >= stack.getMaxDamage()) return false;
        if (cooldownTicks > 0 && !isCooldownElapsed(stack, caster)) return false;
        return true;
    }

    /**
     * Applies durability damage and starts the cooldown timer after a cast.
     */
    @Override
    public void onSpellCast(ItemStack stack, SpellContext ctx) {
        LivingEntity caster = ctx.getCaster();

        // Durability damage
        if (durabilityCostPerCast > 0 && stack.getMaxDamage() > 0) {
            stack.damage(durabilityCostPerCast, caster,
                    e -> e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
        }

        // Cooldown - store the world game time when the wand is next ready
        if (cooldownTicks > 0 && caster.getWorld() != null) {
            long readyAt = caster.getWorld().getTime() + cooldownTicks;
            stack.getOrCreateNbt().putLong(KEY_READY_AT, readyAt);
        }
    }

    // Item overrides
    /**
     * Right-click fires the stored spell through {@link ConduitDispatcher}.
     */
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient) {
            ConduitDispatcher.dispatch(user, stack);
        }
        return TypedActionResult.success(stack);
    }

    /**
     * Appends spell name and modifier list to the item's tooltip.
     */
    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world,
                              List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);

        // Bound spell
        String spellName = getSpellDisplayName(stack);
        tooltip.add(Text.translatable("tooltip.aspectslib.bound_spell",
                Text.literal(spellName).formatted(Formatting.AQUA)));

        // Modifiers
        List<SpellModifier> mods = getStoredModifiers(stack);
        if (!mods.isEmpty()) {
            tooltip.add(Text.translatable("tooltip.aspectslib.modifiers")
                    .formatted(Formatting.GRAY));
            mods.forEach(m -> tooltip.add(
                    Text.literal("  • " + m.getId().getPath())
                            .formatted(Formatting.DARK_GRAY)));
        }

        // Cooldown indicator
        if (cooldownTicks > 0) {
            if (!isCooldownElapsed(stack, null)) {
                tooltip.add(Text.translatable("tooltip.aspectslib.on_cooldown")
                        .formatted(Formatting.RED));
            }
        }
    }

    // Getters
    public int getDurabilityCostPerCast() {
        return durabilityCostPerCast;
    }

    public int getCooldownTicks() {
        return cooldownTicks;
    }

    // Helpers
    /**
     * Returns {@code true} when the wand's cooldown has elapsed.
     *
     * @param entity the entity whose world provides the current game time;
     *               may be {@code null} when calling from a tooltip context
     *               (defaults to "not ready" if world time is unavailable)
     */
    private boolean isCooldownElapsed(ItemStack stack, @Nullable LivingEntity entity) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.contains(KEY_READY_AT)) return true; // never cast yet
        long readyAt = nbt.getLong(KEY_READY_AT);
        if (entity == null || entity.getWorld() == null) return false; // can't verify
        return entity.getWorld().getTime() >= readyAt;
    }
}