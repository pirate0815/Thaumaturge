package dev.overgrown.thaumaturge.item.apophenia;

import dev.overgrown.thaumaturge.component.BookStateComponent;
import dev.overgrown.thaumaturge.component.ModComponents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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
        boolean client = world.isClient();

        if (!client) {

            BookStateComponent bookStateComponent = stack.getOrDefault(ModComponents.BOOK_STATE, BookStateComponent.DEFAULT).toggle();

            stack.set(ModComponents.BOOK_STATE, bookStateComponent);
            playSoundEffect(world, user, bookStateComponent.open());

        }

        ActionResult.SwingSource swingSource = client ? ActionResult.SwingSource.CLIENT : ActionResult.SwingSource.SERVER;
        return new ActionResult.Success(swingSource, new ActionResult.ItemContext(true, stack));

    }

    private void playSoundEffect(World world, PlayerEntity user, boolean open) {
        var sound = open
                ? SoundEvents.ITEM_BOOK_PAGE_TURN
                : SoundEvents.BLOCK_CHISELED_BOOKSHELF_INSERT;

        world.playSound(null, user.getX(), user.getY(), user.getZ(),
                sound, SoundCategory.PLAYERS, 1.0F, 1.0F);
    }
}
