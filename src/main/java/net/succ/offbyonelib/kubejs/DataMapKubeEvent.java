package net.succ.offbyonelib.kubejs;

import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.latvian.mods.kubejs.script.data.VirtualDataMapFile;
import dev.latvian.mods.kubejs.util.JsonUtils;
import dev.latvian.mods.rhino.Context;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

/**
 * The event scripts get for one data map. {@code target} is an id ({@code 'minecraft:lava'}) or a
 * tag ({@code '#c:steam'}); {@code value} is a plain object in the data map's JSON format.
 * <pre>{@code
 * MyModEvents.engineFuels(event => {
 *   event.add('minecraft:lava', { ticks_per_bucket: 400 })
 *   event.remove('#c:biofuel')
 * })
 * }</pre>
 */
public class DataMapKubeEvent<R, T> implements KubeEvent {

    private final DataMapType<R, T> type;
    private final Registry<R> registry;
    private final VirtualDataMapFile<R, T> file;
    private final DynamicOps<JsonElement> ops;

    DataMapKubeEvent(DataMapType<R, T> type, VirtualDataMapFile<R, T> file, DynamicOps<JsonElement> ops) {
        this.type = type;
        this.registry = file.registry;
        this.file = file;
        this.ops = ops;
    }

    /** Adds or replaces the value for an id or a #tag. */
    public void add(Context cx, String target, Object value) {
        T decoded = type.codec().parse(ops, JsonUtils.of(cx, value)).getOrThrow(error ->
                new IllegalArgumentException("Invalid value for " + type.id() + " on " + target + ": " + error));
        if (target.startsWith("#")) file.addTag(tag(target), decoded);
        else file.add(element(target), decoded);
    }

    /** Removes the value from an id or a #tag, including one set by the mod or a datapack. */
    public void remove(String target) {
        if (target.startsWith("#")) file.removeTag(tag(target));
        else file.remove(element(target));
    }

    private TagKey<R> tag(String target) {
        return TagKey.create(registry.key(), ResourceLocation.parse(target.substring(1)));
    }

    private R element(String target) {
        ResourceLocation id = ResourceLocation.parse(target);
        return registry.getOptional(id).orElseThrow(() ->
                new IllegalArgumentException("Unknown " + registry.key().location().getPath() + " " + id + " for " + type.id()));
    }
}
