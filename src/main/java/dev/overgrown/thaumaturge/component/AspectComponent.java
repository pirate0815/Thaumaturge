package dev.overgrown.thaumaturge.component;

import com.mojang.serialization.Codec;
import net.minecraft.component.ComponentType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public final class AspectComponent {
    public static final Codec<AspectComponent> CODEC = Codec.unboundedMap(Identifier.CODEC, Codec.INT)
            .xmap(AspectComponent::new, comp -> comp.aspects);
    public static final PacketCodec<RegistryByteBuf, AspectComponent> PACKET_CODEC = new PacketCodec<>() {
        @Override
        public AspectComponent decode(RegistryByteBuf buf) {
            int size = buf.readVarInt();
            Map<Identifier, Integer> aspects = new HashMap<>();
            for (int i = 0; i < size; i++) {
                Identifier id = Identifier.PACKET_CODEC.decode(buf);
                int value = buf.readVarInt();
                aspects.put(id, value);
            }
            return new AspectComponent(aspects);
        }

        @Override
        public void encode(RegistryByteBuf buf, AspectComponent component) {
            Map<Identifier, Integer> aspects = component.aspects;
            buf.writeVarInt(aspects.size());
            for (Map.Entry<Identifier, Integer> entry : aspects.entrySet()) {
                Identifier.PACKET_CODEC.encode(buf, entry.getKey());
                buf.writeVarInt(entry.getValue());
            }
        }
    };
    public static final ComponentType<AspectComponent> TYPE = ComponentType.<AspectComponent>builder()
            .codec(CODEC)
            .packetCodec(PACKET_CODEC)
            .build();
    public static final AspectComponent DEFAULT = new AspectComponent(Map.of());

    public final Map<Identifier, Integer> aspects;

    public AspectComponent(Map<Identifier, Integer> aspects) {
        this.aspects = Map.copyOf(aspects);
    }

    public AspectComponent addAspect(Identifier aspect, int value) {
        Map<Identifier, Integer> newMap = new HashMap<>(aspects);
        newMap.merge(aspect, value, Integer::sum);
        return new AspectComponent(newMap);
    }

    public AspectComponent removeAspect(Identifier aspect, int value) {
        Map<Identifier, Integer> newMap = new HashMap<>(aspects);
        newMap.computeIfPresent(aspect, (k, v) -> {
            int newValue = v - value;
            return newValue > 0 ? newValue : null;
        });
        return new AspectComponent(newMap);
    }

    public int getAspect(Identifier aspect) {
        return aspects.getOrDefault(aspect, 0);
    }

    public static AspectComponent getOrDefault(ItemStack stack) {
        return stack.getOrDefault(TYPE, DEFAULT);
    }
}
