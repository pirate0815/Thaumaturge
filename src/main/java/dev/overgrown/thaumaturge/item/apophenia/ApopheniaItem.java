package dev.overgrown.thaumaturge.item.apophenia;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ApopheniaItem extends Item {
    private static final String OPEN_KEY = "Open";

    public ApopheniaItem() {
        super(new FabricItemSettings().maxCount(1));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient()) {
            toggleState(stack);
            user.getItemCooldownManager().set(this, 5); // Small cooldown to prevent spamming
        }
        return TypedActionResult.success(stack, world.isClient());
    }

    private void toggleState(ItemStack stack) {
        NbtCompound nbt = stack.getOrCreateNbt();
        boolean isOpen = nbt.getBoolean(OPEN_KEY);
        nbt.putBoolean(OPEN_KEY, !isOpen);
    }

    public static boolean isOpen(ItemStack stack) {
        if (!stack.hasNbt()) return false;
        NbtCompound nbt = stack.getNbt();
        return nbt != null && nbt.getBoolean(OPEN_KEY);
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.TOOT_HORN; // Gives a nice animation when used
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        if (isOpen(stack)) {
            tooltip.add(Text.translatable("item.thaumaturge.apophenia.tooltip.open"));
        } else {
            tooltip.add(Text.translatable("item.thaumaturge.apophenia.tooltip.closed"));
        }
    }
}