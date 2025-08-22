package dev.overgrown.thaumaturge.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Equipment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AethericGogglesItem extends Item implements Equipment {
    public AethericGogglesItem() {
        super(new Settings().maxCount(1));
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        tooltip.add(Text.translatable("item.thaumaturge.aetheric_goggles.tooltip").formatted(Formatting.GRAY));
    }

    public static boolean isWearingGoggles(PlayerEntity player) {
        if (player == null) return false;
        ItemStack headStack = player.getEquippedStack(EquipmentSlot.HEAD);
        return headStack.getItem() instanceof AethericGogglesItem;
    }

    @Override
    public EquipmentSlot getSlotType() {
        return EquipmentSlot.HEAD;
    }

    @Override
    public SoundEvent getEquipSound() {
        return SoundEvents.ITEM_ARMOR_EQUIP_LEATHER;
    }
}