package studio.offbyone.lib.solar;

import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.tags.TagKey;

import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;

/**
 * One mod's solar settings for {@link SolarHelper}. Suppliers are read on every check, so they
 * can point straight at config values.
 *
 * @param alwaysSunny      dimension types with no night and no weather (deep space): noon output
 *                         around the clock, times {@code spaceMultiplier}
 * @param noWeather        dimension types with a day cycle but no rain (airless moons)
 * @param sunAngle         output follows the angle to the sun on Sable sub-levels (contraptions)
 * @param spaceMultiplier  output multiplier in always-sunny dimensions
 * @param realSpaceSun     read the real sun direction in Create: Cosmonautics deep space
 *                         (eclipses, station rotation); off means always-sunny there
 * @param seasons          scale output by Serene Seasons' season in the Overworld
 * @param summerMultiplier output multiplier at midsummer
 * @param winterMultiplier output multiplier at midwinter
 */
public record SolarSettings(TagKey<DimensionType> alwaysSunny, TagKey<DimensionType> noWeather,
                            BooleanSupplier sunAngle, DoubleSupplier spaceMultiplier, BooleanSupplier realSpaceSun,
                            BooleanSupplier seasons, DoubleSupplier summerMultiplier, DoubleSupplier winterMultiplier) {
}
