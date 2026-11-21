package studio.offbyone.lib.wire;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * The metal a wire is made of: how much FE it carries, how long it can be, and how it looks.
 * Register each tier once with {@link WireTiers#register}; tiers from any mod work on any wire
 * node, so an addon can add a new wire to another mod's connectors.
 *
 * @param id        saved on every wire, so never change it
 * @param wireItem  dropped when the wire is cut, one per {@link WireSystem#blocksPerWire} blocks
 * @param texture   the twisted-strand texture {@code WireRenderer} wraps around the wire
 *                  (16x8: x along the wire, one repeat per block; y once around)
 * @param rate      FE/t the wire carries, shared by everything routed through it
 * @param maxLength longest wire in blocks
 */
public record WireTier(ResourceLocation id, Supplier<? extends Item> wireItem, ResourceLocation texture,
                       IntSupplier rate, IntSupplier maxLength) {

    public int getRate() {
        return rate.getAsInt();
    }

    public int getMaxLength() {
        return maxLength.getAsInt();
    }

    public Item getWireItem() {
        return wireItem.get();
    }
}
