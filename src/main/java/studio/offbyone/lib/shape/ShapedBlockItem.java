package studio.offbyone.lib.shape;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * One item for several blocks: it places whichever shape is stored on the stack, is named after
 * it, and the other shapes' blocks drop it and give it on pick-block.
 *
 * The shape lives in a data component the addon registers (its id is saved on item stacks).
 * The first shape is the default and isn't stored, so those stacks stack with fresh items.
 *
 * @param <S> the shape enum
 */
public abstract class ShapedBlockItem<S extends Enum<S>> extends BlockItem implements ShapedItem {

    private final Supplier<? extends DataComponentType<S>> component;
    private final S[] shapes;

    protected ShapedBlockItem(Block block, Supplier<? extends DataComponentType<S>> component, S[] shapes,
                              Properties properties) {
        super(block, properties);
        this.component = component;
        this.shapes = shapes;
    }

    /** The block placed for {@code shape}. */
    public abstract Block blockFor(S shape);

    public Block blockFor(ItemStack stack) {
        return blockFor(getShape(stack));
    }

    public S getShape(ItemStack stack) {
        return stack.getOrDefault(component.get(), shapes[0]);
    }

    public void setShape(ItemStack stack, S shape) {
        if (shape == shapes[0]) stack.remove(component.get());
        else stack.set(component.get(), shape);
    }

    public ItemStack stackOf(S shape) {
        ItemStack stack = new ItemStack(this);
        setShape(stack, shape);
        return stack;
    }

    @Override
    protected BlockState getPlacementState(BlockPlaceContext context) {
        BlockState state = blockFor(context.getItemInHand()).getStateForPlacement(context);
        return state != null && canPlace(context, state) ? state : null;
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return blockFor(stack).getDescriptionId();
    }

    @Override
    public void registerBlocks(Map<Block, Item> blockToItemMap, Item item) {
        super.registerBlocks(blockToItemMap, item);
        for (S shape : shapes)
            blockToItemMap.put(blockFor(shape), item);
    }

    @Override
    public List<ItemStack> shapeOptions(ItemStack held) {
        return Arrays.stream(shapes).map(this::stackOf).toList();
    }

    @Override
    public int selectedShape(ItemStack held) {
        return getShape(held).ordinal();
    }

    @Override
    public void selectShape(ItemStack stack, int index) {
        if (index >= 0 && index < shapes.length)
            setShape(stack, shapes[index]);
    }
}
