package net.succ.offbyonelib.client;

import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import net.createmod.catnip.lang.FontHelper.Palette;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

/**
 * Create-style item descriptions: "Hold [Shift] for Summary", read from the item's
 * {@code <description id>.tooltip.*} lang keys, plus stress and speed stats for kinetic blocks.
 * Call during client setup.
 */
public final class ItemTooltips {

    private ItemTooltips() {}

    public static void describe(ItemLike itemLike) {
        describe(itemLike, Palette.STANDARD_CREATE);
    }

    public static void describe(ItemLike itemLike, Palette palette) {
        Item item = itemLike.asItem();
        TooltipModifier modifier = new ItemDescription.Modifier(item, palette);
        KineticStats stats = KineticStats.create(item);
        if (stats != null) modifier = modifier.andThen(stats);
        TooltipModifier.REGISTRY.register(item, modifier);
    }
}
