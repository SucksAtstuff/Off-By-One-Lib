package studio.offbyone.lib.datagen;

import net.minecraft.data.BlockFamily;
import net.minecraft.world.level.block.Block;

import java.util.EnumMap;
import java.util.Map;

/** Helpers for vanilla {@link BlockFamily} sets (stairs, slabs, walls, polished, chiseled...) in datagen. */
public final class BlockFamilies {

    private BlockFamilies() {}

    /**
     * The family's variants in {@link BlockFamily.Variant} order. BlockFamily keeps them in a
     * HashMap, so iterating {@code getVariants()} directly reshuffles tag files every run.
     */
    public static Map<BlockFamily.Variant, Block> variants(BlockFamily family) {
        Map<BlockFamily.Variant, Block> sorted = new EnumMap<>(BlockFamily.Variant.class);
        sorted.putAll(family.getVariants());
        return sorted;
    }
}
