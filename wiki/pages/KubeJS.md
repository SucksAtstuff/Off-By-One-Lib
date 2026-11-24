# KubeJS

The library makes an addon scriptable with KubeJS without the addon depending on KubeJS. There are two parts, and both are plain data your addon ships:

- **Recipe schemas** give your recipe types script syntax like `event.recipes.mymod.pressing(...)`.
- **Data map events** let scripts edit your NeoForge data maps (fuel values, heat values and so on) with `MyModEvents.engineFuels(event => ...)`.

The library's own KubeJS plugin (`net.succ.offbyonelib.kubejs`) is only loaded by KubeJS, so nothing here runs, or needs KubeJS, when it isn't installed.

## Recipe schemas

KubeJS reads a schema for each recipe type from `data/<modid>/kubejs/recipe_schema/<type>.json`. Generate them with `KubeRecipeSchemaProvider`:

```java
public class ModKubeSchemaProvider extends KubeRecipeSchemaProvider {
    public ModKubeSchemaProvider(PackOutput output) {
        super(output, MODID);
    }

    @Override
    protected void addSchemas() {
        // event.recipes.mymod.pressing('minecraft:paper', '#minecraft:logs').processingTime(200)
        schema("pressing")
                .key("result", Role.OUTPUT, ITEM_STACK)
                .key("ingredient", Role.INPUT, INGREDIENT)
                .key("processing_time", Role.INPUT, TICKS).optional(100).alwaysWrite().functionNames("processingTime")
                .unique("result");
    }
}
```

- Key names are your recipe JSON's field names, in the order the script's constructor takes them. Optional keys go last.
- `optional(value)` gives a default. `optional()` defaults to the component's empty value, such as an empty fluid.
- `alwaysWrite()` writes the default anyway. Use it when your codec requires the field.
- `functionNames(...)` are the builder calls that set the key.
- The component types are KubeJS's. The common ones are constants: `ITEM_STACK`, `INGREDIENT`, `FLAT_SIZED_INGREDIENT`, `FLUID_STACK`, `OPTIONAL_FLUID_STACK`, `TICKS`, `BOOLEAN`, `POSITIVE_INT`, `NON_NEGATIVE_INT` and `NON_NEGATIVE_FLOAT`. There are also `list(component)`, `intRange(min, max)` and `floatRange(min, max)`.

## Data map events

Put the values players might want to change in NeoForge data maps. That already lets datapacks change them. Then list the maps in `src/main/resources/offbyonelib.kubejs.json`:

```json
{
  "event_group": "MyModEvents",
  "data_maps": {
    "engineFuels": "mymod:engine_fuels"
  }
}
```

Server scripts then get one event per map, run on every `/reload`:

```js
MyModEvents.engineFuels(event => {
  event.add('minecraft:lava', { ticks_per_bucket: 400 })  // an id or a #tag
  event.remove('#c:biofuel')                              // also removes your mod's own defaults
})
```

How it works:
- **Values** are plain objects in the map's JSON format, decoded with the map's own codec.
- **Bad input:** an unknown id or a bad value is reported as a script error naming the map.
- **Where edits go:** into KubeJS's generated datapack, on top of the addon's defaults and other datapacks.
- **Shared groups:** mods that use the same group name share one group.
