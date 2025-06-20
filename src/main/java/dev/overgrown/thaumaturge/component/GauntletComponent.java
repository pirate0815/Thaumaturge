package dev.overgrown.thaumaturge.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.networking.SpellCastPacket;
import dev.overgrown.thaumaturge.spell.SpellHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public record GauntletComponent(List<FociEntry> entries) {
    public static final GauntletComponent DEFAULT = new GauntletComponent(List.of());

    public record FociEntry(Item item, SpellCastPacket.SpellTier tier, Identifier aspectId, Identifier modifierId) {
        public static FociEntry fromItemStack(ItemStack stack, RegistryWrapper.WrapperLookup registries) {
            FociComponent component = stack.get(ModComponents.FOCI_COMPONENT);
            Identifier aspectId = component != null ? component.aspectId() : null;
            Identifier modifierId = component != null ? component.modifierId() : Thaumaturge.identifier("simple");
            SpellCastPacket.SpellTier tier = SpellHandler.getFociTier(stack.getItem());
            return new FociEntry(stack.getItem(), tier, aspectId, modifierId);
        }

        public static final Codec<FociEntry> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Registries.ITEM.getCodec().fieldOf("item").forGetter(FociEntry::item),
                        SpellCastPacket.SpellTier.CODEC.fieldOf("tier").forGetter(FociEntry::tier),
                        Identifier.CODEC.fieldOf("aspectId").forGetter(FociEntry::aspectId),
                        Identifier.CODEC.fieldOf("modifierId").forGetter(FociEntry::modifierId)
                ).apply(instance, FociEntry::new)
        );

        public static final PacketCodec<RegistryByteBuf, FociEntry> PACKET_CODEC = PacketCodec.tuple(
                PacketCodecs.registryValue(RegistryKeys.ITEM),
                FociEntry::item,
                SpellCastPacket.SpellTier.PACKET_CODEC,
                FociEntry::tier,
                Identifier.PACKET_CODEC,
                FociEntry::aspectId,
                Identifier.PACKET_CODEC,
                FociEntry::modifierId,
                FociEntry::new
        );
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