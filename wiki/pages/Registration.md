# Registration

## Blocks with their items

`BlockRegistrar` registers a block and its item together into your own registers, and remembers which blocks are decorative so they can go in their own creative tab.

```java
public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
// Declare it before the blocks that use it
private static final BlockRegistrar REG = new BlockRegistrar(BLOCKS, ModItems.ITEMS);

public static final DeferredBlock<Block> SALT_BLOCK = REG.decorative("salt_block",
        () -> new Block(BlockBehaviour.Properties.of().strength(1.5f)));
public static final DeferredBlock<StairBlock> SALT_STAIRS = REG.stairs("salt_stairs", SALT_BLOCK);
public static final DeferredBlock<SlabBlock> SALT_SLAB = REG.slab("salt_slab", SALT_BLOCK);
public static final DeferredBlock<WallBlock> SALT_WALL = REG.wall("salt_wall", SALT_BLOCK);

public static final DeferredBlock<VatBlock> VAT = REG.blockWithItem("vat",
        () -> new VatBlock(...), block -> new MultiBlockLayerItem(block, ModBlockEntities.VAT, new Item.Properties()));
```

| Method | Registers |
|---|---|
| `block(name, block)` | A block with a plain `BlockItem` |
| `blockWithItem(name, block, itemFactory)` | A block with its own item class |
| `decorative(...)`, `decorativeWithItem(...)` | The same, listed in `decorativeBlocks()` |
| `stairs`, `slab`, `wall` | Decorative shapes that copy every property of their base block. Register them after the base |

## Creative tabs that fill themselves

`CreativeTabContents` fills a tab from your registers, so new blocks and items show up without editing the tab:

```java
.displayItems((parameters, output) ->
        CreativeTabContents.addAll(output, ModBlocks.BLOCKS, ModItems.ITEMS, ModBlocks.REG.decorativeBlocks()))
```

`addAll` adds every block (in registration order) except the excluded ones, then plain items, then buckets. It skips blocks with no item of their own (fluids, extra shapes that borrow another block's item) and Create's sequenced assembly in-between items. `addBlocks(output, blocks)` adds exactly the given blocks, for a building blocks tab.

## Item descriptions

`ItemTooltips.describe(item)` gives an item Create's "Hold [Shift] for Summary" tooltip, read from the `<item description id>.tooltip.summary`, `.condition1`, `.behaviour1` (and so on) lang keys, plus stress and speed stats for kinetic blocks. Call it during client setup:

```java
event.enqueueWork(() -> ItemTooltips.describe(ModBlocks.SOLAR_HEATER.get()));
```
