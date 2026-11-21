package studio.offbyone.lib.shape;

import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * An item that places one of several shapes, picked from a radial menu on Create's toolbelt
 * key (Alt by default) while it's held. The library opens the menu and syncs the choice to the
 * server; the item only lists its shapes and stores the choice on the stack.
 * {@link ShapedBlockItem} does all of this for block items.
 */
public interface ShapedItem {

    /** One stack per shape, in menu order, each showing that shape. */
    List<ItemStack> shapeOptions(ItemStack held);

    /** The index in {@link #shapeOptions} of the shape {@code held} places now. */
    int selectedShape(ItemStack held);

    /** Store shape {@code index} on the stack. Runs on both sides. */
    void selectShape(ItemStack stack, int index);
}
