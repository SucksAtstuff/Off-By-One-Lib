package net.succ.offbyonelib.datagen;

import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.foundation.PonderIndex;

import java.util.function.BiConsumer;

/**
 * Ponder tag and scene text for the lang provider, taken straight from the scene code, so it's
 * written in one place. Ponder normally registers plugins during client setup, which doesn't run
 * in datagen, so this registers the plugin first.
 */
public final class PonderLang {

    private PonderLang() {}

    /** Call from a LanguageProvider's addTranslations: {@code PonderLang.provide(new MyPonderPlugin(), MODID, this::add)}. */
    public static void provide(PonderPlugin plugin, String modId, BiConsumer<String, String> add) {
        PonderIndex.addPlugin(plugin);
        PonderIndex.getLangAccess().provideLang(modId, add);
    }
}
