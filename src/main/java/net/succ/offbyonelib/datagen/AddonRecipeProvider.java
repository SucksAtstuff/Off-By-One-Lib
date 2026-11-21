package net.succ.offbyonelib.datagen;

import net.minecraft.advancements.Criterion;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/** Recipes, with helpers for block families, brick chains and metals. Recipe ids are under {@code modId}. */
public abstract class AddonRecipeProvider extends RecipeProvider {

    private final String modId;

    protected AddonRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, String modId) {
        super(output, registries);
        this.modId = modId;
    }

    protected ResourceLocation modLoc(String path) {
        return ResourceLocation.fromNamespaceAndPath(modId, path);
    }

    /**
     * Vanilla's crafting recipes for every variant (stairs, slab, wall, polished, chiseled...),
     * plus, when {@code stonecuttable}, stonecutting each one straight from the base block.
     * Vanilla's own stonecutter helper would save under the minecraft namespace, so these are
     * built by hand.
     */
    protected void blockFamilyRecipes(RecipeOutput output, BlockFamily family, boolean stonecuttable) {
        generateRecipes(output, family, FeatureFlags.DEFAULT_FLAGS);
        if (!stonecuttable) return;

        Block base = family.getBaseBlock();
        String baseName = BuiltInRegistries.BLOCK.getKey(base).getPath();
        BlockFamilies.variants(family).forEach((variant, block) -> {
            if (variant == BlockFamily.Variant.CRACKED) return; // smelted, not cut
            int count = variant == BlockFamily.Variant.SLAB ? 2 : 1;
            String name = BuiltInRegistries.BLOCK.getKey(block).getPath();
            SingleItemRecipeBuilder.stonecutting(Ingredient.of(base), RecipeCategory.BUILDING_BLOCKS, block, count)
                    .unlockedBy(getHasName(base), has(base))
                    .save(output, modLoc(name + "_from_" + baseName + "_stonecutting"));
        });
    }

    /**
     * Bricks: 4 from a 2x2 of {@code polished} (itself 2x2 of {@code raw}), like Deepslate's
     * polished -> bricks chain, or straight from either one in a stonecutter. Pass the raw
     * block as {@code polished} too when there's no polished step.
     */
    protected void bricksRecipes(RecipeOutput output, Block bricks, Block polished, Block raw) {
        String bricksName = BuiltInRegistries.BLOCK.getKey(bricks).getPath();
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, bricks, 4)
                .pattern("SS")
                .pattern("SS")
                .define('S', polished)
                .unlockedBy(getHasName(polished), has(polished))
                .save(output);
        for (Block from : Stream.of(polished, raw).distinct().toList()) {
            SingleItemRecipeBuilder.stonecutting(Ingredient.of(from), RecipeCategory.BUILDING_BLOCKS, bricks)
                    .unlockedBy(getHasName(from), has(from))
                    .save(output, modLoc(bricksName + "_from_" + BuiltInRegistries.BLOCK.getKey(from).getPath() + "_stonecutting"));
        }
    }

    /** Stonecutting {@code a} into {@code b} and back. */
    protected void stonecutBothWays(RecipeOutput output, Block a, Block b) {
        for (Block[] pair : new Block[][] {{a, b}, {b, a}}) {
            String to = BuiltInRegistries.BLOCK.getKey(pair[1]).getPath();
            String from = BuiltInRegistries.BLOCK.getKey(pair[0]).getPath();
            SingleItemRecipeBuilder.stonecutting(Ingredient.of(pair[0]), RecipeCategory.BUILDING_BLOCKS, pair[1])
                    .unlockedBy(getHasName(pair[0]), has(pair[0]))
                    .save(output, modLoc(to + "_from_" + from + "_stonecutting"));
        }
    }

    /** Smelting and blasting recipes turning {@code input} into a metal ingot. */
    protected void ingotSmelting(RecipeOutput output, TagKey<Item> input, String inputName, Item ingot, String metal) {
        ingotSmelting(output, Ingredient.of(input), has(input), inputName, ingot, metal);
    }

    protected void ingotSmelting(RecipeOutput output, Ingredient input, Criterion<?> unlock, String inputName,
                                 Item ingot, String metal) {
        SimpleCookingRecipeBuilder.smelting(input, RecipeCategory.MISC, ingot, 0.7f, 200)
                .unlockedBy("has_" + inputName, unlock)
                .save(output, modLoc(metal + "_ingot_from_smelting_" + inputName));

        SimpleCookingRecipeBuilder.blasting(input, RecipeCategory.MISC, ingot, 0.7f, 100)
                .unlockedBy("has_" + inputName, unlock)
                .save(output, modLoc(metal + "_ingot_from_blasting_" + inputName));
    }

    /**
     * 9 nuggets <-> ingot <-> 9 ingots as a storage block. Inputs use the given (usually c:)
     * tags, so any mod's version packs and unpacks into yours.
     */
    protected void metalStorage(RecipeOutput output, String metal, Item nugget, Item ingot, ItemLike block,
                                TagKey<Item> nuggetTag, TagKey<Item> ingotTag, TagKey<Item> blockTag) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ingot)
                .pattern("NNN")
                .pattern("NNN")
                .pattern("NNN")
                .define('N', nuggetTag)
                .unlockedBy("has_" + metal + "_nugget", has(nuggetTag))
                .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, nugget, 9)
                .requires(ingotTag)
                .unlockedBy("has_" + metal + "_ingot", has(ingotTag))
                .save(output, modLoc(metal + "_nugget_from_ingot"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, block)
                .pattern("III")
                .pattern("III")
                .pattern("III")
                .define('I', ingotTag)
                .unlockedBy("has_" + metal + "_ingot", has(ingotTag))
                .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ingot, 9)
                .requires(blockTag)
                .unlockedBy("has_" + metal + "_block", has(blockTag))
                .save(output, modLoc(metal + "_ingot_from_block"));
    }
}
