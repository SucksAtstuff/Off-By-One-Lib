package studio.offbyone.lib.wire;

import studio.offbyone.lib.wire.WireNodeBlockEntity;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import studio.offbyone.lib.wire.RedstoneWireRelayBlock;
import studio.offbyone.lib.wire.WireSwitchBlock;

import java.util.List;

/** The Wire Switch and Redstone Wire Relay: two terminals, joined while the switch is closed. */
public class WireSwitchBlockEntity extends WireNodeBlockEntity implements IHaveGoggleInformation {

    public WireSwitchBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, WireSystem system) {
        super(type, pos, state, system);
    }

    @Override
    public int terminals() {
        return 2;
    }

    @Override
    public boolean joinsTerminals() {
        BlockState state = getBlockState();
        return state.hasProperty(WireSwitchBlock.CLOSED) && state.getValue(WireSwitchBlock.CLOSED);
    }

    public void tick() {
        if (level != null && !level.isClientSide) validateWires();
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        BlockState state = getBlockState();
        boolean relay = state.hasProperty(RedstoneWireRelayBlock.INVERTED);
        system.goggles().label(relay ? "redstone_wire_relay_header" : "wire_switch_header").forGoggles(tooltip);

        boolean closed = joinsTerminals();
        system.goggles().label("switch_state")
                .style(ChatFormatting.GRAY)
                .add(system.goggles().label(closed ? "switch_closed" : "switch_open")
                        .style(closed ? ChatFormatting.GREEN : ChatFormatting.RED))
                .forGoggles(tooltip, 1);

        if (relay) {
            boolean inverted = state.getValue(RedstoneWireRelayBlock.INVERTED);
            system.goggles().label("relay_closes")
                    .style(ChatFormatting.GRAY)
                    .add(system.goggles().label(inverted ? "relay_unpowered" : "relay_powered").style(ChatFormatting.AQUA))
                    .forGoggles(tooltip, 1);
        }

        system.goggles().label("wires")
                .style(ChatFormatting.GRAY)
                .add(Component.literal(countOn(0) + " / " + maxConnections() + ", "
                        + countOn(1) + " / " + maxConnections()).withStyle(ChatFormatting.AQUA))
                .forGoggles(tooltip, 1);
        return true;
    }
}
