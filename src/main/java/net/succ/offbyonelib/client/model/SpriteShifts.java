package net.succ.offbyonelib.client.model;

import com.simibubi.create.foundation.block.connected.AllCTTypes;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTSpriteShifter;
import net.minecraft.resources.ResourceLocation;

/**
 * Connected texture shifts for {@link TankModel} and {@link CasingModel}. Each texture needs a
 * {@code <texture>_connected.png} sheet next to it, laid out like Create's.
 */
public final class SpriteShifts {

    private SpriteShifts() {}

    /** Tanks: joins in a rectangle, like Create's Fluid Tank. */
    public static CTSpriteShiftEntry rectangle(ResourceLocation texture) {
        return CTSpriteShifter.getCT(AllCTTypes.RECTANGLE, texture, connected(texture));
    }

    /** Casings: joins in every direction, like Create's casings. */
    public static CTSpriteShiftEntry omni(ResourceLocation texture) {
        return CTSpriteShifter.getCT(AllCTTypes.OMNIDIRECTIONAL, texture, connected(texture));
    }

    private static ResourceLocation connected(ResourceLocation texture) {
        return texture.withSuffix("_connected");
    }
}
