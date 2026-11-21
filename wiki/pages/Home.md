# Off by One Lib

Shared code for Off by One Studio's Create addons on NeoForge 1.21.1: multiblock tanks, machine helpers, wires, solar power and the boilerplate every addon repeats. It adds no blocks or items of its own. Your addon extends its classes and registers everything under its own mod id.

**Start** - Add the library to an addon, and how versions work when several mods bundle it.

**Blocks and Machines** - Register blocks with their items, fill creative tabs, build Create-style tanks, and the helpers machines keep needing.

**Power** - Wires that carry FE between blocks, with tiers any mod can add, and sunlight checks that handle space, contraptions and seasons.

**Integration** - Create-style advancements, JEI categories, Display Link sources, and datagen bases for block families, loot, recipes and Ponder.

## Who it's for

Off by One Studio's own addons (Create: Solarpunk is the first), and anyone else making a Create addon who wants the same pieces. It's MIT licensed.

## The one rule

The library never owns your content. Block, item, block entity, data component and sound ids are always registered by your addon, so a block built on the library is still `yourmod:thing`, and moving code into the library never breaks a world. The only things registered under `offbyonelib` are ones that must exist once per game, like the shape menu's packet.
