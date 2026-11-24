# 1.1.0

## New

### KubeJS

- **KubeJS support without a KubeJS dependency** - the library ships its own KubeJS plugin, loaded by KubeJS only when it's installed. Addons opt in with data files, so they never need KubeJS to build or run.
- **Data map events** - list your NeoForge data maps in `offbyonelib.kubejs.json` and server scripts get one event per map, like `MyModEvents.engineFuels(event => event.add('minecraft:lava', {...}))`. Values use the map's own JSON format, edits apply on every `/reload`, and a bad id or value is reported as a script error.
- **`KubeRecipeSchemaProvider`** - datagen for KubeJS recipe schemas, so your recipe types get script syntax like `event.recipes.mymod.pressing(...)` with builder calls like `.processingTime(200)`.

### Datagen

- **`AddonLangProvider`** - a lang provider with helpers for the repetitive keys: `tooltip(block, summary, condition, behaviour, ...)` for Create-style item descriptions, plus `goggle()`, `subtitle()`, `jei()` and `toTitleCase()`. A misspelled block is a compile error, and a condition without a behaviour fails datagen.

### JEI

- **Recipe IDs** - `AddonRecipeCategory` now tells JEI each recipe's ID, so it shows on the output's tooltip with advanced tooltips on (F3+H) and bookmarks remember it. Categories whose entries wrap a recipe override `recipeOf`.

## Changes

- `GoggleLines.key()` returns a label's full lang key.
- `test_kubejs` in `gradle.properties` adds KubeJS to dev runs.

# 1.0.0

First release: registration helpers, creative tab filling, multiblock tanks, machine helpers, the shape menu, wires, solar helpers with space and season compat, JEI and Display Link bases, Create-style advancements, and datagen bases for block families, loot, recipes, tags and Ponder.
