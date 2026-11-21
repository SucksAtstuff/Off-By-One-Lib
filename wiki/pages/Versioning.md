# Versioning

Several mods can bundle different versions of the library, and NeoForge loads only the newest. So an addon built against 1.0 may run on 1.4. That only works if newer versions never break older addons.

## The rules

- Versions are plain semver: `1.4.2`, with no Minecraft version in it (the jar name carries that).
- Within a major version, nothing public is removed or changed. New things are added, and old ones are marked `@Deprecated` and kept.
- A breaking change means a new major version, and addons raise their range to match.
- Code in an `internal` package can change at any time. Don't call it from an addon.

## Ranges in addons

Bundle with a range that starts at the version you built against and stops before the next major:

```groovy
strictly "[1.2.0,2.0)"
prefer "1.2.0"
```

Before 1.0 there's no promise between minor versions, so pre-1.0 addons should use a range that stops at 1.0.
