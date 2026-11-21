# Off by One Lib

**Docs:** https://sucksatstuff.github.io/Off-By-One-Lib/

Shared code for Off by One Studio's Create addons, for NeoForge 1.21.1 and Create 6.

It's a library: it adds no blocks or items of its own. Addons extend its classes and register
everything under their own mod id, so a block built on the library still belongs to the addon.

## What's in it

- Multiblock fluid tanks (Create-style, formed with `ConnectivityHandler`) and the layer placer item
- Machine helpers: tank saving, hopper-safe item handlers, bucket/item interactions, goggle lines,
  generator direction scrolling, energy formatting
- Client: connected-texture tank and casing models, half-shaft renderer, tank gauges, looping
  machine sounds, a radial shape menu
- Wires: connectors, switches, relays and spools that route FE between blocks, with tiers addons
  can add to
- Solar: sun phase, sky and angle checks, with Sable, Create: Cosmonautics and Serene Seasons support
- Coming next: datagen tools (block families, default loot tables, tinted model parents, Ponder schematics and lang export)

## Using it in an addon

Bundle it with jarJar so players never install it separately. NeoForge loads the newest bundled
copy when several addons include it.

```groovy
dependencies {
    jarJar(implementation("studio.offbyone:offbyonelib:[0.1.0,1.0)")) {
        version { prefer "0.1.0" }
    }
}
```

While working on the library and an addon at the same time, point the addon at your local copy
in its `settings.gradle`:

```groovy
includeBuild("../Off By One Lib")
```

## Docs

The docs website is built from the Markdown in `wiki/pages` (`_Sidebar.md` is the navigation) and published to GitHub Pages on every push to `main` that touches `wiki/`. Preview it locally:

```bash
pip install -r wiki/requirements.txt
python wiki/build.py wiki/pages site
python -m http.server -d site
```

A `[[link]]` to a page that doesn't exist fails the build.

## Building

```bash
./gradlew build                # jar in build/libs
./gradlew publishToMavenLocal  # install to ~/.m2 for local addons
./gradlew runClient
```

## Versioning

Semver. Addons bundle a range, and a game may load a newer copy than an addon was built with, so
nothing addons call is removed or changed within a major version.

## License

MIT
