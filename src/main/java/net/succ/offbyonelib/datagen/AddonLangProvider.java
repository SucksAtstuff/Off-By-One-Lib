package net.succ.offbyonelib.datagen;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.succ.offbyonelib.util.GoggleLines;

/**
 * A LanguageProvider with helpers that build an addon's repetitive lang keys, so only the text
 * is typed out:
 *
 * <pre>{@code
 * tooltip(ModBlocks.ENGINE, "Burns _fuel_ for _Rotational Force_.",
 *         "While fuelled", "Outputs 16 RPM.");
 * goggle("fuel", "Fuel: ");
 * subtitle("engine_loop", "Engine running");
 * jei("category.mixing", "Mixing");
 * }</pre>
 */
public abstract class AddonLangProvider extends LanguageProvider {

    protected final String modId;
    private final GoggleLines goggles;

    /** {@code goggles} is the addon's own GoggleLines, so goggle() writes the keys it reads. */
    protected AddonLangProvider(PackOutput output, String modId, GoggleLines goggles) {
        super(output, modId, "en_us");
        this.modId = modId;
        this.goggles = goggles;
    }

    /**
     * Create-style item tooltip ({@code block.<modid>.<id>.tooltip.*}): the summary, then
     * condition/behaviour pairs, numbered from 1.
     */
    protected void tooltip(DeferredBlock<?> block, String summary, String... conditionsAndBehaviours) {
        tooltip(block.getId().getPath(), summary, conditionsAndBehaviours);
    }

    /** The same, by block id, for blocks registered without a field (in a loop, say). */
    protected void tooltip(String blockId, String summary, String... conditionsAndBehaviours) {
        if (conditionsAndBehaviours.length % 2 != 0)
            throw new IllegalArgumentException("Tooltip for " + blockId + " has a condition without a behaviour");
        String base = "block." + modId + "." + blockId + ".tooltip.";
        add(base + "summary", summary);
        for (int i = 0; i < conditionsAndBehaviours.length; i += 2) {
            add(base + "condition" + (i / 2 + 1), conditionsAndBehaviours[i]);
            add(base + "behaviour" + (i / 2 + 1), conditionsAndBehaviours[i + 1]);
        }
    }

    /** A goggle line label, under the addon's GoggleLines key. */
    protected void goggle(String key, String text) {
        add(goggles.key(key), text);
    }

    /** A sound subtitle: {@code subtitles.<modid>.<sound>}. */
    protected void subtitle(String sound, String text) {
        add("subtitles." + modId + "." + sound, text);
    }

    /** {@code jei.<modid>.<key>}. */
    protected void jei(String key, String text) {
        add("jei." + modId + "." + key, text);
    }

    /** {@code some_block_id} -> {@code Some Block Id}, for names taken from registry ids. */
    protected static String toTitleCase(String id) {
        StringBuilder sb = new StringBuilder();
        for (String word : id.split("_")) {
            if (word.isEmpty()) continue;
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return sb.toString();
    }
}
