package studio.offbyone.lib.datagen;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.ModelFile.UncheckedModelFile;
import net.neoforged.neoforge.client.model.generators.ModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;
import studio.offbyone.lib.OffByOneLib;

import java.util.function.Function;

/**
 * Blockstates and models, with block families generated in one call and tinted versions for
 * blocks coloured by a block colour handler.
 */
public abstract class AddonBlockStateProvider extends BlockStateProvider {

    protected AddonBlockStateProvider(PackOutput output, String modId, ExistingFileHelper existingFileHelper) {
        super(output, modId, existingFileHelper);
    }

    /**
     * Every variant of a family; the base block isn't covered. Stairs, slabs and walls use
     * {@code baseTexture} (null for {@code block/<base id>}); full-block variants (polished,
     * chiseled...) use {@code block/<variant id>} on every face, or on the sides with
     * {@code <variant id>_top} on the top and bottom when that texture exists.
     */
    protected void blockFamily(BlockFamily family, @Nullable ResourceLocation baseTexture) {
        ResourceLocation baseModel = blockTexture(family.getBaseBlock()); // block/<base id>
        ResourceLocation tex = baseTexture != null ? baseTexture : baseModel;
        BlockFamilies.variants(family).forEach((variant, block) -> {
            String path = BuiltInRegistries.BLOCK.getKey(block).getPath();
            switch (variant) {
                case STAIRS -> {
                    stairsBlock((StairBlock) block, tex);
                    simpleBlockItem(block, new UncheckedModelFile(modLoc("block/" + path)));
                }
                case SLAB -> {
                    // The double slab reuses the base block's model. Passed unchecked, since
                    // the base model may only be generated later in this run.
                    slabBlock((SlabBlock) block, models().slab(path, tex, tex, tex),
                            models().slabTop(path + "_top", tex, tex, tex), new UncheckedModelFile(baseModel));
                    simpleBlockItem(block, new UncheckedModelFile(modLoc("block/" + path)));
                }
                case WALL -> {
                    wallBlock((WallBlock) block, tex);
                    itemModels().wallInventory(path, tex);
                }
                default -> {
                    // A <id>_top texture, if one exists, goes on the top and bottom.
                    ResourceLocation side = blockTexture(block);
                    ResourceLocation top = side.withSuffix("_top");
                    simpleBlockWithItem(block, models().existingFileHelper.exists(top, ModelProvider.TEXTURE)
                            ? models().cubeColumn(path, side, top)
                            : cubeAll(block));
                }
            }
        });
    }

    /**
     * A tinted family, base block included: the same shapes as {@link #blockFamily}, built on
     * the library's copies of vanilla's parents ({@code offbyonelib:block/tinted/*}) whose faces
     * have tintindex 0, so a block colour handler can colour one grey texture per shape. A
     * {@code <texture>_top.png}, if one exists, goes on the top and bottom faces of every shape.
     * Full-block variants get their texture from {@code variantTexture}. Tinting only darkens,
     * so keep the textures light grey.
     */
    protected void tintedBlockFamily(BlockFamily family, ResourceLocation texture,
                                     Function<Block, ResourceLocation> variantTexture) {
        ResourceLocation tex = texture;
        ResourceLocation top = topOrSide(tex);
        Block base = family.getBaseBlock();
        String basePath = BuiltInRegistries.BLOCK.getKey(base).getPath();
        ModelFile cube = tintedCube(basePath, tex);
        simpleBlockWithItem(base, cube);
        BlockFamilies.variants(family).forEach((variant, block) -> {
            String path = BuiltInRegistries.BLOCK.getKey(block).getPath();
            switch (variant) {
                case STAIRS -> {
                    stairsBlock((StairBlock) block,
                            sideTopBottom(tinted(path, "stairs"), tex, top),
                            sideTopBottom(tinted(path + "_inner", "inner_stairs"), tex, top),
                            sideTopBottom(tinted(path + "_outer", "outer_stairs"), tex, top));
                    simpleBlockItem(block, new UncheckedModelFile(modLoc("block/" + path)));
                }
                case SLAB -> {
                    slabBlock((SlabBlock) block, sideTopBottom(tinted(path, "slab"), tex, top),
                            sideTopBottom(tinted(path + "_top", "slab_top"), tex, top), cube);
                    simpleBlockItem(block, new UncheckedModelFile(modLoc("block/" + path)));
                }
                case WALL -> {
                    wallBlock((WallBlock) block,
                            tinted(path + "_post", "wall_post").texture("wall", tex).texture("top", top),
                            tinted(path + "_side", "wall_side").texture("wall", tex).texture("top", top),
                            tinted(path + "_side_tall", "wall_side_tall").texture("wall", tex).texture("top", top));
                    itemModels().getBuilder(path).parent(tintedParent("wall_inventory"))
                            .texture("wall", tex).texture("top", top);
                }
                default -> {
                    ResourceLocation ownTex = variantTexture.apply(block);
                    if (ownTex == null) throw new IllegalStateException("No tinted texture for " + path);
                    simpleBlockWithItem(block, tintedCube(path, ownTex));
                }
            }
        });
    }

    /** A tinted full block: cube_all, or cube_column when a {@code <texture>_top.png} exists. */
    protected ModelFile tintedCube(String path, ResourceLocation tex) {
        ResourceLocation top = topOrSide(tex);
        return top.equals(tex)
                ? tinted(path, "cube_all").texture("all", tex)
                : tinted(path, "cube_column").texture("side", tex).texture("end", top);
    }

    /** {@code <texture>_top} if that texture exists, otherwise the texture itself. */
    protected ResourceLocation topOrSide(ResourceLocation tex) {
        ResourceLocation top = tex.withSuffix("_top");
        return models().existingFileHelper.exists(top, ModelProvider.TEXTURE) ? top : tex;
    }

    /** A block model on one of the tinted parents (cube_all, cube_column, stairs, slab, wall_post...). */
    protected BlockModelBuilder tinted(String path, String parent) {
        return models().getBuilder(path).parent(tintedParent(parent));
    }

    // The library's own model, so the addon's ExistingFileHelper can't see it: unchecked.
    private static ModelFile tintedParent(String parent) {
        return new UncheckedModelFile(OffByOneLib.id("block/tinted/" + parent));
    }

    protected static BlockModelBuilder sideTopBottom(BlockModelBuilder model, ResourceLocation side, ResourceLocation top) {
        return model.texture("side", side).texture("top", top).texture("bottom", top);
    }

    /** Y rotation for a model authored facing north. */
    protected static int horizontalYRot(Direction facing) {
        return switch (facing) {
            case EAST -> 90;
            case SOUTH -> 180;
            case WEST -> 270;
            default -> 0; // NORTH
        };
    }
}
