package studio.offbyone.lib.datagen;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Collection;
import java.util.Set;

/**
 * Block loot where every block drops itself unless {@link #generateDrops} gives it a table.
 * A new block that should drop something else (or nothing) needs an entry there, or it
 * silently drops itself. Fluid blocks are skipped.
 */
public abstract class AddonBlockLootProvider extends BlockLootSubProvider {

    private final DeferredRegister.Blocks blocks;

    protected AddonBlockLootProvider(HolderLookup.Provider registries, DeferredRegister.Blocks blocks) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
        this.blocks = blocks;
    }

    /** Tables for the blocks that don't just drop themselves. */
    protected abstract void generateDrops();

    @Override
    protected final void generate() {
        generateDrops();
        for (Block block : getKnownBlocks()) {
            if (!map.containsKey(block.getLootTable())) dropSelf(block);
        }
    }

    /** Slabs of these families drop two when doubled. */
    protected void familySlabs(Collection<BlockFamily> families) {
        families.forEach(family -> {
            Block slab = family.get(BlockFamily.Variant.SLAB);
            if (slab != null) add(slab, this::createSlabItemTable);
        });
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return blocks.getEntries().stream()
                .map(Holder::value)
                .filter(b -> !(b instanceof LiquidBlock))
                ::iterator;
    }
}
