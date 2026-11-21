# Shape Menu

One item that places several blocks: hold it and press Create's toolbelt key (Alt by default) to pick a shape from a radial menu. Create: Solarpunk's Particle Accelerator Pipe and filigree railings work this way. The library opens the menu, swaps it in for Create's toolbox menu when a toolbox is nearby, and syncs the choice to the server, however many mods have shaped items.

## A shaped block item

Make an enum of shapes and a data component to store the choice (registered by your mod, since it's saved on item stacks), then extend `ShapedBlockItem`:

```java
public enum PipeShape implements StringRepresentable {
    STRAIGHT("straight"), BEND("bend");
    public static final Codec<PipeShape> CODEC = StringRepresentable.fromEnum(PipeShape::values);
    public static final StreamCodec<ByteBuf, PipeShape> STREAM_CODEC =
            ByteBufCodecs.VAR_INT.map(i -> values()[i], PipeShape::ordinal);
    ...
}

public static final DeferredHolder<DataComponentType<?>, DataComponentType<PipeShape>> PIPE_SHAPE =
        DATA_COMPONENTS.registerComponentType("pipe_shape", b -> b
                .persistent(PipeShape.CODEC).networkSynchronized(PipeShape.STREAM_CODEC));

public class PipeItem extends ShapedBlockItem<PipeShape> {
    public PipeItem(Block block, Properties properties) {
        super(block, ModDataComponents.PIPE_SHAPE, PipeShape.values(), properties);
    }

    @Override
    public Block blockFor(PipeShape shape) {
        return shape == PipeShape.STRAIGHT ? getBlock() : ModBlocks.PIPE_BEND.get();
    }
}
```

The item places the block for its shape and is named after it. The first shape is the default and isn't stored, so those stacks stack with fresh items. Every shape's block drops the item and gives it on pick-block (return `item.stackOf(shape)` from the block's `getCloneItemStack` to keep the shape).

To show the shape on the item model, register an item property that reads `item.getShape(stack).ordinal()`.

## Anything else with shapes

Implement `ShapedItem` directly: list one stack per shape, say which is selected, and store a choice.
