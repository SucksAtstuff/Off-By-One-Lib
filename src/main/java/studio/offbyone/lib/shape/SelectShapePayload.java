package studio.offbyone.lib.shape;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import studio.offbyone.lib.OffByOneLib;

/** Client -> server: the player picked shape {@code index} for the {@link ShapedItem} in their main hand. */
public record SelectShapePayload(int index) implements CustomPacketPayload {

    public static final Type<SelectShapePayload> TYPE = new Type<>(OffByOneLib.id("select_shape"));
    public static final StreamCodec<ByteBuf, SelectShapePayload> STREAM_CODEC =
            ByteBufCodecs.VAR_INT.map(SelectShapePayload::new, SelectShapePayload::index);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(TYPE, STREAM_CODEC, SelectShapePayload::handle);
    }

    private static void handle(SelectShapePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ItemStack held = context.player().getMainHandItem();
            if (held.getItem() instanceof ShapedItem item)
                item.selectShape(held, payload.index());
        });
    }
}
