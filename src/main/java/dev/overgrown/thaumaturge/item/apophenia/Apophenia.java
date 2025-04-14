package dev.overgrown.thaumaturge.item.apophenia;

import dev.overgrown.thaumaturge.component.ModComponents;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class Apophenia extends Item {
    public Apophenia(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient()) {
            NbtCompound nbt = getOrCreateNbtComponent(stack);
            byte currentState = nbt.getByte("is_open", (byte) 0);
            byte newState = (byte) (currentState == 0 ? 1 : 0);

            nbt.putByte("is_open", newState);
            stack.set(ModComponents.BOOK_STATE, NbtComponent.of(nbt));

            playSoundEffect(world, user, newState);
        }
        boolean isClient = world.isClient();
        ActionResult.SwingSource swingSource = isClient ? ActionResult.SwingSource.CLIENT : ActionResult.SwingSource.SERVER;
        return new ActionResult.Success(swingSource, new ActionResult.ItemContext(true, stack));
    }

    private NbtCompound getOrCreateNbtComponent(ItemStack stack) {
        NbtComponent component = stack.get(ModComponents.BOOK_STATE);
        return component != null ? component.copyNbt() : new NbtCompound();
    }

    private void playSoundEffect(World world, PlayerEntity user, byte state) {
        var sound = state == 1
                ? SoundEvents.ITEM_BOOK_PAGE_TURN
                : SoundEvents.BLOCK_CHISELED_BOOKSHELF_INSERT;

        world.playSound(null, user.getX(), user.getY(), user.getZ(),
                sound, SoundCategory.PLAYERS, 1.0F, 1.0F);
    }
}