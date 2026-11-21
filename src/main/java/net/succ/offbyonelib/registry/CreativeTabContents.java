package net.succ.offbyonelib.registry;

import com.simibubi.create.content.processing.sequenced.SequencedAssemblyItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/** Creative tabs that fill themselves from an addon's registers, so new entries need no tab edit. */
public final class CreativeTabContents {

    private CreativeTabContents() {}

    /**
     * Everything the addon registers except {@code exclude}: blocks in registration order,
     * then items, then buckets. Blocks without an item of their own (fluids, and extra shapes
     * that borrow another block's item) are skipped, and so are sequenced assembly's
     * in-between items.
     */
    public static void addAll(CreativeModeTab.Output output, DeferredRegister.Blocks blocks,
                              DeferredRegister.Items items, Collection<? extends Supplier<? extends Block>> exclude) {
        Set<Block> excluded = exclude.stream().map(Supplier::get).collect(Collectors.toSet());
        for (DeferredHolder<Block, ? extends Block> block : blocks.getEntries()) {
            if (block.get().asItem() instanceof BlockItem item && item.getBlock() == block.get()
                    && !excluded.contains(block.get()))
                output.accept(item);
        }
        List<Item> plainItems = items.getEntries().stream()
                .<Item>map(DeferredHolder::get)
                .filter(item -> !(item instanceof BlockItem) && !(item instanceof SequencedAssemblyItem))
                .toList();
        plainItems.stream().filter(item -> !(item instanceof BucketItem)).forEach(output::accept);
        plainItems.stream().filter(item -> item instanceof BucketItem).forEach(output::accept);
    }

    /** Exactly these blocks, in order (e.g. {@link BlockRegistrar#decorativeBlocks()}). */
    public static void addBlocks(CreativeModeTab.Output output, Collection<? extends Supplier<? extends Block>> blocks) {
        blocks.forEach(block -> output.accept(block.get()));
    }
}
