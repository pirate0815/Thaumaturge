package dev.overgrown.thaumaturge.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.networking.SpellCastPacket;
import dev.overgrown.thaumaturge.spell.SpellHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public record GauntletComponent(List<FociEntry> entries) {
    public static final GauntletComponent DEFAULT = new GauntletComponent(List.of());

    public record FociEntry(SpellCastPacket.SpellTier tier, Identifier aspectId, Identifier modifierId, NbtCompound nbt) {
        public static final Codec<FociEntry> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        SpellCastPacket.SpellTier.CODEC.fieldOf("tier").forGetter(FociEntry::tier),
                        Identifier.CODEC.fieldOf("aspectId").forGetter(FociEntry::aspectId),
                        Identifier.CODEC.fieldOf("modifierId").forGetter(FociEntry::modifierId),
                        NbtCompound.CODEC.fieldOf("nbt").forGetter(FociEntry::nbt)
                ).apply(instance, FociEntry::new)
        );

        public static final PacketCodec<RegistryByteBuf, FociEntry> PACKET_CODEC = PacketCodec.tuple(
                SpellCastPacket.SpellTier.PACKET_CODEC,
                FociEntry::tier,
                Identifier.PACKET_CODEC,
                FociEntry::aspectId,
                Identifier.PACKET_CODEC,
                FociEntry::modifierId,
                PacketCodecs.codec(NbtCompound.CODEC),
                FociEntry::nbt,
                FociEntry::new
        );

        public static FociEntry fromItemStack(ItemStack stack, RegistryWrapper.WrapperLookup registries) {
            NbtCompound nbt = (NbtCompound) stack.toNbt(registries);
            FociComponent component = stack.get(ModComponents.FOCI_COMPONENT);
            Identifier aspectId = component != null ? component.aspectId() : null;
            SpellCastPacket.SpellTier tier = SpellHandler.getFociTier(stack.getItem());
            // Add default simple modifier
            return new FociEntry(tier, aspectId, Thaumaturge.identifier("simple"), nbt);
        }
    }

    public static final Codec<GauntletComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.list(FociEntry.CODEC).fieldOf("entries").forGetter(GauntletComponent::entries)
            ).apply(instance, GauntletComponent::new)
    );

    public static final PacketCodec<RegistryByteBuf, GauntletComponent> PACKET_CODEC =
            PacketCodecs.collection(ArrayList::new, FociEntry.PACKET_CODEC)
                    .xmap(GauntletComponent::new, gc -> new ArrayList<>(gc.entries()));

    public int fociCount() {
        return entries.size();
    }
}