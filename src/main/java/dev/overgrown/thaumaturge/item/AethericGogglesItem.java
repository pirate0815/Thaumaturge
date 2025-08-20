package dev.overgrown.thaumaturge.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AethericGogglesItem extends ArmorItem {
    // Custom armor material with zero protection
    private static final ArmorMaterial AETHERIC_MATERIAL = new SimpleArmorMaterial("aetheric", 0, new int[]{0, 0, 0, 0}, 0, 0, 0);

    public AethericGogglesItem() {
        super(AETHERIC_MATERIAL, Type.HELMET, new Settings().maxCount(1));
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        tooltip.add(Text.translatable("item.thaumaturge.aetheric_goggles.tooltip").formatted(Formatting.GRAY));
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);

        // Only update when worn in helmet slot
        if (entity instanceof PlayerEntity player &&
                player.getEquippedStack(EquipmentSlot.HEAD).getItem() == this) {
            // Add any special effects when worn
        }
    }

    public static boolean isWearingGoggles(PlayerEntity player) {
        if (player == null) return false;
        ItemStack headStack = player.getEquippedStack(EquipmentSlot.HEAD);
        return headStack.getItem() instanceof AethericGogglesItem;
    }
}