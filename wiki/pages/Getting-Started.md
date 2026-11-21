# Getting Started

The library is bundled inside your addon's jar with NeoForge's jar-in-jar, so players never install it separately. When several mods bundle it, NeoForge loads the newest copy.

## Add the dependency

In `build.gradle`:

```groovy
dependencies {
    jarJar(implementation("studio.offbyone:offbyonelib")) {
        version {
            strictly "[${offbyonelib_version},1.0)"
            prefer offbyonelib_version
        }
    }
}
```

and in `gradle.properties`:

```properties
offbyonelib_version=0.1.0
```

The range says which versions your addon works with. Keep its top at the next major version (see [[Versioning]]).

## Work on the library and an addon together

Check the library out next to your addon and include it as a composite build in the addon's `settings.gradle`:

```groovy
if (file("../Off By One Lib").isDirectory()) {
    includeBuild("../Off By One Lib")
}
```

Gradle then builds the library from source, so a change shows up in the addon straight away. Without the folder it downloads the published version, so other people can still build your addon.

## What's in it

| Package | What |
|---|---|
| `registry` | `BlockRegistrar`, `CreativeTabContents` ([[Registration]]) |
| `util` | Tank saving, hopper-safe inventories, bucket handling, goggle lines, energy formatting ([[Machine Helpers]]) |
| `multiblock` | Create-style fluid tanks and their layer item ([[Multiblock Tanks]]) |
| `client` | Connected texture models, half shafts, tank gauges, machine sounds, item tooltips |
| `shape` | Items with several shapes, picked from a radial menu ([[Shape Menu]]) |
| `wire` | FE wires, connectors, switches and spools ([[Wires]]) |
| `solar` | Sunlight checks and roof-style solar blocks ([[Solar]]) |
| `advancement`, `compat.jei`, `display` | [[Advancements]], [[JEI and Display Links]] |
| `datagen` | Block families, tinted models, loot, recipes and Ponder schematics ([[Datagen]]) |
