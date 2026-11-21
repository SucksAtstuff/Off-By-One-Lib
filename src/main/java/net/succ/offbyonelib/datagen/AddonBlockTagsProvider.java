package net.succ.offbyonelib.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/** Block tags, with the shape and tool tags for {@link BlockFamily} sets. */
public abstract class AddonBlockTagsProvider extends BlockTagsProvider {

    protected AddonBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                                     String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, modId, existingFileHelper);
    }

    /**
     * Stairs, slabs and walls into their shape tags (the wooden ones for wooden families), and
     * every variant into {@code tool} (e.g. {@code BlockTags.MINEABLE_WITH_PICKAXE}). The base
     * block isn't touched.
     */
    protected void familyTags(BlockFamily family, boolean wooden, TagKey<Block> tool) {
        BlockFamilies.variants(family).forEach((variant, block) -> {
            switch (variant) {
                case STAIRS -> tag(wooden ? BlockTags.WOODEN_STAIRS : BlockTags.STAIRS).add(block);
                case SLAB -> tag(wooden ? BlockTags.WOODEN_SLABS : BlockTags.SLABS).add(block);
                case WALL -> tag(BlockTags.WALLS).add(block);
                default -> {}
            }
            tag(tool).add(block);
        });
    }
}
