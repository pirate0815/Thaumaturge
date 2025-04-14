package dev.overgrown.thaumaturge.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

/**
 * 	This component stores the state of a book. You can add more properties to this record, but currently, it only stores
 * 	the following properties:
 *
 * 	@param open determines if the book is open.
 */
public record BookStateComponent(boolean open) {

	//	The codec for this component. Uses the record codec builder for future-proofing (in case you need to add more
	//	states)
	public static final Codec<BookStateComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.BOOL.fieldOf("open").forGetter(BookStateComponent::open)
	).apply(instance, BookStateComponent::new));

	//	The packet codec for this component; used for syncing the properties of this component to the client
	public static final PacketCodec<ByteBuf, BookStateComponent> PACKET_CODEC = PacketCodec.tuple(
		PacketCodecs.BOOLEAN, BookStateComponent::open,
		BookStateComponent::new
	);

	public static final BookStateComponent DEFAULT = new BookStateComponent(false);

	//	Toggles the "open" state of this component
	public BookStateComponent toggle() {
		return new BookStateComponent(!this.open());
	}

}
