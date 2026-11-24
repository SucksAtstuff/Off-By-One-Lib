package net.succ.offbyonelib.kubejs;

import com.mojang.logging.LogUtils;
import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventGroupRegistry;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.generator.KubeDataGenerator;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.ScriptType;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.RegistryManager;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The library's only KubeJS plugin, loaded by KubeJS through {@code kubejs.plugins.txt}, so
 * nothing here is touched without KubeJS installed. Each mod that ships a
 * {@link ScriptDataMaps#FILE} gets an event group with one server event per listed data map,
 * fired on every data reload; what scripts add goes into KubeJS's generated datapack, on top of
 * the mod's own defaults and other datapacks.
 */
public class OffByOneKubeJSPlugin implements KubeJSPlugin {

    private static final Logger LOGGER = LogUtils.getLogger();

    private record MapEvent(EventHandler handler, ResourceLocation dataMap) {}

    private final List<MapEvent> events = new ArrayList<>();

    @Override
    public void registerEvents(EventGroupRegistry registry) {
        // Mods may share a group name, so groups are merged by name
        Map<String, EventGroup> groups = new LinkedHashMap<>();
        for (ScriptDataMaps maps : ScriptDataMaps.scan()) {
            EventGroup group = groups.computeIfAbsent(maps.eventGroup(), EventGroup::of);
            maps.dataMaps().forEach((name, id) ->
                    events.add(new MapEvent(group.server(name, () -> DataMapKubeEvent.class), id)));
        }
        groups.values().forEach(registry::register);
    }

    @Override
    public void generateData(KubeDataGenerator generator) {
        for (MapEvent event : events) {
            if (!event.handler().hasListeners()) continue;
            DataMapType<?, ?> type = find(event.dataMap());
            if (type == null) {
                LOGGER.error("No data map {} is registered; its KubeJS event does nothing", event.dataMap());
                continue;
            }
            post(generator, event.handler(), type);
        }
    }

    private static <R, T> void post(KubeDataGenerator generator, EventHandler handler, DataMapType<R, T> type) {
        generator.dataMap(type, file -> handler.post(ScriptType.SERVER,
                new DataMapKubeEvent<>(type, file, generator.getRegistries().json())));
    }

    private static DataMapType<?, ?> find(ResourceLocation id) {
        for (var byRegistry : RegistryManager.getDataMaps().values()) {
            DataMapType<?, ?> type = byRegistry.get(id);
            if (type != null) return type;
        }
        return null;
    }
}
