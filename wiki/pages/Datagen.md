# Datagen

Base classes for an addon's data generators, so the parts every addon repeats come for free. You still run `./gradlew runData` in your addon, and it writes into your own `src/generated/resources`; your providers extend these bases and keep your own content.

## Block families

Vanilla's `BlockFamily` sets (stairs, slab, wall, polished, chiseled...) built on a base block. Always walk a family's variants with `BlockFamilies.variants(family)`: vanilla keeps them in a HashMap, so iterating `getVariants()` directly reshuffles your tag files every run.

### Models

Extend `AddonBlockStateProvider`:

```java
public class ModBlockStateProvider extends AddonBlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, MODID, helper);
    }

    @Override
    protected void registerStatesAndModels() {
        blockWithItem(ModBlocks.SALT_BLOCK);
        blockFamily(SALT_FAMILY, null);  // null: stairs/slabs/walls use block/<base id>
    }
}
```

`blockFamily(family, baseTexture)` makes every variant; the base block keeps its own model. Full-block variants use `block/<variant id>`, or `block/<variant id>_top` on the top and bottom when that texture exists. `horizontalYRot(facing)` and `sideTopBottom(...)` help with the rest of your models.

### Tinted blocks

For blocks coloured by a block colour handler (one grey texture, many colours), `tintedBlockFamily(family, texture, variantTexture)` builds every shape, base block included, on the library's tinted parents: copies of vanilla's cube, stairs, slab and wall models with `tintindex: 0` on every face (`offbyonelib:block/tinted/*`). Vanilla's own parents have no tint index, so a colour handler would be ignored. Register your block and item colour handlers as usual; tinting only darkens, so keep the texture light grey.

### Tags, recipes and loot

```java
// AddonBlockTagsProvider: shape tags (wooden ones for wooden families) and a tool tag
familyTags(SALT_FAMILY, false, BlockTags.MINEABLE_WITH_PICKAXE);

// AddonRecipeProvider: vanilla's crafting recipes, plus stonecutting from the base
blockFamilyRecipes(output, SALT_FAMILY, true);

// AddonBlockLootProvider: double slabs drop two
familySlabs(List.of(SALT_FAMILY));
```

## Loot that defaults to dropping itself

`AddonBlockLootProvider` gives every block in your register a "drops itself" table unless `generateDrops()` adds another. A new block that should drop something else (or nothing) needs an entry there, or it silently drops itself. Fluid blocks are skipped.

```java
public class ModLootProvider extends AddonBlockLootProvider {
    public ModLootProvider(HolderLookup.Provider registries) {
        super(registries, ModBlocks.BLOCKS);
    }

    @Override
    protected void generateDrops() {
        add(ModBlocks.LITHIUM_ORE.get(), block -> createOreDrop(block, ModItems.RAW_LITHIUM.get()));
        dropWhenSilkTouch(ModBlocks.SOLAR_GLASS.get());
    }
}
```

## Recipe helpers

`AddonRecipeProvider` saves under your mod id (`modLoc(path)`) and adds:

| Method | Recipes |
|---|---|
| `bricksRecipes(output, bricks, polished, raw)` | 4 bricks from a 2x2 of the polished block, or from either block in a stonecutter |
| `stonecutBothWays(output, a, b)` | Stonecutting a into b and back |
| `ingotSmelting(output, input, inputName, ingot, metal)` | Smelting and blasting into an ingot |
| `metalStorage(output, metal, nugget, ingot, block, nuggetTag, ingotTag, blockTag)` | Nuggets, ingot and storage block, both ways, from tags |

`DatagenUtil.createItem("brass_casing")` looks up a Create item; `itemTag` and `blockTag` make tag keys.

## Ponder

Build Ponder schematics in code instead of saving structures by hand:

```java
public class ModPonderProvider extends PonderSchematicProvider {
    private static final Map<String, SceneStructure> SCHEMATICS = new LinkedHashMap<>();
    static {
        SCHEMATICS.put("heater/usage", new SceneStructure(5, 4, 5).withBasePlate()
                .addBlock(2, 1, 2, "mymod:heater", "facing", "north", "lit", "false")
                .addBlock(1, 1, 2, "create:fluid_pipe"));
    }

    public ModPonderProvider(PackOutput output) {
        super(output, MODID);
    }

    @Override
    protected Map<String, SceneStructure> schematics() {
        return SCHEMATICS;
    }
}
```

They're written to `assets/<modid>/ponder/<path>.nbt`. Multiblocks don't form in Ponder, so save formed ones with `addBlockWithNbt` (controller and size), or they show as separate blocks. Gzip timestamps make every schematic show as changed after each `runData`; revert the ones whose scenes you didn't touch.

Ponder scene and tag text can live in the scene code. Export it in your lang provider:

```java
PonderLang.provide(new MyPonderPlugin(), MODID, this::add);
```

## Lang

`AddonLangProvider` builds the repetitive keys, so only the text is typed out. Pass your `GoggleLines`, so goggle labels go under the same keys your block entities read:

```java
public class ModLangProvider extends AddonLangProvider {
    public ModLangProvider(PackOutput output) {
        super(output, MODID, MyMod.GOGGLES);
    }

    @Override
    protected void addTranslations() {
        // block.<modid>.engine.tooltip.summary / condition1 / behaviour1 ...
        tooltip(ModBlocks.ENGINE, "Burns _fuel_ for _Rotational Force_.",
                "While fuelled", "Outputs 16 RPM.");
        goggle("fuel", "Fuel: ");                 // under your GoggleLines prefix
        subtitle("engine_loop", "Engine running"); // subtitles.<modid>.engine_loop
        jei("category.mixing", "Mixing");          // jei.<modid>.category.mixing
        add(ModBlocks.ENGINE.get(), toTitleCase("engine"));
    }
}
```

`tooltip` takes the block itself, so a renamed block is a compile error; the `String` overload is for blocks registered without a field. A condition without a behaviour fails datagen. The tooltips still need registering on the client (`ItemTooltips`).
