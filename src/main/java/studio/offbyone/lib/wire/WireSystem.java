package studio.offbyone.lib.wire;

import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import studio.offbyone.lib.util.GoggleLines;

import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * What the mod that registers the wire blocks owns: its lang keys, settings, data components
 * and block entity types. Make one, and pass it to the wire blocks, block entities and spools.
 * Wires from different systems still connect, since the network only needs
 * {@link WireNodeBlockEntity}s.
 *
 * Lang keys: goggle labels come from {@code goggles} ({@code wire_connector_header},
 * {@code wire_switch_header}, {@code redstone_wire_relay_header}, {@code wires},
 * {@code throughput}, {@code switch_state}, {@code switch_closed}, {@code switch_open},
 * {@code relay_closes}, {@code relay_powered}, {@code relay_unpowered}); spool messages are
 * {@code message.<modId>.wire.<started|cancelled|full|start_missing|already_connected|too_long|not_enough>}
 * and its tooltip {@code item.<modId>.spool.<wires_left|stats|started>}.
 *
 * @param maxConnections  wires per terminal
 * @param blocksPerWire   blocks of length each wire item covers
 * @param wireStart       spool component: the node a wire was started from
 * @param wireStartTerminal spool component: which terminal of it
 * @param emptySpool      what a spool turns into once it runs out
 */
public record WireSystem(String modId, GoggleLines goggles, IntSupplier maxConnections, IntSupplier blocksPerWire,
                         Supplier<? extends DataComponentType<GlobalPos>> wireStart,
                         Supplier<? extends DataComponentType<Integer>> wireStartTerminal,
                         Supplier<? extends Item> emptySpool,
                         Supplier<? extends BlockEntityType<WireConnectorBlockEntity>> connectorType,
                         Supplier<? extends BlockEntityType<WireSwitchBlockEntity>> switchType) {

    public int getMaxConnections() {
        return maxConnections.getAsInt();
    }

    /** Wire items a wire of this length uses (and gives back when it's removed). */
    public int wiresFor(double length) {
        return Math.max(1, (int) Math.ceil(length / blocksPerWire.getAsInt()));
    }
}
