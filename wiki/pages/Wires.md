# Wires

Wire Connectors stick to any block. Spools string wires between them, and FE that a block pushes into its connector comes out of the blocks under every other connector it's wired to. Wire Switches and Redstone Wire Relays cut or join parts of a network. Connectors hold no energy themselves.

Each mod that registers the wire blocks supplies a `WireSystem`. Networks only need the library's `WireNodeBlockEntity`, so connectors from different mods join the same network.

## Using another mod's wires

If Create: Solarpunk (or another mod with wires) is installed, its connectors already work on your machines: anything with an FE capability on the face the connector sits on can push into or be fed by a network. Nothing to register.

## Adding a wire tier

Tiers from any mod work on any connector. Register one with an id (saved on every wire, so never change it), the wire item a cut wire drops, its texture, and its rate and length:

```java
public static final WireTier SILVER = WireTiers.register(new WireTier(
        ResourceLocation.fromNamespaceAndPath(MODID, "silver"),
        () -> ModItems.SILVER_WIRE.get(),
        ResourceLocation.fromNamespaceAndPath(MODID, "textures/entity/wire/silver.png"),
        () -> Config.silverRate,       // FE/t, shared by everything routed through the wire
        () -> Config.silverLength));    // longest wire in blocks
```

Register tiers before any world loads (from your mod constructor). The texture is 16x8 twisted strands: x runs along the wire (one repeat per block), y once around it.

A spool strings wires of its tier: `new SpoolItem(SILVER, wireSystem, new Item.Properties())`, where `wireSystem` is the system of the mod whose connectors it's meant for.

## Registering the wire blocks yourself

Only for a mod that has to have wires without another wire mod installed. Make a `WireSystem` with your lang keys, settings, data components and block entity types:

```java
public static final WireSystem SYSTEM = new WireSystem(MODID, GOGGLES,
        () -> Config.maxConnections, () -> Config.blocksPerWire,
        () -> ModDataComponents.WIRE_START.get(),           // GlobalPos, on the spool
        () -> ModDataComponents.WIRE_START_TERMINAL.get(),  // Integer, on the spool
        () -> ModItems.EMPTY_SPOOL.get(),
        () -> ModBlockEntities.WIRE_CONNECTOR.get(), () -> ModBlockEntities.WIRE_SWITCH.get());
```

Then register the blocks and block entities with it:

```java
new WireConnectorBlock(SYSTEM, properties)
new WireSwitchBlock(SYSTEM, properties)
new RedstoneWireRelayBlock(SYSTEM, properties)
new WireConnectorBlockEntity(type, pos, state, SYSTEM)  // for the connector
new WireSwitchBlockEntity(type, pos, state, SYSTEM)     // for the switch and relay
```

Register the connector's energy on the face touching its block, and draw the wires with `WireRenderer` (subclass it to change a wire's texture with `wireTexture` or draw along it with `decorate`):

```java
event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ModBlockEntities.WIRE_CONNECTOR.get(),
        (be, side) -> side == null || side == be.back() ? be.energyStorage : null);
event.registerBlockEntityRenderer(ModBlockEntities.WIRE_CONNECTOR.get(), WireRenderer::new);
event.registerBlockEntityRenderer(ModBlockEntities.WIRE_SWITCH.get(), WireRenderer::new);
```

### Lang keys

Goggle labels come from the system's `GoggleLines`: `wire_connector_header`, `wire_switch_header`, `redstone_wire_relay_header`, `wires`, `throughput`, `switch_state`, `switch_closed`, `switch_open`, `relay_closes`, `relay_powered`, `relay_unpowered`. Spool messages are `message.<modid>.wire.<key>` (`started`, `cancelled`, `full`, `start_missing`, `already_connected`, `too_long`, `not_enough`), and its tooltip `item.<modid>.spool.wires_left`, `.stats` and `.started`.
