# Solar

`SolarHelper` answers "how much sun does this block get?" the same way for every solar block, including the hard cases: space dimensions with no night, the real sun in Create: Cosmonautics deep space (eclipses and station rotation), blocks on Sable sub-levels (Create: Aeronautics contraptions) that tilt toward or away from the sun and get shaded by other contraptions, and Serene Seasons.

## Settings

Make one `SolarHelper` per mod, with that mod's dimension tags and config:

```java
public static final SolarHelper SOLAR = new SolarHelper(new SolarSettings(
        ModTags.ALWAYS_SUNNY, ModTags.NO_WEATHER,  // dimension type tags
        () -> Config.sunAngle,                     // tilt matters on contraptions
        () -> Config.spaceMultiplier,              // output multiplier in always-sunny dimensions
        () -> Config.realSpaceSun,                 // read Cosmonautics' real sun
        () -> Config.seasons,                      // Serene Seasons scaling
        () -> Config.summerMultiplier, () -> Config.winterMultiplier));
```

Dimension types in the always-sunny tag run at noon output around the clock with no weather; ones in the no-weather tag keep their day cycle but never rain. Let datapacks and space mods add their dimensions to your tags.

## Output

```java
// 10 at dawn and dusk (and in the rain), 40 at noon, 0 at night or under a roof
float fe = SOLAR.byPhase(level, pos, 10, 40) * SOLAR.outputMultiplier(level, pos);
```

| Method | Gives |
|---|---|
| `getPhase(level, pos)` | `NIGHT`, `MORNING`, `NOON` or `EVENING` |
| `byPhase(level, pos, dawn, noon)` | Output for the current sun, 0 without sky access |
| `outputMultiplier(level, pos)` | Seasons and space multipliers combined |
| `hasSkyAccess(level, pos)`, `canSeeSky(level, pos)` | Sky checks that work in space and on contraptions |
| `isDaytime`, `isRaining`, `isThundering` | Weather that respects no-weather dimensions |
| `tiltFactor(level, pos, normal, max)` | Extra output for a surface tilted toward the sun (with Sable and `sunAngle` on) |
| `sunAngle(level)`, `sunOffset(level, pos)`, `sunExposure(...)`, `comparatorSignal(...)` | Readouts for Display Links and comparators |

Space and contraption sky checks are cached for a second, so calling these every tick is fine. The Cosmonautics sun is only known on the server; sync anything the client shows.

## Solar roofs and windows

`SharedSolarBlockEntity` is for small solar blocks laid out in large numbers, like roof tiles or glass. Each generates into its own buffer and passes energy to touching ones that hold less, including diagonally up or down a row, so draining any one block drains the whole roof through it. Different subclasses share with each other.

## Compat

Sable, Cosmonautics and Serene Seasons are reached through reflection only, so none of them is a dependency. Cosmonautics has no public API for this yet, so its compat may break on their updates; any failure disables it, logs once, and falls back to the always-sunny tag. `SableCompat.forEachSubLevelBlockEntity(level, visitor)` also lets you scan every block entity on every sub-level with the chunk it's really in.
