package studio.offbyone.lib.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Registers a block and its item together into an addon's own registers, and keeps track of
 * decorative blocks (stairs, slabs, walls and the like) for a separate creative tab.
 *
 * <pre>{@code
 * public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
 * private static final BlockRegistrar REG = new BlockRegistrar(BLOCKS, ModItems.ITEMS);
 * public static final DeferredBlock<Block> SALT_BLOCK = REG.decorative("salt_block", () -> new Block(...));
 * public static final DeferredBlock<StairBlock> SALT_STAIRS = REG.stairs("salt_stairs", SALT_BLOCK);
 * }</pre>
 */
public class BlockRegistrar {

    private final DeferredRegister.Blocks blocks;
    private final DeferredRegister.Items items;
    private final List<DeferredBlock<? extends Block>> decorative = new ArrayList<>();

    public BlockRegistrar(DeferredRegister.Blocks blocks, DeferredRegister.Items items) {
        this.blocks = blocks;
        this.items = items;
    }

    /** A block with a plain BlockItem. */
    public <T extends Block> DeferredBlock<T> block(String name, Supplier<T> block) {
        return blockWithItem(name, block, b -> new BlockItem(b, new Item.Properties()));
    }

    /** A block with its own item class (multiblock placers, items with shapes, custom renderers). */
    public <T extends Block> DeferredBlock<T> blockWithItem(String name, Supplier<T> block,
                                                            Function<T, ? extends BlockItem> itemFactory) {
        DeferredBlock<T> registered = blocks.register(name, block);
        items.register(name, () -> itemFactory.apply(registered.get()));
        return registered;
    }

    /** A block for the building blocks tab instead of the main one. */
    public <T extends Block> DeferredBlock<T> decorative(String name, Supplier<T> block) {
        return track(block(name, block));
    }

    public <T extends Block> DeferredBlock<T> decorativeWithItem(String name, Supplier<T> block,
                                                                 Function<T, ? extends BlockItem> itemFactory) {
        return track(blockWithItem(name, block, itemFactory));
    }

    // Stairs, slabs and walls copy every property of the block they're made from, and are
    // decorative. Register them after their base block.

    public DeferredBlock<StairBlock> stairs(String name, Supplier<? extends Block> base) {
        return decorative(name, () -> new StairBlock(base.get().defaultBlockState(),
                BlockBehaviour.Properties.ofFullCopy(base.get())));
    }

    public DeferredBlock<SlabBlock> slab(String name, Supplier<? extends Block> base) {
        return decorative(name, () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(base.get())));
    }

    public DeferredBlock<WallBlock> wall(String name, Supplier<? extends Block> base) {
        return decorative(name, () -> new WallBlock(BlockBehaviour.Properties.ofFullCopy(base.get()).forceSolidOn()));
    }

    /** Every decorative block, in registration order. */
    public List<DeferredBlock<? extends Block>> decorativeBlocks() {
        return Collections.unmodifiableList(decorative);
    }

    private <T extends Block> DeferredBlock<T> track(DeferredBlock<T> block) {
        decorative.add(block);
        return block;
    }
}
