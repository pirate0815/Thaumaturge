package dev.overgrown.thaumaturge.item.bonewits_dust;

import dev.overgrown.thaumaturge.block.ModBlocks;
import dev.overgrown.thaumaturge.item.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public class BonewitsDust extends Item {
    public BonewitsDust(Settings settings) {
        super(settings);
    }
    private static final Map<Block, TransformationHandler> TRANSFORMATIONS = new HashMap<>();

    static {
        TRANSFORMATIONS.put(Blocks.BOOKSHELF, (world, pos, player, stack) -> {
            BlockState currentState = world.getBlockState(pos);
            if (currentState.isOf(Blocks.BOOKSHELF)) {
                world.breakBlock(pos, false);
                ItemStack book = new ItemStack(ModItems.APOPHENIA);
                ItemEntity item = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, book);
                world.spawnEntity(item);
                return true;
            }
            return false;
        });

        TRANSFORMATIONS.put(Blocks.CAULDRON, (world, pos, player, stack) -> {
            BlockState currentState = world.getBlockState(pos);
            if (currentState.isOf(Blocks.CAULDRON)) {
                world.setBlockState(pos, ModBlocks.VESSEL.getDefaultState());
                return true;
            }
            return false;
        });
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        TransformationHandler handler = TRANSFORMATIONS.get(block);
        if (handler != null) {
            if (!world.isClient()) {
                ServerWorld serverWorld = (ServerWorld) world;
                PlayerEntity player = context.getPlayer();
                ItemStack stack = context.getStack();

                // Play initial chime sound
                world.playSound(null, pos, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.BLOCKS, 1.0F, 1.0F);

                // Spawn dust particles around the block
                spawnDustParticles(serverWorld, pos);

                // Schedule delayed transformation and effects
                serverWorld.getServer().execute(new DelayedTransformationTask(serverWorld, pos, handler, player, stack, 70));
            }
            return world.isClient() ? ActionResult.SUCCESS : ActionResult.SUCCESS_SERVER;
        }
        return ActionResult.PASS;
    }

    private void spawnDustParticles(ServerWorld world, BlockPos pos) {
        // Convert Vector 3f components to integer RGB
        float r = 0.9F;
        float g = 0.1F;
        float b = 0.1F;
        int red = (int)(r * 255);
        int green = (int)(g * 255);
        int blue = (int)(b * 255);
        int color = (red << 16) | (green << 8) | blue;

        DustParticleEffect dustEffect = new DustParticleEffect(color, 1.0F); // Use the integer color
        for (int i = 0; i < 360; i += 10) {
            double angle = Math.toRadians(i);
            double x = pos.getX() + 0.5 + Math.cos(angle) * 0.5;
            double z = pos.getZ() + 0.5 + Math.sin(angle) * 0.5;
            world.spawnParticles(dustEffect, x, pos.getY() + 0.5, z, 1, 0, 0, 0, 0);
        }
    }

    private static class DelayedTransformationTask implements Runnable {
        private final ServerWorld world;
        private final BlockPos pos;
        private final TransformationHandler handler;
        private final PlayerEntity player;
        private final ItemStack stack;
        private int ticksRemaining;

        public DelayedTransformationTask(ServerWorld world, BlockPos pos, TransformationHandler handler, PlayerEntity player, ItemStack stack, int delayTicks) {
            this.world = world;
            this.pos = pos;
            this.handler = handler;
            this.player = player;
            this.stack = stack;
            this.ticksRemaining = delayTicks;
        }

        @Override
        public void run() {
            if (--ticksRemaining <= 0) {
                // Play blast sound and particles
                world.playSound(null, pos, SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST, SoundCategory.BLOCKS, 1.0F, 1.0F);
                world.spawnParticles(ParticleTypes.WITCH, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10, 0.5, 0.5, 0.5, 0.0);
                world.spawnParticles(ParticleTypes.CLOUD, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 20, 0.5, 0.5, 0.5, 0.0);

                // Execute transformation
                boolean success = handler.handle(world, pos, player, stack);
                if (success && player != null && !player.isCreative()) {
                    stack.decrement(1);
                }
            } else {
                // Reschedule task for next tick
                world.getServer().execute(this);
            }
        }
    }

    @FunctionalInterface
    private interface TransformationHandler {
        boolean handle(World world, BlockPos pos, PlayerEntity player, ItemStack stack);
    }
}