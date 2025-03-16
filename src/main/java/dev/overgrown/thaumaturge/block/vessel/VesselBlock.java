package dev.overgrown.thaumaturge.block.vessel;

import com.mojang.serialization.Codec;
import dev.overgrown.thaumaturge.block.ModBlocks;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.util.ItemScatterer;
import net.minecraft.registry.tag.BlockTags;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class VesselBlock extends Block implements BlockEntityProvider {
    public enum FluidType implements StringIdentifiable {
        EMPTY, WATER, LAVA, POWDERED_SNOW;

        public static final Codec<FluidType> CODEC = StringIdentifiable.createCodec(FluidType::values);

        @Override
        public String asString() {
            return this.name().toLowerCase();
        }
    }

    public static final EnumProperty<FluidType> FLUID_TYPE = EnumProperty.of("fluid_type", FluidType.class);
    public static final IntProperty LEVEL = IntProperty.of("level", 0, 3);

    public VesselBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState()
                .with(FLUID_TYPE, FluidType.WATER)
                .with(LEVEL, 0));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FLUID_TYPE, LEVEL);
    }

    private boolean isHeatSource(BlockState state) {
        return state.isIn(BlockTags.FIRE) ||
                state.isOf(Blocks.LAVA) ||
                (state.getBlock() instanceof CampfireBlock && state.get(CampfireBlock.LIT));
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        Hand hand = player.getActiveHand();
        ItemStack stack = player.getStackInHand(hand);
        FluidType fluidType = getFluidType(stack);

        if (fluidType != null && isHeatSource(world.getBlockState(pos.down()))) {
            int currentLevel = state.get(LEVEL);
            if (currentLevel < 3) {
                world.setBlockState(pos, state.with(FLUID_TYPE, fluidType).with(LEVEL, currentLevel + 1), Block.NOTIFY_ALL);
                if (!player.isCreative()) {
                    stack.decrement(1);
                    player.giveItemStack(new ItemStack(Items.BUCKET));
                }
                world.playSound(null, pos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
                if (!world.isClient) {
                    addToSequenceAndCheckRecipes(world, pos, state, stack.getItem());
                }
                return ActionResult.SUCCESS;
            } else {
                player.sendMessage(Text.translatable("block.thaumaturge.vessel.full"), true);
                return ActionResult.CONSUME;
            }
        } else if (!world.isClient) {
            addToSequenceAndCheckRecipes(world, pos, state, stack.getItem());
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    private FluidType getFluidType(ItemStack stack) {
        if (stack.isOf(Items.WATER_BUCKET)) return FluidType.WATER;
        if (stack.isOf(Items.LAVA_BUCKET)) return FluidType.LAVA;
        if (stack.isOf(Items.POWDER_SNOW_BUCKET)) return FluidType.POWDERED_SNOW;
        return null;
    }

    private void addToSequenceAndCheckRecipes(World world, BlockPos pos, BlockState state, Item item) {
        VesselBlockEntity blockEntity = (VesselBlockEntity) world.getBlockEntity(pos);
        if (blockEntity != null) {
            blockEntity.addItem(item);
            checkRecipes(world, pos, blockEntity, state);
        }
    }

    private void checkRecipes(World world, BlockPos pos, VesselBlockEntity blockEntity, BlockState state) {
        List<VesselRecipe> recipes = ((ServerWorld) world).getRecipeManager().values().stream()
                .filter(entry -> entry.value() instanceof VesselRecipe)
                .map(entry -> (VesselRecipe) entry.value())
                .toList();

        VesselRecipeInput input = new VesselRecipeInput(
                blockEntity.getSequence(),
                state.get(FLUID_TYPE),
                state.get(LEVEL)
        );

        for (VesselRecipe recipe : recipes) {
            if (recipe.matches(input, world)) {
                ItemStack output = recipe.craft(input, world.getRegistryManager());
                ItemScatterer.spawn(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, output);
                blockEntity.resetSequence();
                world.setBlockState(pos, state.with(FLUID_TYPE, FluidType.EMPTY).with(LEVEL, 0), Block.NOTIFY_ALL);
                break;
            }
        }
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        super.onStateReplaced(state, world, pos, newState, moved);
        if (!world.isClient && !state.isOf(newState.getBlock())) {
            int level = state.get(LEVEL);
            if (level > 0) {
                Item fluidItem = Items.AIR;
                switch (state.get(FLUID_TYPE)) {
                    case WATER -> fluidItem = Items.WATER_BUCKET;
                    case LAVA -> fluidItem = Items.LAVA_BUCKET;
                    case POWDERED_SNOW -> fluidItem = Items.POWDER_SNOW_BUCKET;
                }
                ItemScatterer.spawn(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(fluidItem));
            }
        }
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new VesselBlockEntity(pos, state);
    }

    public static class VesselBlockEntity extends BlockEntity {
        public static class ModBlockEntities {
            public static final BlockEntityType<VesselBlockEntity> VESSEL_BLOCK_ENTITY =
                    FabricBlockEntityTypeBuilder.create(VesselBlockEntity::new, ModBlocks.VESSEL).build();
        }
        private final List<Item> sequence = new ArrayList<>();

        public VesselBlockEntity(BlockPos pos, BlockState state) {
            super(ModBlockEntities.VESSEL_BLOCK_ENTITY, pos, state);
        }

        public void addItem(Item item) {
            sequence.add(item);
            markDirty();
        }

        public List<Item> getSequence() {
            return new ArrayList<>(sequence);
        }

        public void resetSequence() {
            sequence.clear();
            markDirty();
        }

        @Override
        public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
            super.readNbt(nbt, registries);
            sequence.clear();
            NbtList list = nbt.getList("Sequence", NbtElement.STRING_TYPE);
            for (NbtElement element : list) {
                Item item = Registries.ITEM.get(Identifier.of(element.asString()));
                sequence.add(item);
            }
        }

        @Override
        protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
            super.writeNbt(nbt, registries);
            NbtList list = new NbtList();
            for (Item item : sequence) {
                list.add(NbtString.of(Registries.ITEM.getId(item).toString()));
            }
            nbt.put("Sequence", list);
        }

        @Nullable
        @Override
        public Packet<ClientPlayPacketListener> toUpdatePacket() {
            return BlockEntityUpdateS2CPacket.create(this);
        }

        @Override
        public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
            return createNbt(registryLookup);
        }
    }
}